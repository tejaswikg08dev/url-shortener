package com.urlshortener.analytics.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyAggregationJob {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void aggregateDailyStats(){
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Running DailyAggregationJob at {}", yesterday);

        String sql = """
                INSERT INTO url_daily_stats (short_key, stat_date, click_count, unique_visitors)
                SELECT short_key, DATE(clicked_at), COUNT(*), COUNT(DISTINCT ip_hash)
                FROM click_events
                WHERE DATE(clicked_at) = ?
                GROUP BY short_key, DATE(clicked_at)
                ON CONFLICT(short_key, stat_date)
                DO UPDATE SET
                    click_count = EXCLUDED.click_count,
                    unique_visitors = EXCLUDED.unique_visitors
                """;

        int rows = jdbcTemplate.update(sql, yesterday);
        log.info("Aggregated {} rows for {}", rows, yesterday);
    }
}
