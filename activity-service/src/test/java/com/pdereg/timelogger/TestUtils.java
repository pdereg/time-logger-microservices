package com.pdereg.timelogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdereg.timelogger.domain.Activity;
import org.apache.commons.lang3.RandomStringUtils;

public class TestUtils {

    private TestUtils() {

    }

    public static String generateRandomActivityName() {
        return generateRandomString(Activity.MIN_NAME_SIZE);
    }

    public static String generateRandomString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static long generateActivityDuration() {
        return Activity.MIN_REQUIRED_DURATION;
    }

    public static boolean[] generateActivityWeekdays() {
        return new boolean[]{true, false, true, false, true, false, true};
    }

    public static <T> byte[] toJson(T object) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
