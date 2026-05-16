package com.urlshortener.analytics.service;

import com.urlshortener.analytics.model.ClickEvent;
import com.urlshortener.common.dto.ClickEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ClickEventEnrichmentService {

    private final GeoIpService geoIpService;
    private final UserAgentParserService uaParserService;

    private static final String IP_HASH_SALT = "url-shortener-salt-2026";

    public ClickEvent enrich(ClickEventMessage message){
        return ClickEvent.builder()
                .shortKey(message.shortKey())
                .clickedAt(message.timestamp())
                .ipHash(hashIp(message.ip()))
                .userAgent(truncate(message.userAgent(), 500))
                .referrer(truncate(message.referrer(), 500))
                .countryCode(geoIpService.lookupCountry(message.ip()))
                .deviceType(uaParserService.parseDeviceType(message.userAgent()))
                .build();
    }

    private String hashIp(String ip){
        if (ip == null) return null;

        String toHash = ip + IP_HASH_SALT;
        return DigestUtils.md5DigestAsHex(toHash.getBytes(StandardCharsets.UTF_8));
    }

    private String truncate(String value, int maxLength){
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
