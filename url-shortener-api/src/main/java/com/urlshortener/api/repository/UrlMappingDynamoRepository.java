package com.urlshortener.api.repository;

import com.urlshortener.api.model.UrlMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UrlMappingDynamoRepository {

    private final DynamoDbEnhancedClient dynamoDbClient;

    private DynamoDbTable<UrlMapping> getTable(){
        return dynamoDbClient.table("url_mappings",
                TableSchema.fromBean(UrlMapping.class));
    }

    public void save(UrlMapping mapping){
        getTable().putItem(mapping);
        log.debug("Saved URL mapping: {} -> {}", mapping.getShortKey(), mapping.getLongUrl());
    }

    public Optional<UrlMapping> findByShortKey(String shortKey){
        UrlMapping result = getTable().getItem(Key.builder().partitionValue(shortKey).build());
        return Optional.ofNullable(result);
    }

    public void delete(String shortKey){
        getTable().deleteItem(Key.builder().partitionValue(shortKey).build());
        log.debug("Deleted URL mapping: {}", shortKey);
    }

    public boolean exists(String shortKey){
        return findByShortKey(shortKey).isPresent();
    }
}
