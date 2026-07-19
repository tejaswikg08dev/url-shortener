package com.urlshortener.api.controller;

import com.urlshortener.api.security.UserPrincipal;
import com.urlshortener.api.service.QrcodeService;
import com.urlshortener.api.service.UrlService;
import com.urlshortener.common.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.urlshortener.commons.dto.UpdateUrlRequest;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Slf4j
public class UrlController {
    private final UrlService urlService;
    private final QrcodeService qrcodeService;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping
    public ResponseEntity<UrlResponse> createUrl(
            @Valid @RequestBody CreateUrlRequest request, @AuthenticationPrincipal UserPrincipal principal){
        log.info("Received request to create url {}", request.longUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.createUrl(request, principal.getUserId()));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<UrlResponse>> listUrls(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag){
        return ResponseEntity.ok(urlService.getUserUrls(principal.getUserId(), search, tag, page, size));
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<UrlResponse> getUrl(
            @PathVariable String shortKey,
            @AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(urlService.getUrl(shortKey, principal.getUserId()));
    }

    @PutMapping("/{shortKey}")
    public ResponseEntity<UrlResponse> updateUrl(
            @PathVariable String shortKey,
            @Valid @RequestBody UpdateUrlRequest request,
            @AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(urlService.updateUrl(shortKey, request, principal.getUserId()));
    }

    @DeleteMapping("/{shortKey}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable String shortKey,
            @AuthenticationPrincipal UserPrincipal principal){
        urlService.deleteUrl(shortKey, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkCreateResponse> bulkCreate(
            @Valid @RequestBody BulkCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
            ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.bulkCreate(request, principal.getUserId()));
    }

    @GetMapping("/{shortKey}/qr")
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable String shortKey,
            @RequestParam(defaultValue = "250") int size){
        String shortUrl = baseUrl+ "/" + shortKey;
        byte[] qrImage = qrcodeService.generateQrcode(shortUrl, size);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);//CICD Test
    }

}
