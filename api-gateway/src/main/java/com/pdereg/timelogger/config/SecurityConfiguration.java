package com.pdereg.timelogger.config;

import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.security.jwt.JwtHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;
import java.util.Collection;

/**
 * Provides beans for security configuration.
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String GATEWAY_USERNAME = "_GATEWAY";
    private static final String AUTHORIZATION_BEARER_VALUE = "Bearer ";

    private Authentication authentication;

    @Bean
    @Qualifier("authorization")
    public String authorizationHeaderValue(JwtHandler handler) {
        final String rawToken = handler.createToken(getAuthentication());
        return AUTHORIZATION_BEARER_VALUE + rawToken;
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
                .anyRequest().permitAll();
    }

    private Authentication getAuthentication() {
        if (authentication == null) {
            authentication = createNewAuthentication();
        }

        return authentication;
    }

    private Authentication createNewAuthentication() {
        final SimpleGrantedAuthority userAuthority = new SimpleGrantedAuthority(Authorities.USER);
        final SimpleGrantedAuthority gatewayAuthority = new SimpleGrantedAuthority(Authorities.GATEWAY);
        final Collection<? extends GrantedAuthority> authorities = Arrays.asList(userAuthority, gatewayAuthority);

        final User user = new User(GATEWAY_USERNAME, "", authorities);
        return new UsernamePasswordAuthenticationToken(user, "", authorities);
    }
}
