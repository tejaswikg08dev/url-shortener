package com.urlshortener.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.common.dto.ClickEventMessage;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsProducerService {
    private final SqsTemplate sqsTemplate;

    private  final ObjectMapper objectMapper;

    @Value("${app.sqs.click-events-queue}")
    private String queueName;

    @Async
    public void sendClickEvent(ClickEventMessage event){
        try{
            String messageBody = objectMapper.writeValueAsString(event);

            sqsTemplate.send(queueName, messageBody);

            log.debug("Click event sent to SQS for: {}", event.shortKey());
        } catch (Exception e){
            log.error("Failed to send click event to SQS for {}: {}", event.shortKey(), e.getMessage());
        }
    }
}
