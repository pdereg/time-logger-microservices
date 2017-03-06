package com.pdereg.timelogger.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * Retrieves JSON Web Token from an incoming request's header and attempts user authentication.
 */
public class JwtFilter extends GenericFilterBean {

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String AUTHORIZATION_HEADER_KEY_ALT = "WWW-Authenticate";
    private static final String AUTHORIZATION_BEARER_VALUE = "Bearer ";

    private final JwtHandler jwtHandler;

    public JwtFilter(JwtHandler jwtHandler) {
        this.jwtHandler = jwtHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        final Optional<String> rawToken = extractToken((HttpServletRequest) request);
        if (!rawToken.isPresent()) {
            chain.doFilter(request, response);
            return;
        }

        final Optional<Authentication> authentication = jwtHandler.validateToken(rawToken.get());
        if (!authentication.isPresent()) {
            chain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication.get());
        chain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String rawToken = request.getHeader(AUTHORIZATION_HEADER_KEY);

        if (!isTokenFormatValid(rawToken)) {
            rawToken = request.getHeader(AUTHORIZATION_HEADER_KEY_ALT);

            if (!isTokenFormatValid(rawToken)) {
                return Optional.empty();
            }
        }

        return Optional.of(extractTokenBearer(rawToken));
    }

    private boolean isTokenFormatValid(String token) {
        return StringUtils.hasText(token) && token.startsWith(AUTHORIZATION_BEARER_VALUE);
    }

    private String extractTokenBearer(String token) {
        return token.substring(AUTHORIZATION_BEARER_VALUE.length(), token.length());
    }
}
