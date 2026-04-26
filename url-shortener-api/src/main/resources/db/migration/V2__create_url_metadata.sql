CREATE TABLE url_metadata (
                              id              BIGSERIAL PRIMARY KEY,
                              short_key       VARCHAR(10) UNIQUE NOT NULL,
                              long_url        TEXT NOT NULL,
                              user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE,
    -- REFERENCES = foreign key constraint
    -- ON DELETE CASCADE = if user is deleted, their URLs are also deleted
                              is_custom_alias BOOLEAN DEFAULT FALSE,
                              expires_at      TIMESTAMP,
                              tags            TEXT[],
    -- TEXT[] = PostgreSQL array type (stores multiple strings)
                              click_count     BIGINT DEFAULT 0,
                              created_at      TIMESTAMP DEFAULT NOW(),
                              updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_url_short_key ON url_metadata(short_key);
CREATE INDEX idx_url_user_id ON url_metadata(user_id);
CREATE INDEX idx_url_expires ON url_metadata(expires_at);
CREATE INDEX idx_url_tags ON url_metadata USING GIN(tags);
-- GIN index = Generalized Inverted Index
-- Optimized for array/full-text search (find URLs with tag "marketing")