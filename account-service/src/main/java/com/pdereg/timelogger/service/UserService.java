package com.pdereg.timelogger.service;

import com.pdereg.timelogger.domain.User;
import com.pdereg.timelogger.repository.UserRepository;
import com.pdereg.timelogger.security.Authorities;
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

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

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
        User newUser = new User();
        newUser.setUsername(username);
        addInitialAuthorities(newUser);
        encodeAndSetPassword(newUser, password);

        return CompletableFuture.supplyAsync(() -> userRepository.save(newUser));
    }

    /**
     * Fetches and returns all {@link User} instances from database.
     *
     * @return A list of all {@link User} instances
     */
    public CompletableFuture<List<User>> findAll() {
        return CompletableFuture.supplyAsync(() -> userRepository.findAll());
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
        CompletableFuture<Optional<User>> future = findOneByUsername(username);

        return future.thenAccept(userOptional ->
                userOptional.ifPresent(user -> {
                    encodeAndSetPassword(user, password);
                    userRepository.save(user);
                })
        );
    }

    /**
     * Deletes user of provided {@code username} from the database.
     *
     * @param username Name of the user to delete
     */
    public CompletableFuture<Void> deleteUser(String username) {
        CompletableFuture<Optional<User>> future = findOneByUsername(username);

        return future.thenAccept(userOptional ->
                userOptional.ifPresent(userRepository::delete)
        );
    }

    private void addInitialAuthorities(User user) {
        GrantedAuthority userAuthority = new SimpleGrantedAuthority(Authorities.USER);
        user.addAuthority(userAuthority);
    }

    private void encodeAndSetPassword(User user, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
    }
}
