CREATE TABLE refresh_tokens (
                                id          BIGSERIAL PRIMARY KEY,
                                user_id     BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                token       VARCHAR(255) UNIQUE NOT NULL,
                                expires_at  TIMESTAMP NOT NULL,
                                created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
-- Fast lookup when validating a refresh token