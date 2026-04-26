CREATE TABLE click_events (
                              id          BIGSERIAL PRIMARY KEY,
                              short_key   VARCHAR(10) NOT NULL,
                              clicked_at  TIMESTAMP NOT NULL,
                              ip_hash     VARCHAR(64),
                              user_agent  VARCHAR(500),
                              referrer    VARCHAR(500),
                              country_code VARCHAR(3),
                              device_type VARCHAR(20)
);

CREATE INDEX idx_clicks_short_key ON click_events(short_key);
CREATE INDEX idx_clicks_time ON click_events(clicked_at);
CREATE INDEX idx_clicks_key_time ON click_events(short_key, clicked_at);
-- Composite index: fast queries that filter by BOTH short_key AND time range