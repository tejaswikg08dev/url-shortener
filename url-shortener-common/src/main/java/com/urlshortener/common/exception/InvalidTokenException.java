package com.urlshortener.common.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String reason)
    {
        super("Invalid token: " + reason);
    }
}
