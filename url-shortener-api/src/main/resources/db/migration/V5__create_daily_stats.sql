CREATE TABLE url_daily_stats (
                                 short_key       VARCHAR(10),
                                 stat_date       DATE,
                                 click_count     BIGINT DEFAULT 0,
                                 unique_visitors BIGINT DEFAULT 0,
                                 PRIMARY KEY (short_key, stat_date)
    -- Composite primary key: one row per URL per day
);