package com.urlshortener.api.service;

import com.sun.jdi.LongValue;
import com.urlshortener.common.util.Base62Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class KeyGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    private final Queue<Long> keyBuffer = new ConcurrentLinkedQueue<>();

    private final int batchSize;

    private final ReentrantLock refillLock = new ReentrantLock();

    public KeyGeneratorService(JdbcTemplate jdbcTemplate,
                               @Value("${app.key-generator.batch-size:1000}") int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.batchSize = batchSize;
    }

    public String nextKey(){
        Long id = keyBuffer.poll();
        if(id == null){
            refillBuffer();
            id = keyBuffer.poll();
            if(id == null){
                throw new RuntimeException("Failed to generate key - buffer empty after refill");
            }
        }

        if (keyBuffer.size() < batchSize  / 4){
            CompletableFuture.runAsync(this::refillBuffer);
        }

        return Base62Encoder.encode(id);
    }

    private void refillBuffer(){
        if(refillLock.tryLock())return;
        try{
            if(keyBuffer.size() >= batchSize / 2) return;

            String sql = "SELECT nextval('short_key_seq') FROM generate_series(1, ?)";

            List<Long> ids = jdbcTemplate.queryForList(sql, Long.class, batchSize);

            keyBuffer.addAll(ids);

            log.info("Refilled key buffer with {} keys. Buffer size: {}", ids.size(), keyBuffer.size());
        } finally{
            refillLock.unlock();
        }
    }

}
