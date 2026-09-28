package com.aryan.url_shortener.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class HashUtilTest {

    @Test 
    void shouldReturnSameHash_whenInputIsSame() {
        String url = "https://google.com";

        String hash1 = HashUtil.sha256(url);
        String hash2 = HashUtil.sha256(url);

        assertEquals(hash1, hash2);
    } 

    @Test 
    void shouldReturnDifferentHash_whenInputIsDifferent() {
        String url1 = "https://google.com";
        String url2 = "https://youtube.com";

        String hash1 = HashUtil.sha256(url1);
        String hash2 = HashUtil.sha256(url2);
        
        assertNotEquals(hash1, hash2);
    }
}
