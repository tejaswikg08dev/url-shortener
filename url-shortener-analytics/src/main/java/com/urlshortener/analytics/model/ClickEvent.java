package com.urlshortener.analytics.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "click_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_key", nullable = false, length = 10)
    private String shortKey;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "referrer", length = 500)
    private String referrer;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "device_type", length = 20)
    private String deviceType;
}
