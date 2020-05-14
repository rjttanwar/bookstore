package com.netent.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    @NotNull(message = "{book.isbn.empty}")
    private Long isbn;

    @NotNull
    @NotEmpty(message = "{book.title.empty}")
    private String title;

    @NotNull
    @NotEmpty(message = "{book.author.empty}")
    private String author;

    @NotNull(message = "{book.price.empty}")
    private Float price;
}
