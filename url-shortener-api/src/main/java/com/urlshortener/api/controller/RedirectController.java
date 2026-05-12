package com.urlshortener.api.controller;

import com.urlshortener.api.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    public ResponseEntity<Void> redirect(
            @PathVariable String shortKey,
            HttpServletRequest request){
        String longUrl = urlService.resolve(shortKey, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl)).build();
    }
}
