package com.urlshortener.common.exception;

public class UrlNotFoundException extends RuntimeException {

    private final String shortKey;

    public UrlNotFoundException(String shortKey) {

        super("URL not found: " + shortKey);
        this.shortKey = shortKey;
    }

    public String getShortKey() {
        return shortKey;
    }
}
