package com.example.piggy_saving.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class DecodeBase64ToObject {
    private final ObjectMapper objectMapper;
    public <T> T decodeBase64ToObject(String base64, Class<T> clazz) {
        try {
            // Step 1: Decode Base64
            byte[] decodedBytes = Base64.getDecoder().decode(base64);

            // Step 2: Convert to JSON string
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            // Step 3: Convert JSON → Object
            return objectMapper.readValue(json, clazz);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Base64 payload", e);
        }
    }
}
