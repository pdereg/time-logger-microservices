package com.pdereg.timelogger.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JSON Web Token handler. Encapsulates creation and validation of JWTs.
 */
public class JwtHandler {

    private static final String AUTHORITIES_KEY = "auth";

    private final SecretKey secretKey;
    private final Duration tokenValidity;

    public JwtHandler(SecretKey secretKey, Duration tokenValidity) {
        this.secretKey = secretKey;
        this.tokenValidity = tokenValidity;
    }

    /**
     * Creates a new JWT for provided {@code authentication}.
     *
     * @param authentication {@link Authentication} instance for which to create a new JWT
     * @return Newly created JSON Web Token
     */
    public String createToken(Authentication authentication) {
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String parsedAuthorities = parseAuthorities(authorities);

        Date validity = newTokenValidity();

        return Jwts.builder()
                .setSubject(username)
                .claim(AUTHORITIES_KEY, parsedAuthorities)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .setExpiration(validity)
                .compact();
    }

    /**
     * Validates provided {@code rawToken}.
     *
     * @param rawToken Raw JWT to validate
     * @return An {@link Authentication} instance if validation is successful
     */
    public Optional<Authentication> validateToken(String rawToken) {
        Optional<Claims> claims = parseToken(rawToken);
        if (!claims.isPresent()) {
            return Optional.empty();
        }

        String username = claims.get().getSubject();
        Collection<? extends GrantedAuthority> authorities = parseAuthorities(claims.get());

        User user = new User(username, "", authorities);
        return Optional.of(new UsernamePasswordAuthenticationToken(user, "", authorities));
    }

    private String parseAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private Date newTokenValidity() {
        long tokenDurationAsMillis = tokenValidity.toMillis();
        long now = new Date().getTime();

        return new Date(now + tokenDurationAsMillis);
    }

    private Optional<Claims> parseToken(String rawToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(rawToken)
                    .getBody();

            return Optional.of(claims);
        } catch (SignatureException e) {
            return Optional.empty();
        }
    }

    private Collection<? extends GrantedAuthority> parseAuthorities(Claims claims) {
        String[] rawAuthorities = claims.get(AUTHORITIES_KEY).toString().split(",");

        return Arrays.stream(rawAuthorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
