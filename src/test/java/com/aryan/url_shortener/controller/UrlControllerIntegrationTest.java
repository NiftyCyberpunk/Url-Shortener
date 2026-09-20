package com.aryan.url_shortener.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest 
@ActiveProfiles ("test")
@AutoConfigureMockMvc 
public class UrlControllerIntegrationTest {
    
    @Autowired 
    private MockMvc mockMvc;

    @Test
    void shouldCreateShortUrl() throws Exception {

        mockMvc.perform(
            post("/api/url-shortener")
            .contentType("application/json")
            .content("""
                {
                    "originalUrl": "https://example.com"
                }
            """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortUrl").exists());
    }

    @Test
    void shouldRejectInvalidUrl() throws Exception {

        mockMvc.perform(
                post("/api/url-shortener")
                .contentType("application/json")
                .content("""
                    {
                        "originalUrl": "google.com"
                    }
                """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
    }

    @Test 
    void shouldRejectExpiredUrlTime() throws Exception {

        mockMvc.perform(
            post("/api/url-shortener")
            .contentType("application/json")
            .content("""
                {
                    "originalUrl": "https://example.com",
                    "expiresAt": "2020-01-01T00:00:00"
                }
            """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
    }

    @Test 
    void shouldCreateAndRedirectShortUrl() throws Exception {

        MvcResult createResult = mockMvc.perform(
            post("/api/url-shortener")
            .contentType("application/json")
            .content("""
                        {
                            "originalUrl": "https://example.com"
                        }
                    """)
        )
        .andExpect(status().isCreated())
        .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode resposne = objectMapper.readTree(
            createResult.getResponse().getContentAsString()
        );

        String shortUrl = resposne.get("shortUrl").asText();

        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/nifty/" + shortCode)
        )
        .andExpect(status().isFound())
        .andExpect(header().string("Location","https://example.com"));
    }
}
