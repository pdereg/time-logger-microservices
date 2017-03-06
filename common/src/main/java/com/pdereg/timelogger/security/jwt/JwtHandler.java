package com.pdereg.timelogger.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

    static final String AUTHORITIES_KEY = "auth";

    private final SecretKey secretKey;
    private final Duration tokenValidity;
    private final String issuer;
    private final String audience;

    public JwtHandler(SecretKey secretKey, Duration tokenValidity, String issuer, String audience) {
        this.secretKey = secretKey;
        this.tokenValidity = tokenValidity;
        this.issuer = issuer;
        this.audience = audience;
    }

    /**
     * Creates a new JWT for provided {@code authentication} using current system time.
     *
     * @see JwtHandler#createToken(Authentication, Date)
     */
    public String createToken(Authentication authentication) {
        final Date now = new Date();
        return createToken(authentication, now);
    }

    /**
     * Creates a new JWT for provided {@code authentication}.
     *
     * @param authentication {@link Authentication} instance for which to create a new JWT
     * @return Newly created JSON Web Token
     */
    public String createToken(Authentication authentication, Date issuedAt) {
        final String username = authentication.getName();

        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        final String parsedAuthorities = parseAuthorities(authorities);

        final Date validity = newTokenValidity(issuedAt);

        return Jwts.builder()
                .setIssuer(issuer)
                .setIssuedAt(issuedAt)
                .setExpiration(validity)
                .setSubject(username)
                .setAudience(audience)
                .claim(AUTHORITIES_KEY, parsedAuthorities)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * Validates provided {@code rawToken}.
     *
     * @param rawToken Raw JWT to validate
     * @return An {@link Authentication} instance if validation is successful
     */
    public Optional<Authentication> validateToken(String rawToken) {
        final Optional<Claims> claims = parseToken(rawToken);
        if (!claims.isPresent()) {
            return Optional.empty();
        }

        final String username = claims.get().getSubject();
        final Collection<? extends GrantedAuthority> authorities = parseAuthorities(claims.get());

        final User user = new User(username, "", authorities);
        return Optional.of(new UsernamePasswordAuthenticationToken(user, "", authorities));
    }

    private String parseAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private Date newTokenValidity(Date from) {
        final long tokenDurationAsMillis = tokenValidity.toMillis();
        final long fromMillis = from.getTime();

        return new Date(fromMillis + tokenDurationAsMillis);
    }

    private Optional<Claims> parseToken(String rawToken) {
        try {
            final Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(rawToken)
                    .getBody();

            return Optional.of(claims);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    private Collection<? extends GrantedAuthority> parseAuthorities(Claims claims) {
        final String[] rawAuthorities = claims.get(AUTHORITIES_KEY).toString().split(",");

        return Arrays.stream(rawAuthorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
