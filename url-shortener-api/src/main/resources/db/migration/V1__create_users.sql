-- V1: Create the users table
-- This is the first migration — runs when the app starts for the first time

CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
    -- BIGSERIAL = auto-incrementing BIGINT (1, 2, 3, ...)
    -- PRIMARY KEY = unique identifier for each row
                       email           VARCHAR(255) UNIQUE NOT NULL,
                       password_hash   VARCHAR(255) NOT NULL,
                       name            VARCHAR(100),
                       role            VARCHAR(20) DEFAULT 'USER',
                       api_key         VARCHAR(64) UNIQUE,
                       created_at      TIMESTAMP DEFAULT NOW(),
                       updated_at      TIMESTAMP DEFAULT NOW()
);

-- Index on email for fast lookups during login
CREATE INDEX idx_users_email ON users(email);
-- Without this index: login does a full table scan (slow)
-- With this index: login does an index lookup (fast)