-- PostgreSQL sequence for generating unique short key IDs
-- Starts at 100,000,000 so all keys are 7+ characters in base62
CREATE SEQUENCE short_key_seq START 100000000 INCREMENT 1;