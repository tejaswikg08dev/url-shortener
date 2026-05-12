package com.urlshortener.api.service;

import com.urlshortener.common.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitService {

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.ip.capacity:100}")
    private int ipCapacity;

    @Value("${app.rate-limit.user.capacity:1000}")
    private int userCapacity;

    public void checkIpRateLimit(String ip){
        Bucket bucket = ipBuckets.computeIfAbsent(ip, k -> createBucket(ipCapacity));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            throw new RateLimitExceededException(waitSeconds);
        }
    }

    public void checkUserRateLimit(String userId){
        Bucket bucket = userBuckets.computeIfAbsent(userId, k->createBucket(userCapacity));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            throw new RateLimitExceededException(waitSeconds);
        }
    }

    private Bucket createBucket(int capacity){
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1)))).build();
    }
}
