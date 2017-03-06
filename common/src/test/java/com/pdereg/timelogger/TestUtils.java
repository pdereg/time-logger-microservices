package com.pdereg.timelogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.codec.Base64;

import java.io.IOException;
import java.util.Map;

public class TestUtils {

    private TestUtils() {

    }

    public static String generateRandomString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static String decodeBase64(String encodedString) {
        final byte[] encodedAsBytes = encodedString.getBytes();
        return new String(Base64.decode(encodedAsBytes));
    }

    public static <T> Map<String, T> fromJson(String jsonContent) {
        final TypeReference<Map<String, T>> reference = new TypeReference<Map<String, T>>() {};
        final ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(jsonContent, reference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
