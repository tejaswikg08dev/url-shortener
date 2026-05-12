package com.urlshortener.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "url:";

    public Optional<String> get(String shortKey){
        try{
            String value = redisTemplate.opsForValue().get(KEY_PREFIX + shortKey);
            return Optional.ofNullable(value);
        } catch(Exception e){
            log.warn("Redis GET failed for {}: {}", shortKey, e.getMessage());
            return Optional.empty();
        }
    }

    public void set(String shortKey, String longUrl, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + shortKey, longUrl, ttl);

        }  catch(Exception e){
            log.warn("Redis SET failed for {}: {}", shortKey, e.getMessage());
        }
    }

    public void delete(String shortKey){
        try {
            redisTemplate.delete(KEY_PREFIX + shortKey);
        } catch(Exception e){
            log.warn("Redis DELETE failed for {}: {}", shortKey, e.getMessage());
        }

    }
}
