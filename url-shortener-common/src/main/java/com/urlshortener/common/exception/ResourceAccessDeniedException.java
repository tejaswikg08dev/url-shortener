package com.urlshortener.common.exception;

public class ResourceAccessDeniedException extends RuntimeException {
    public ResourceAccessDeniedException(String resource) {

        super("Access denied to resource " + resource);
    }
}
