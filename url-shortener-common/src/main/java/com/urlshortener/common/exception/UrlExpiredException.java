package com.urlshortener.common.exception;

public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String shortKey) {

        super("URL has expired: " + shortKey);
    }
}
