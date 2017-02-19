package com.pdereg.timelogger.configuration;

import com.pdereg.timelogger.security.jwt.JwtHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

/**
 * Provides beans for common configuration that is used across other microservices.
 */
@Configuration
public class CommonConfiguration {

    public static final String SECRET_ENV_KEY = "TIME_LOGGER_SECRET";
    public static final String ISSUER_NAME = "time-logger";

    private Environment environment;

    @Autowired
    public CommonConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public JwtHandler jwtHandler() {
        SecretKey secretKey = getJwtSecretKey();
        return new JwtHandler(secretKey, Duration.ofHours(1), ISSUER_NAME, ISSUER_NAME);
    }

    private SecretKey getJwtSecretKey() {
        String secret = environment.getProperty(SECRET_ENV_KEY, "");
        return new SecretKeySpec(secret.getBytes(), "AES");
    }
}
