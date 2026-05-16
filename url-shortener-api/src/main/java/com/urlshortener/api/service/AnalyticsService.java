package com.urlshortener.api.service;


import com.urlshortener.api.model.UrlMetadata;
import com.urlshortener.api.repository.UrlMetadataRepository;
import com.urlshortener.common.dto.DailyClickDto;
import com.urlshortener.common.dto.TopItemDto;
import com.urlshortener.common.dto.UrlStatsResponse;
import com.urlshortener.common.exception.ResourceAccessDeniedException;
import com.urlshortener.common.exception.UrlNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UrlMetadataRepository metadataRepo;

    private final JdbcTemplate jdbcTemplate;

    public UrlStatsResponse getStats(String shortKey, int days, Long userId){
        UrlMetadata metadata = verifyOwnership(shortKey, userId);

        long uniqueVisitors = getUniqueVisitors(shortKey);
        List<DailyClickDto> clicksByDay = getClickTimeSeries(shortKey, days);
        List<TopItemDto> topCountries = getTopCountries(shortKey, 10);
        List<TopItemDto> topReferrers = getTopReferrers(shortKey, 10);
        List<TopItemDto> deviceBreakdown = getDeviceBreakdown(shortKey);

        return new UrlStatsResponse(
                shortKey,
                metadata.getClickCount(),
                uniqueVisitors,
                metadata.getCreatedAt(),
                clicksByDay,
                topCountries,
                topReferrers,
                deviceBreakdown);
    }

    public List<DailyClickDto> getClickTimeSeries(String shortKey, int days, Long userId){
        verifyOwnership(shortKey, userId);
        return getClickTimeSeries(shortKey, days);
    }

    public List<TopItemDto> getTopReferrers(String shortKey, int limit, Long userId){
        verifyOwnership(shortKey, userId);
        return getTopReferrers(shortKey, limit);
    }

    public List<TopItemDto> getTopCountries(String shortKey, int limit, Long userId){
        verifyOwnership(shortKey, userId);
        return getTopCountries(shortKey, limit);
    }

    public List<TopItemDto> getDeviceBreakdown(String shortKey, Long userId){
        verifyOwnership(shortKey, userId);
        return getDeviceBreakdown(shortKey);
    }

    public byte[] exportCsv(String shortKey, Long userId){
        verifyOwnership(shortKey, userId);

        String sql = "SELECT short_key, clicked_at, ip_hash, user_agent, "+
                "referrer, country_code, device_type "+
                "FROM click_events WHERE short_key = ? ORDER BY clicked_at DESC";

        StringBuilder csv = new StringBuilder();
        csv.append("short_key, clicked_at, ip_hash, user_agent, referrer, country_code, device_type\n");

        jdbcTemplate.query(sql,rs -> {
            csv.append(rs.getString("short_key")).append(",")
                    .append(rs.getTimestamp("clicked_at")).append(",")
                    .append(rs.getString("ip_hash")).append(",")
                    .append(escapeCsv(rs.getString("user_agent"))).append(",")
                    .append(escapeCsv(rs.getString("referrer"))).append(",")
                    .append(rs.getString("country_code")).append(",")
                    .append(rs.getString("device_type")).append("\n");
        }, shortKey);

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<DailyClickDto> getClickTimeSeries(String shortKey, int days){
        String sql = """
                SELECT stat_date, click_count, unique_visitors
                FROM url_daily_stats
                WHERE short_key = ?
                AND stat_date >= CURRENT_DATE - CAST(? AS INTEGER) * INTERVAL '1 day'
                ORDER BY stat_date ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailyClickDto(
                rs.getDate("stat_date").toLocalDate(),
                rs.getLong("click_count"),
                rs.getLong("unique_visitors")
        ),  shortKey, days);
    }

    private List<TopItemDto> getTopReferrers(String shortKey, int limit){
        String sql = """
                WITH total AS (
                    SELECT COUNT(*) as cnt FROM click_events WHERE short_key = ?
                    )
                    SELECT
                        COALESCE(NULLIF(referrer, ''), 'Direct') as name,
                        COUNT(*) as count,
                        ROUND(COUNT(*) * 100.0 / GREATEST(total.cnt, 1), 2) as percentage
                    FROM click_events, total
                    WHERE short_key = ?
                    GROUP BY referrer, total.cnt
                    ORDER BY count DESC
                    LIMIT ?;
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new TopItemDto(
                rs.getString("name"),
                rs.getLong("count"),
                rs.getDouble("percentage")
        ), shortKey, shortKey, limit);
    }

    private List<TopItemDto> getTopCountries(String shortKey, int limit){
        String sql = """
                WITH total AS (
                    SELECT COUNT(*) as cnt FROM click_events WHERE short_key = ?
                    )
                    SELECT
                        COALESCE(country_code, 'XX') as name,
                        COUNT(*) as count,
                        ROUND(COUNT(*) * 100.0 / GREATEST(total.cnt, 1), 2) as percentage
                    FROM click_events, total
                    WHERE short_key = ?
                    group by country_code, total.cnt
                    ORDER BY count DESC
                    LIMIT ?;
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new TopItemDto(
                rs.getString("name"),
                rs.getLong("count"),
                rs.getDouble("percentage")
        ), shortKey, shortKey, limit);
    }

    private List<TopItemDto> getDeviceBreakdown(String shortKey){
        String sql = """
                WITH total AS (
                    SELECT COUNT(*) as cnt FROM click_events WHERE short_key = ?
                    )
                    SELECT
                        COALESCE(device_type, 'unknown') as name,
                        COUNT(*) as count,
                        ROUND(COUNT(*) * 100.0 / GREATEST(total.cnt, 1), 2) as percentage
                    FROM click_events, total
                    WHERE short_key = ?
                    GROUP BY device_type, total.cnt
                    ORDER BY count DESC
                    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new TopItemDto(
                rs.getString("name"),
                rs.getLong("count"),
                rs.getDouble("percentage")
        ), shortKey, shortKey);
    }

    private long getUniqueVisitors(String shortKey){
        String sql = "SELECT COUNT(DISTINCT ip_hash) FROM click_events WHERE short_key = ?";
        Long result = jdbcTemplate.queryForObject(sql, Long.class, shortKey);
        return result != null ? result : 0;
    }

    private UrlMetadata verifyOwnership(String shortKey, Long userId){
        UrlMetadata metadata = metadataRepo.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException(shortKey));

        if(!metadata.getUser().getId().equals(userId)){
            throw new ResourceAccessDeniedException(shortKey);
        }
        return metadata;
    }

    private String escapeCsv(String value){
        if(value == null) return "";
        if(value.contains(",") || value.contains("\"") || value.contains("\n")){
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
