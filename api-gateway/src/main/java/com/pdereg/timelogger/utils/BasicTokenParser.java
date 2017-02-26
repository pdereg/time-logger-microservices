package com.pdereg.timelogger.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Base64Utils;

import java.util.Optional;

/**
 * A simple utility for parsing basic authentication tokens.
 */
public class BasicTokenParser {

    private static final String AUTHORIZATION_BASIC_VALUE = "Basic ";
    private static final String AUTHORIZATION_VALUE_SENTINEL = ":";

    private BasicTokenParser() {

    }

    /**
     * Parses provided token and returns username and password as a pair of strings.
     *
     * @param token Authentication token to parse
     * @return A pair of string, where the first value is the username, and second is password
     */
    public static Optional<Pair<String, String>> parse(String token) {
        final String encodedToken = normalizeToken(token);
        final Optional<String> decodedToken = decodeToken(encodedToken);
        return decodedToken.flatMap(BasicTokenParser::splitTokenValues);
    }

    private static String normalizeToken(String token) {
        if (token.toLowerCase().startsWith(AUTHORIZATION_BASIC_VALUE.toLowerCase())) {
            return token.substring(AUTHORIZATION_BASIC_VALUE.length(), token.length());
        }

        return token;
    }

    private static Optional<String> decodeToken(String token) {
        try {
            byte[] decodedAsBytes = Base64Utils.decodeFromString(token);
            String decodedAsString = new String(decodedAsBytes);
            return Optional.of(decodedAsString);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static Optional<Pair<String, String>> splitTokenValues(String token) {
        // split() is not used because password may potentially contain sentinels too
        final int splitIndex = token.indexOf(AUTHORIZATION_VALUE_SENTINEL);
        if (splitIndex == -1) {
            return Optional.empty();
        }

        final String username = token.substring(0, splitIndex);
        final String password = token.substring(splitIndex + 1);
        final Pair<String, String> usernameAndPassword = new ImmutablePair<>(username, password);

        return Optional.of(usernameAndPassword);
    }
}
