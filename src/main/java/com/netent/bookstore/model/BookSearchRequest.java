package com.netent.bookstore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BookSearchRequest {
    private Long isbn;
    private String title;
    private String author;
}
