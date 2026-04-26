package com.urlshortener.common.exception;

public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String reason) {
        super("Invalid URL: " + reason);
    }
}
