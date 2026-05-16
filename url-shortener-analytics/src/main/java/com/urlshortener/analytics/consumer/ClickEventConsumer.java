package com.urlshortener.analytics.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.analytics.model.ClickEvent;
import com.urlshortener.analytics.repository.ClickEventRepository;
import com.urlshortener.analytics.service.ClickEventEnrichmentService;
import com.urlshortener.common.dto.ClickEventMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

    private final ClickEventEnrichmentService enrichmentService;
    private final ClickEventRepository clickEventRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;


    @SqsListener("${app.sqs.click-events-queue}")
    public void handleClickEvent(String messageBody){
        try {
            ClickEventMessage message = objectMapper.readValue(messageBody, ClickEventMessage.class);

            ClickEvent enriched = enrichmentService.enrich(message);

            clickEventRepository.save(enriched);

            jdbcTemplate.update(
                    "UPDATE url_metadata SET click_count = click_count + 1 WHERE short_key = ?",
                    message.shortKey());

            log.debug("Processed click for: {}", message.shortKey());
        } catch (Exception ex) {
            log.error("Failed to process click for: {}", messageBody, ex);
            throw new RuntimeException(ex);
        }
    }
}
