package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.domain.User;
import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.service.UserService;
import com.pdereg.timelogger.web.rest.errors.AccountNotFoundException;
import com.pdereg.timelogger.web.rest.errors.UsernameInUseException;
import com.pdereg.timelogger.web.rest.model.CreateAccountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for user accounts.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private UserService userService;

    @Autowired
    public AccountResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new {@link User} account.
     *
     * @param request HTTP request body which contains data for user creation
     * @return Newly created {@link User} instance
     */
    @PostMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Secured(Authorities.ADMIN)
    @Async
    public CompletableFuture<HttpEntity<User>> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        CompletableFuture<Optional<User>> future = userService.findOneByUsername(username);
        return future.thenCompose(userOptional -> {
            if (userOptional.isPresent()) {
                throw new UsernameInUseException();
            }

            return userService.createUser(username, password)
                    .thenApply(this::createAccountResponse);
        });
    }

    /**
     * Fetches and returns all {@link User} instance.
     *
     * @return A list of all {@link User} instances
     */
    @GetMapping("/accounts")
    @Secured(Authorities.ADMIN)
    @Async
    public CompletableFuture<List<User>> getAllAccounts() {
        return userService.findAll();
    }

    /**
     * Fetches and returns a {@link User} instance for a given {@code username}.
     *
     * @param username Name of the user to fetch
     * @return {@link User} instance for a given {@code username}
     */
    @GetMapping("/accounts/{username:" + User.USERNAME_PATTERN + "}")
    @Async
    public CompletableFuture<User> getAccount(@PathVariable String username) {
        return userService.findOneByUsername(username)
                .thenApply(user -> {
                    if (!user.isPresent()) {
                        throw new AccountNotFoundException();
                    }

                    return user.get();
                });
    }

    /**
     * Deletes an existing {@link User} instance for a given {@code username}.
     *
     * @param username Name of the user to delete
     */
    @DeleteMapping("/accounts/{username:" + User.USERNAME_PATTERN + "}")
    @Secured(Authorities.ADMIN)
    @Async
    public CompletableFuture<Void> deleteAccount(@PathVariable String username) {
        CompletableFuture<Optional<User>> future = userService.findOneByUsername(username);
        return future.thenCompose(userOptional -> {
            if (!userOptional.isPresent()) {
                throw new AccountNotFoundException();
            }

            return userService.deleteUser(username);
        });
    }

    private ResponseEntity<User> createAccountResponse(User user) {
        URI accountUri = createAccountUri(user);

        return ResponseEntity
                .created(accountUri)
                .body(user);
    }

    private URI createAccountUri(User user) {
        try {
            return new URI("/api/accounts/" + user.getUsername());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
