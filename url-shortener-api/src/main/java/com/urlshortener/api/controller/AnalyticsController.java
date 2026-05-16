package com.urlshortener.api.controller;

import com.urlshortener.api.security.UserPrincipal;
import com.urlshortener.api.service.AnalyticsService;
import com.urlshortener.common.dto.DailyClickDto;
import com.urlshortener.common.dto.TopItemDto;
import com.urlshortener.common.dto.UrlStatsResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/urls/{shortKey}/stats")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<UrlStatsResponse> getStats(
            @PathVariable String shortKey,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(analyticsService.getStats(shortKey, days, principal.getUserId()));
    }

    @GetMapping("/clicks")
    public ResponseEntity<List<DailyClickDto>> getClickTimeSeries(
            @PathVariable String shortKey,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(analyticsService.getClickTimeSeries(shortKey, days, principal.getUserId()));
    }

    @GetMapping("/referrers")
    public ResponseEntity<List<TopItemDto>> getTopReferrers(
            @PathVariable String shortKey,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(analyticsService.getTopReferrers(shortKey, limit, principal.getUserId()));
    }

    @GetMapping("/countries")
    public ResponseEntity<List<TopItemDto>> getTopCountries(
            @PathVariable String shortKey,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(analyticsService.getTopCountries(shortKey, limit, principal.getUserId()));
    }

    @GetMapping("/devices")
    public ResponseEntity<List<TopItemDto>> getDeviceBreakdown(
            @PathVariable String shortKey,
            @AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(analyticsService.getDeviceBreakdown(shortKey, principal.getUserId()));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable String shortKey,
            @AuthenticationPrincipal UserPrincipal principal){
        byte[] csv = analyticsService.exportCsv(shortKey, principal.getUserId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header("Content-Disposition",
                        "attachment; filename=\"analytics-"+ shortKey +".csv\"")
                .body(csv);
    }
}
