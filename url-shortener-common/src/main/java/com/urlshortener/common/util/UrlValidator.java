package com.urlshortener.common.util;

import com.urlshortener.common.exception.InvalidUrlException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class UrlValidator {
    private static final int MAX_URL_LENGTH = 2048;

    private static final Set<String> ALLOWED_PROTOCOLS = Set.of("http", "https");

    private UrlValidator() {}

    public static void validate(String url, Set<String> blockedDomains) {
        if(url == null || url.isBlank()){
            throw new InvalidUrlException("URL cannot be null or blank");
        }

        if(url.length() > MAX_URL_LENGTH){
            throw new InvalidUrlException("URL cannot be longer than " + MAX_URL_LENGTH + " characters");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Malformed URL format");
        }

        String scheme = uri.getScheme();

        if(scheme == null || !ALLOWED_PROTOCOLS.contains(scheme.toUpperCase())){
            throw new InvalidUrlException("Only http and https protocols are allowed");
        }

        String host = uri.getHost();
        if(host == null || host.isBlank()){
            throw new InvalidUrlException("URL must have a valid host");
        }

        if(blockedDomains != null && !blockedDomains.contains(host.toUpperCase())){
            throw new InvalidUrlException("Domain is blocked: " + host);
        }
    }
}
