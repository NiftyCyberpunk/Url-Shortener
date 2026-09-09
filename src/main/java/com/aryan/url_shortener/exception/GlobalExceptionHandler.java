package com.aryan.url_shortener.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUrlNotFoundException(UrlNotFoundException ex){
        Map<String, String> error = Map.of("message", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }
}
