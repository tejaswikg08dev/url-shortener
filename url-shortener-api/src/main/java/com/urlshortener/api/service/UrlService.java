package com.urlshortener.api.service;

import com.urlshortener.api.model.UrlMapping;
import com.urlshortener.api.model.UrlMetadata;
import com.urlshortener.api.model.User;
import com.urlshortener.api.repository.UrlMappingDynamoRepository;
import com.urlshortener.api.repository.UrlMetadataRepository;
import com.urlshortener.common.dto.*;
import com.urlshortener.common.exception.AliasAlreadyExistsException;
import com.urlshortener.common.exception.ResourceAccessDeniedException;
import com.urlshortener.common.exception.UrlExpiredException;
import com.urlshortener.common.exception.UrlNotFoundException;
import com.urlshortener.common.util.UrlValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisSubscribedConnectionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {
    private final UrlMetadataRepository metadataRepo;
    private final UrlMappingDynamoRepository dynamoRepo;
    private final KeyGeneratorService keyGenerator;
    private final RedisService redisService;
    private final SqsProducerService sqsProducer;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.url.default-expiry-days:365}")
    private int defaultExpiryDays;

    @Transactional
    public UrlResponse createUrl(CreateUrlRequest request, Long userId) {
        UrlValidator.validate(request.longUrl(), Set.of());

        String shortKey;
        boolean isCustom = false;

        if(request.customAlias() != null && !request.customAlias().isBlank()) {
            shortKey = request.customAlias();
            isCustom = true;
            if(dynamoRepo.exists(shortKey)) {
                throw new AliasAlreadyExistsException(shortKey);
            }
        } else {
            shortKey = keyGenerator.nextKey();
        }

        Instant expiresAt = request.expiresAt() != null ? request.expiresAt() : Instant.now().plus(defaultExpiryDays, ChronoUnit.DAYS);

        UrlMapping mapping = new UrlMapping();
        mapping.setShortKey(shortKey);
        mapping.setLongUrl(request.longUrl());
        mapping.setExpiresAt(expiresAt.getEpochSecond());
        mapping.setCreatedAt(Instant.now().getEpochSecond());
        dynamoRepo.save(mapping);

        User userRef = new User();
        userRef.setId(userId);

        UrlMetadata metadata = UrlMetadata.builder()
                .shortKey(shortKey)
                .longUrl(request.longUrl())
                .user(userRef)
                .customAlias(isCustom)
                .expiresAt(expiresAt)
                .tags(request.tags() != null ? request.tags() : List.of())
                .build();
        metadataRepo.save(metadata);

        Duration cacheTtl = Duration.between(Instant.now(), expiresAt);
        if (cacheTtl.toHours() > 1) cacheTtl = Duration.ofHours(1);
        redisService.set(shortKey, request.longUrl(), cacheTtl);

        log.info("URL created: {} -> {} by user {}", shortKey, request.longUrl(), userId);
        return buildUrlResponse(metadata);
    }

    public String resolve(String shortKey, HttpServletRequest request) {
        String longUrl;

        Optional<String> cached = redisService.get(shortKey);
        if(cached.isPresent()) {
            longUrl = cached.get();
            log.debug("Cache HIT for: {}", shortKey);
        } else {
            log.debug("Cache MISS for: {}", shortKey);
            UrlMapping mapping = dynamoRepo.findByShortKey(shortKey)
                    .orElseThrow(() -> new UrlNotFoundException(shortKey));

            if (mapping.getExpiresAt() != null && Instant.ofEpochSecond(mapping.getExpiresAt()).isBefore(Instant.now())) {
                throw new UrlExpiredException(shortKey);
            }

            longUrl = mapping.getLongUrl();

            Duration ttl = Duration.ofHours(1);
            if(mapping.getExpiresAt() != null) {
                Duration untilExpiry = Duration.between(Instant.now(), Instant.ofEpochSecond(mapping.getExpiresAt()));
                if(untilExpiry.toHours() < 1 && !untilExpiry.isNegative()) {
                    ttl = untilExpiry;
                }
            }
            redisService.set(shortKey, longUrl, ttl);
        }

        ClickEventMessage clickEvent = new ClickEventMessage(shortKey, getClientIp(request), request.getHeader("User-Agent"),
                request.getHeader("Referer"),
                Instant.now());
        sqsProducer.sendClickEvent(clickEvent);

        return longUrl;
    }

    public UrlResponse getUrl(String shortKey, Long userId) {
        UrlMetadata metadata = metadataRepo.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException(shortKey));

        if(!metadata.getUser().getId().equals(userId)) {
            throw new RedisSubscribedConnectionException(shortKey);
        }
        return buildUrlResponse(metadata);
    }

    public PagedResponse<UrlResponse> getUserUrls(Long userId, String search,
                                                  String tag, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<UrlMetadata> result;

        if (search != null && !search.isBlank()){
            result = metadataRepo.searchByUser(userId, search, pageable);
        } else {
            result = metadataRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<UrlResponse> content = result.getContent().stream()
                .map(this::buildUrlResponse)
                .toList();

        return new PagedResponse<>(content, page, size, result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

    @Transactional
    public UrlResponse updateUrl(String shortKey, com.urlshortener.commons.dto.UpdateUrlRequest request, Long userId) {
        UrlMetadata metadata = metadataRepo.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException(shortKey));

        if(!metadata.getUser().getId().equals(userId)) {
            throw  new ResourceAccessDeniedException(shortKey);
        }

        if(request.longUrl() != null){
            UrlValidator.validate(request.longUrl(), Set.of());
            metadata.setLongUrl(request.longUrl());

            UrlMapping mapping = dynamoRepo.findByShortKey(shortKey)
                    .orElseThrow(() -> new UrlNotFoundException(shortKey));
            mapping.setLongUrl(request.longUrl());
            dynamoRepo.save(mapping);
        }

        if (request.expiresAt() != null) {
            metadata.setExpiresAt(request.expiresAt());
        }

        if(request.tags() != null){
            metadata.setTags(request.tags());
        }

        metadataRepo.save(metadata);

        redisService.delete(shortKey);

        return buildUrlResponse(metadata);
    }


    @Transactional
    public void deleteUrl(String shortKey, Long userId) {
        UrlMetadata metadata = metadataRepo.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException(shortKey));

        if(!metadata.getUser().getId().equals(userId)) {
            throw  new ResourceAccessDeniedException(shortKey);
        }

        redisService.delete(shortKey);
        dynamoRepo.delete(shortKey);
        metadataRepo.deleteByShortKey(shortKey);

        log.info("URL deleted: {} by  user {}", shortKey, userId);
    }

    public BulkCreateResponse bulkCreate(BulkCreateRequest request, Long userId) {
        List<UrlResponse> successful = new ArrayList<>();
        List<BulkError> failed = new ArrayList<>();

        for(int i=0; i<request.urls().size(); i++){
            try{
                UrlResponse response = createUrl(request.urls().get(i), userId);
                successful.add(response);
            } catch (Exception e) {
                failed.add(new BulkError(i, request.urls().get(i).longUrl(), e.getMessage()));
            }
        }

        return new BulkCreateResponse(successful, failed, request.urls().size(), successful.size(), failed.size());
    }

    public PagedResponse<UrlResponse> getAllUrls(int page, int size){
        Page<UrlMetadata> result = metadataRepo.findAll(PageRequest.of(page, size));
        List<UrlResponse> content = result.getContent().stream()
                .map(this::buildUrlResponse).toList();

        return new PagedResponse<>(content, page, size, result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

    @Transactional
    public void adminDeleteUrl(String shortKey){
        redisService.delete(shortKey);
        dynamoRepo.delete(shortKey);
        metadataRepo.deleteByShortKey(shortKey);
        log.info("URL admin-deleted: {}", shortKey);
    }

    private UrlResponse buildUrlResponse(UrlMetadata metadata){
        return new UrlResponse(
                metadata.getShortKey(),
                baseUrl + "/" +metadata.getShortKey(),
                metadata.getLongUrl(),
                metadata.isCustomAlias(),
                metadata.getExpiresAt(),
                metadata.getTags(),
                metadata.getClickCount(),
                metadata.getCreatedAt()
        );
    }

    private String getClientIp(HttpServletRequest request){
        String xff = request.getHeader("X-Forwarded-For");
        if(xff != null && !xff.isEmpty()){
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
































}
