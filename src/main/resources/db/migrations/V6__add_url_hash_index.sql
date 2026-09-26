ALTER TABLE url
MODIFY COLUMN url_hash CHAR(64) NOT NULL;

CREATE INDEX idx_url_original_expires
ON url(url_hash, expires_at);