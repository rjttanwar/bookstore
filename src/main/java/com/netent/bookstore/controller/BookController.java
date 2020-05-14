package com.netent.bookstore.controller;

import com.netent.bookstore.model.BookDTO;
import com.netent.bookstore.model.BookSearchRequest;
import com.netent.bookstore.model.NotFoundException;
import com.netent.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/book")
public class BookController {

    @Autowired
    private final BookService bookService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BookDTO save(@RequestBody BookDTO b) {
        log.info("Saving a book with isbn {}", b.getIsbn());
        return this.bookService.save(b);
    }

    @GetMapping(value = "/{isbn}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookDTO get(@PathVariable long isbn) {
        log.info("Find a book with isbn {}", isbn);
        return this.bookService.findByIsbn(isbn).orElseThrow(() -> new NotFoundException("No book found"));
    }

    @GetMapping(value = "/media/{isbn}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> searchMedia(@PathVariable long isbn) {
        log.info("Searching media for a book with isbn {}", isbn);
        return this.bookService.searchMedia(isbn);
    }

    @GetMapping(value = "/buy/{isbn}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookDTO buyBook(@PathVariable long isbn) {
        log.info("Buy a book with isbn {}", isbn);
        return this.bookService.buyBook(isbn);
    }

    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<BookDTO> search(@RequestBody BookSearchRequest request, Pageable pageable) {
        return this.bookService.search(request, pageable);
    }
}
