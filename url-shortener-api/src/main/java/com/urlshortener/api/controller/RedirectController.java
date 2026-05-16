package com.urlshortener.api.controller;

import com.urlshortener.api.service.QrcodeService;
import com.urlshortener.api.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    private final QrcodeService qrcodeService;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortKey,
            HttpServletRequest request){
        String longUrl = urlService.resolve(shortKey, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl)).build();
    }

    @GetMapping("/{shortKey}/qr")
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable String shortKey,
            @RequestParam(defaultValue = "250") int size){
        String shortUrl = baseUrl+ "/" + shortKey;
        byte[] qrImage = qrcodeService.generateQrcode(shortUrl, size);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}
