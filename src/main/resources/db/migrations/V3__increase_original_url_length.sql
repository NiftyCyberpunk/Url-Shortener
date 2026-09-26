DROP INDEX idx_url_original_expires ON url;

ALTER TABLE url
MODIFY COLUMN original_url VARCHAR(2048) NOT NULL;