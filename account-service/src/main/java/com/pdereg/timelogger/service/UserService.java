package com.pdereg.timelogger.service;

import com.pdereg.timelogger.domain.User;
import com.pdereg.timelogger.repository.UserRepository;
import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.service.error.UserNotFoundException;
import com.pdereg.timelogger.service.error.UsernameInUseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Provides an abstraction layer over {@link UserRepository}. Used for performing CRUD operations on {@link User}
 * instances.
 */
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Creates and returns a new {@link User} instance.
     *
     * @param username Name of the user to create
     * @param password User's plain password. Note that it will be hashed first before saving.
     * @return New {@link User} instance
     */
    public CompletableFuture<User> createUser(String username, String password) {
        final User user = new User();
        user.setUsername(username);
        addInitialAuthorities(user);
        encodeAndSetPassword(user, password);

        return findOneByUsername(username)
                .thenAccept(userOptional -> {
                    if (userOptional.isPresent()) {
                        throw new UsernameInUseException();
                    }
                })
                .thenComposeAsync(unit -> CompletableFuture.supplyAsync(() -> userRepository.save(user)));
    }

    /**
     * Fetches and returns all {@link User} instances from database.
     *
     * @return A list of all {@link User} instances
     */
    public CompletableFuture<List<User>> findAll() {
        return CompletableFuture.supplyAsync(userRepository::findAll);
    }

    /**
     * Fetches and returns a {@link User} instance for a given {@code username}.
     *
     * @param username Name of the user to fetch
     * @return Optional {@link User} instance for a given {@code username}
     */
    public CompletableFuture<Optional<User>> findOneByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> userRepository.findOneByUsername(username));
    }

    /**
     * Changes password for a user of given {@code username}.
     *
     * @param username Name of the user for whom to change the password
     * @param password New plain password. Note that it will be hashed first before saving.
     */
    public CompletableFuture<Void> changePassword(String username, String password) {
        return findOneByUsername(username)
                .thenAccept(userOptional -> userOptional.ifPresent(user -> {
                            encodeAndSetPassword(user, password);
                            userRepository.save(user);
                        })
                );
    }

    /**
     * Checks provided {@code password} for the given {@code user}.
     *
     * @param username Name of the user for whom to check provided {@code password}
     * @param password Raw password to check
     * @return {@code true} if password is correct; {@code false} otherwise
     */
    public CompletableFuture<Boolean> checkPassword(String username, String password) {
        return findOneByUsername(username)
                .thenApply(user -> user.filter(
                        userOptional -> passwordEncoder.matches(password, userOptional.getPassword())).isPresent()
                );
    }

    /**
     * Deletes user of provided {@code username} from the database.
     *
     * @param username Name of the user to delete
     */
    public CompletableFuture<Void> deleteUser(String username) {
        return findOneByUsername(username)
                .thenApply(user -> user.<UserNotFoundException>orElseThrow(UserNotFoundException::new))
                .thenAccept(userRepository::delete);
    }

    private void addInitialAuthorities(User user) {
        final GrantedAuthority userAuthority = new SimpleGrantedAuthority(Authorities.USER);
        user.addAuthority(userAuthority);
    }

    private void encodeAndSetPassword(User user, String password) {
        final String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
    }
}
