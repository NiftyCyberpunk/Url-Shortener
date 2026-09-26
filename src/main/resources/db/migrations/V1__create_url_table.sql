CREATE TABLE url (
    id BIGINT NOT NULL AUTO_INCREMENT,
    access_count BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    original_url VARCHAR(255) NOT NULL,
    short_code VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_url_short_code (short_code)
);