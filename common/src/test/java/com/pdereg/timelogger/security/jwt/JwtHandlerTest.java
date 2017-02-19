package com.pdereg.timelogger.security.jwt;

import com.pdereg.timelogger.TestUtils;
import com.pdereg.timelogger.security.Authorities;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class JwtHandlerTest {

    private static final String TEST_ISSUER = "common-test";
    private static final Date TEST_ISSUED_AT = new Date();
    private static final Duration TEST_DURATION = Duration.ofDays(1);
    private static final String TEST_AUDIENCE = "common-test";
    private static final String TEST_SUBJECT = "john.doe@example.com";
    private static final String TEST_AUTHORITY = Authorities.USER;
    private static final String TEST_SECRET = "test1234";

    @Mock
    private Authentication authentication;

    private JwtHandler jwtHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        initializeMocks();
        initializeJwtHandler();
    }

    private void initializeMocks() {
        GrantedAuthority authority = new SimpleGrantedAuthority(TEST_AUTHORITY);
        Collection<? extends GrantedAuthority> authorities = Collections.singleton(authority);

        when(authentication.getName()).thenReturn(TEST_SUBJECT);
        doReturn(authorities).when(authentication).getAuthorities();
    }

    private void initializeJwtHandler() {
        SecretKey secretKey = new SecretKeySpec(TEST_SECRET.getBytes(), "AES");
        jwtHandler = new JwtHandler(secretKey, TEST_DURATION, TEST_ISSUER, TEST_AUDIENCE);
    }

    @Test
    public void createToken_returnsCorrectTokenPayload() {
        String token = jwtHandler.createToken(authentication, TEST_ISSUED_AT);
        Map<String, String> payload = extractTokenPayload(token);

        assertEquals(TEST_ISSUER, payload.get("iss"));
        assertEquals(TEST_AUDIENCE, payload.get("aud"));
        assertEquals(TEST_SUBJECT, payload.get("sub"));
        assertEquals(TEST_AUTHORITY, payload.get(JwtHandler.AUTHORITIES_KEY));
    }

    @Test
    public void validateToken_returnsAuthenticationWithCorrectNameIfTokenIsValid() {
        String token = jwtHandler.createToken(authentication);
        Optional<Authentication> authentication = jwtHandler.validateToken(token);

        assertTrue(authentication.isPresent());
        assertEquals(TEST_SUBJECT, authentication.get().getName());
    }

    @Test
    public void validateToken_returnsAuthenticationWithCorrectAuthoritiesIfTokenIsValid() {
        String token = jwtHandler.createToken(authentication);
        Optional<Authentication> authentication = jwtHandler.validateToken(token);

        assertTrue(authentication.isPresent());

        Collection<? extends GrantedAuthority> authorities = authentication.get().getAuthorities();
        boolean result = authorities.stream()
                .map(GrantedAuthority::toString)
                .anyMatch(authority -> authority.equals(TEST_AUTHORITY));

        assertTrue(result);
    }

    @Test
    public void validateToken_returnsEmptyValueIfTokenIsExpired() {
        Date issuedAt = new Date(TEST_ISSUED_AT.getTime() / 2);
        String token = jwtHandler.createToken(authentication, issuedAt);

        Optional<Authentication> authentication = jwtHandler.validateToken(token);
        assertFalse(authentication.isPresent());
    }

    @Test
    public void validateToken_returnsEmptyValueIfTokenIsNotValid() {
        String newSecret = TEST_SECRET + "*";
        SecretKey secretKey = new SecretKeySpec(newSecret.getBytes(), "AES");
        JwtHandler otherJwtHandler = new JwtHandler(secretKey, TEST_DURATION, TEST_ISSUER, TEST_AUDIENCE);

        String token = otherJwtHandler.createToken(authentication, TEST_ISSUED_AT);

        Optional<Authentication> authentication = jwtHandler.validateToken(token);
        assertFalse(authentication.isPresent());
    }

    @Test
    public void validateToken_returnsEmptyValueIfTokenHasIncorrectFormat() {
        String token = TestUtils.generateRandomString(10);

        Optional<Authentication> authentication = jwtHandler.validateToken(token);
        assertFalse(authentication.isPresent());
    }

    private Map<String, String> extractTokenPayload(String encodedToken) {
        int payloadStartIndex = encodedToken.indexOf(".") + 1;
        int payloadEndIndex = encodedToken.lastIndexOf(".");

        String encodedPayload = encodedToken.substring(payloadStartIndex, payloadEndIndex);
        String decodedPayload = TestUtils.decodeBase64(encodedPayload);
        return TestUtils.fromJson(decodedPayload);
    }
}
