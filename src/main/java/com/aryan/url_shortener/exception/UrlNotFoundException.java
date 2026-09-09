package com.aryan.url_shortener.exception;

public class UrlNotFoundException extends RuntimeException {
    
    public UrlNotFoundException() {
        super("Url not found.");
    }    
}
