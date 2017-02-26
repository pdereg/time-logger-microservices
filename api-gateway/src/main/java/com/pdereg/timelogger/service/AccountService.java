package com.pdereg.timelogger.service;

import com.pdereg.timelogger.network.AccountClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Provides an abstraction layer over {@link AccountClient}. Used for authenticating users.
 */
@Service
public class AccountService {

    private final AccountClient accountClient;
    private final String gatewayAuthorizationHeader;

    @Autowired
    public AccountService(AccountClient accountClient, @Qualifier("authorization") String gatewayAuthorizationHeader) {
        this.accountClient = accountClient;
        this.gatewayAuthorizationHeader = gatewayAuthorizationHeader;
    }

    /**
     * Authenticates user with provided {@code username} and {@code password}.
     *
     * @param username Name of the user to authenticate
     * @param password User's password
     * @return An {@link Authentication} instance containing user's credentials or error
     */
    public CompletableFuture<Authentication> authenticate(String username, String password) {
        return CompletableFuture
                .supplyAsync(() -> accountClient.authenticate(gatewayAuthorizationHeader, username, password))
                .thenApply(authorities -> createAuthentication(username, authorities));
    }

    private Authentication createAuthentication(String username, Set<String> authorities) {
        final Collection<? extends GrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        final User user = new User(username, "", grantedAuthorities);
        return new UsernamePasswordAuthenticationToken(user, "", grantedAuthorities);
    }
}
