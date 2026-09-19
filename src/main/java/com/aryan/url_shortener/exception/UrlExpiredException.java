package com.aryan.url_shortener.exception;

public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException() {
        super("Url is expired.");
    }
}
