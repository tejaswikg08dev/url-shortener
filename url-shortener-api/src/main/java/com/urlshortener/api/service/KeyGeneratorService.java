package com.urlshortener.api.service;

import com.urlshortener.common.util.Base62Encoder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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

    @PostConstruct
    public void init() {
        ensureSequenceExists();
        doRefill();
        log.info("KeyGeneratorService initialized. Buffer size: {}", this.keyBuffer.size());
    }

    public String nextKey(){
        Long id = keyBuffer.poll();
        if(id == null){
            doRefill();
            id = keyBuffer.poll();
            if(id == null){
                throw new IllegalStateException("Failed to generate key - buffer empty after refill");
            }
        }

        if (keyBuffer.size() < batchSize  / 4){
            CompletableFuture.runAsync(this::refillBuffer);
        }

        return Base62Encoder.encode(id);
    }

    private void refillBuffer() {
        if (refillLock.tryLock()) return;
        try {
            doRefill();
        } finally {
            refillLock.unlock();
        }
    }

    private void doRefill() {
            if(keyBuffer.size() >= batchSize / 2) return;
        try {
            String sql = "SELECT nextval('short_key_seq') FROM generate_series(1, ?)";

            List<Long> ids = jdbcTemplate.queryForList(sql, Long.class, batchSize);

            keyBuffer.addAll(ids);

            log.info("Refilled key buffer with {} keys. Buffer size: {}", ids.size(), keyBuffer.size());
        } catch(DataAccessException e){
            log.error("Failed to refill key buffer: {}", e.getMessage());
        }
    }

    private void ensureSequenceExists() {
        try{
            Boolean exists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS(SELECT 1 FROM pg_sequences WHERE sequencename = 'short_key_seq')", Boolean.class);
            if (Boolean.FALSE.equals(exists)) {
                log.warn("Sequence short_key_seq does not exist. Creating it.");
                jdbcTemplate.execute("CREATE SEQUENCE short_key_seq START 100000000 INCREMENT BY 1");
            }
        } catch (DataAccessException e){
            log.error("Failed to verify/create sequence 'short_key_seq': {}", e.getMessage());
        }
    }

}
