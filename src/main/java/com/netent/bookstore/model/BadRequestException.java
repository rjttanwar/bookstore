package com.netent.bookstore.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}