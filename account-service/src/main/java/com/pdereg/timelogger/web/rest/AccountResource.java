package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.domain.User;
import com.pdereg.timelogger.security.annotations.AdminOrAccountOwnerRequired;
import com.pdereg.timelogger.security.annotations.AdminRequired;
import com.pdereg.timelogger.security.annotations.GatewayRequired;
import com.pdereg.timelogger.service.UserService;
import com.pdereg.timelogger.web.rest.errors.AccountNotFoundException;
import com.pdereg.timelogger.web.rest.errors.CreateAccountRequest;
import com.pdereg.timelogger.web.rest.errors.InvalidCredentialsException;
import com.pdereg.timelogger.web.rest.errors.UsernameInUseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for user accounts.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final UserService userService;

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
    @PostMapping("/accounts")
    @AdminRequired
    public CompletableFuture<HttpEntity<User>> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        final String username = request.getUsername();
        final String password = request.getPassword();

        return userService
                .findOneByUsername(username)
                .thenAccept(userOptional -> {
                    if (userOptional.isPresent()) {
                        throw new UsernameInUseException();
                    }
                })
                .thenComposeAsync(unit -> userService.createUser(username, password))
                .thenApply(this::createAccountResponse);
    }

    /**
     * Fetches and returns all {@link User} instance.
     *
     * @return A list of all {@link User} instances
     */
    @GetMapping("/accounts")
    @AdminRequired
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
    @AdminOrAccountOwnerRequired
    public CompletableFuture<User> getAccount(@PathVariable String username) {
        return userService
                .findOneByUsername(username)
                .thenApply(user -> user.<AccountNotFoundException>orElseThrow(AccountNotFoundException::new));
    }

    /**
     * Authenticates user of provided {@code username}. For internal use only.
     *
     * @param username Name of the user to authenticate
     * @param password User's raw password
     * @return A set of user's authorities upon successful authentication or error
     */
    @GetMapping("/accounts/{username:" + User.USERNAME_PATTERN + "}/authenticate")
    @GatewayRequired
    public CompletableFuture<Set<String>> authenticate(@PathVariable String username, @RequestParam String password) {
        return userService
                .findOneByUsername(username)
                .thenApply(user -> user.<InvalidCredentialsException>orElseThrow(InvalidCredentialsException::new))
                .thenApply(user -> {
                    if (userService.checkPassword(user, password)) {
                        return user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toSet());
                    }

                    throw new InvalidCredentialsException();
                });
    }

    /**
     * Deletes an existing {@link User} instance for a given {@code username}.
     *
     * @param username Name of the user to delete
     */
    @DeleteMapping("/accounts/{username:" + User.USERNAME_PATTERN + "}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<Void> deleteAccount(@PathVariable String username) {
        return userService
                .findOneByUsername(username)
                .thenApply(user -> user.<AccountNotFoundException>orElseThrow(AccountNotFoundException::new))
                .thenApply(User::getUsername)
                .thenComposeAsync(userService::deleteUser);
    }

    private ResponseEntity<User> createAccountResponse(User user) {
        final URI accountUri = createAccountUri(user);

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
