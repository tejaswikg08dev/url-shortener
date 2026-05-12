package com.urlshortener.api.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class UrlMapping {

    private String shortKey;
    private String longUrl;
    private Long expiresAt;
    private Long createdAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("short_key")
    public String getShortKey() {
        return shortKey;
    }
    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }

    @DynamoDbAttribute("long_url")
    public String getLongUrl() {
        return longUrl;
    }
    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    @DynamoDbAttribute("expires_at")
    public Long getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @DynamoDbAttribute("created_at")
    public Long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
