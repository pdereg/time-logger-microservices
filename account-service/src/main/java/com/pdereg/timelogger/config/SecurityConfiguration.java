package com.pdereg.timelogger.config;

import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.security.jwt.JwtFilter;
import com.pdereg.timelogger.security.jwt.JwtHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

/**
 * Provides beans for security configuration.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/**").hasAuthority(Authorities.USER)
                .and()
                .addFilterBefore(getJwtFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private JwtFilter getJwtFilter() {
        return new JwtFilter(jwtHandler());
    }

    // TODO: Implement an actual method
    private JwtHandler jwtHandler() {
        String secret = "test123";
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "AES");
        return new JwtHandler(secretKey, Duration.ofHours(1), "", "");
    }
}
