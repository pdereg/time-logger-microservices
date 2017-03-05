package com.pdereg.timelogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdereg.timelogger.domain.Log;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Calendar;

public class TestUtils {

    private TestUtils() {

    }

    public static String generateRandomString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static long generateDuration() {
        return Log.MIN_DURATION;
    }

    public static long generateStartTime(long duration) {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis() - duration;
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
