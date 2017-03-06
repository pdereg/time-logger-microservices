package com.pdereg.timelogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdereg.timelogger.domain.User;
import org.apache.commons.lang3.RandomStringUtils;

public class TestUtils {

    private TestUtils() {

    }

    public static String generateRandomString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static String generateRandomUsername() {
        return generateRandomString(User.MIN_USERNAME_SIZE);
    }

    public static String generateRandomPassword() {
        return generateRandomString(User.MAX_USERNAME_SIZE);
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
