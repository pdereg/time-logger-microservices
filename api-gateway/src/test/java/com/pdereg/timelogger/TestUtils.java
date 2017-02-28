package com.pdereg.timelogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.Base64Utils;

public class TestUtils {

    private TestUtils() {

    }

    public static String generateRandomString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static String createBasicToken(String username, String password) {
        final String rawToken = username + ":" + password;
        return Base64Utils.encodeToString(rawToken.getBytes());
    }

    public static <T> byte[] toJson(T object) {
        final ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
