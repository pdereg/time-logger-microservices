package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.security.jwt.JwtHandler;
import com.pdereg.timelogger.service.AccountService;
import com.pdereg.timelogger.utils.BasicTokenParser;
import com.pdereg.timelogger.web.rest.errors.InvalidCredentialsException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for authentication tokens.
 */
@RestController
@RequestMapping("/api")
public class TokenResource {

    private final AccountService accountService;
    private final JwtHandler jwtHandler;

    @Autowired
    public TokenResource(AccountService accountService, JwtHandler jwtHandler) {
        this.accountService = accountService;
        this.jwtHandler = jwtHandler;
    }

    /**
     * Creates and returns a new JSON Web Token for given basic authentication token.
     *
     * @param authorizationHeaderValue Header value of user-provided basic authentication token
     * @return JSON Web Token for provided {@code authorizationHeaderValue} or error
     */
    @GetMapping("/token")
    public CompletableFuture<String> getToken(@RequestHeader("Authorization") String authorizationHeaderValue) {
        final Optional<Pair<String, String>> authorizationOptional = BasicTokenParser.parse(authorizationHeaderValue);
        if (!authorizationOptional.isPresent()) {
            throw new InvalidCredentialsException();
        }

        final Pair<String, String> authorization = authorizationOptional.get();

        return accountService
                .authenticate(authorization.getLeft(), authorization.getRight())
                .exceptionally(throwable -> {
                    throw new InvalidCredentialsException();
                })
                .thenApply(jwtHandler::createToken);
    }
}
