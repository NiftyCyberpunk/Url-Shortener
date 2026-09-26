UPDATE url
SET url_hash = SHA2(original_url, 256);