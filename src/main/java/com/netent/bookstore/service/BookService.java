package com.netent.bookstore.service;

import com.netent.bookstore.model.BookDTO;
import com.netent.bookstore.model.BookSearchRequest;
import com.netent.bookstore.model.MediaPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface BookService {
	Optional<BookDTO> findByIsbn(long isbn);

	BookDTO save(@Valid BookDTO bookDto);

	BookDTO buyBook(long isbn);

	Page<BookDTO> search(BookSearchRequest request, Pageable pageable);

    List<String> searchMedia(long isbn);
}
