package com.netent.bookstore.service;

import com.netent.bookstore.dao.BookRepository;
import com.netent.bookstore.entity.Book;
import com.netent.bookstore.entity.Book_;
import com.netent.bookstore.model.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Validated
@Service
@Slf4j
@AllArgsConstructor
public class BookServiceImpl implements BookService {

    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    private final BookRepository bookRepository;

    @Autowired
    private final MediaService mediaService;

    private final ModelMapper mapper = new ModelMapper();

    @Override
    public Optional<BookDTO> findByIsbn(long isbn) {
        Optional<Book> book = this.bookRepository.findByIsbn(isbn);
        return book.map(b -> mapper.map(b, BookDTO.class));
    }

    @Override
    @Transactional
    public BookDTO save(@Valid BookDTO bookDto) {
        Book book = mapper.map(bookDto, Book.class);
        Optional<Book> b = this.bookRepository.findByIsbn(book.getIsbn());
        if (b.isPresent()) {
            book = b.get();
            checkAuthorAndTitle(book, bookDto);
            book.setPrice(bookDto.getPrice());
            book.setCount(book.getCount() + 1);
            log.info("Increasing the book count for {}", book.getIsbn());
        } else {
            log.info("Adding a new book with isbn {}", book.getIsbn());
            book.setCount(1);
        }
        book = this.bookRepository.save(book);
        return mapper.map(book, BookDTO.class);
    }

    private void checkAuthorAndTitle(Book book, BookDTO bookDto) {
        if (!book.getAuthor().equalsIgnoreCase(bookDto.getAuthor()) || !book.getTitle().equalsIgnoreCase(bookDto.getTitle())) {
            log.debug("Book with isbn {} already exists with different credentials", book.getIsbn());
            throw new BadRequestException("Book already exists with same isbn but with other author/title");
        }
    }

    @Override
    @Transactional
    public BookDTO buyBook(long isbn) {
        Optional<Book> b = this.bookRepository.findByIsbn(isbn);
        if (b.isPresent()) {
            Book book = b.get();
            book.setCount(book.getCount() - 1);
            log.info("Decreasing the book count for {}", isbn);
            this.bookRepository.save(book);
            maintainCount(book);
            return mapper.map(book, BookDTO.class);
        } else {
            log.debug("Book with isbn {} not found", isbn);
            throw new NotFoundException("Book not found");
        }
    }

    @Transactional
    private void maintainCount(Book persistedBook) {
        if (null!=persistedBook && persistedBook.getCount() == 0) {
            log.info("Book count reached zero, adding book with isbn {}", persistedBook.getIsbn());
            persistedBook.setCount(1);
            this.bookRepository.save(persistedBook);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDTO> search(BookSearchRequest request, Pageable pageable) {
        Long totalResult = countFilteredRecords(request);
        log.info("Search with request {} has {} total records", request, totalResult);
        List<BookDTO> books = findRecords(request, pageable);
        return new PageImpl<>(books, pageable, totalResult);
    }

    @Override
    public List<String> searchMedia(long isbn) {
        Optional<BookDTO> optionalBook = findByIsbn(isbn);

        if (optionalBook.isPresent()) {
            BookDTO bookDTO = optionalBook.get();
            List<MediaPost> posts = mediaService.searchMedia();
            return posts
                    .stream()
                    .filter(post -> post.getTitle().toLowerCase().contains(bookDTO.getTitle().toLowerCase()) || post.getBody().toLowerCase().contains(bookDTO.getTitle().toLowerCase()))
                    .map(MediaPost::getTitle)
                    .collect(Collectors.toList());
        }
        throw new NotFoundException("Book not found");
    }

    @Transactional(readOnly = true)
    private Long countFilteredRecords(BookSearchRequest request) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Book> books = countQuery.from(Book.class);
        applyFilter(countQuery, builder, request, books);
        countQuery.select(builder.count(books));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Transactional(readOnly = true)
    private List<BookDTO> findRecords(BookSearchRequest request, Pageable pageable) {
        if (null == pageable) {
            pageable = PageRequest.of(0, 20);
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BookDTO> query = builder.createQuery(BookDTO.class);
        Root<Book> books = query.from(Book.class);
        addProjections(query, books);
        applyFilter(query, builder, request, books);
        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private void addProjections(CriteriaQuery<BookDTO> query, Root<Book> books) {
        query.multiselect(books.get(Book_.isbn),
                books.get(Book_.title),
                books.get(Book_.author),
                books.get(Book_.price)
        );
    }

    private void applyFilter(CriteriaQuery query, CriteriaBuilder builder, BookSearchRequest request, Root<Book> books) {
        List<Predicate> whereClause = new ArrayList<>();
        if (StringUtils.hasText(request.getAuthor())) {
            whereClause.add(builder.like(builder.upper(books.get(Book_.author)), createLikePattern(request.getAuthor())));
        }
        if (StringUtils.hasText(request.getTitle())) {
            whereClause.add(builder.like(builder.upper(books.get(Book_.title)), createLikePattern(request.getTitle())));
        }
        if (null != request.getIsbn()) {
            whereClause.add(builder.equal(books.get(Book_.isbn), request.getIsbn()));
        }
        if (!CollectionUtils.isEmpty(whereClause)) {
            query.where(whereClause.toArray(new Predicate[whereClause.size()]));
        }
    }

    private String createLikePattern(String val) {
        return "%" + val.toUpperCase() + "%";
    }
}
