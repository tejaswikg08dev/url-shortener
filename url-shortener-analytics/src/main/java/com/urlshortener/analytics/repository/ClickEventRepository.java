package com.urlshortener.analytics.repository;

import com.urlshortener.analytics.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
}
