package com.netent.bookstore.dao;

import com.netent.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    public Optional<Book> findByIsbn(long isbn);
}
