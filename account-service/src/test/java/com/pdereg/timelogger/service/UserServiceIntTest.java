package com.pdereg.timelogger.service;

import com.pdereg.timelogger.Application;
import com.pdereg.timelogger.domain.User;
import com.pdereg.timelogger.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Optional;

import static com.pdereg.timelogger.TestUtils.generateRandomPassword;
import static com.pdereg.timelogger.TestUtils.generateRandomUsername;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserServiceIntTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void createUser_savesNewUserInRepository() {
        String username = generateRandomUsername();
        String password = generateRandomPassword();

        User newUser = userService.createUser(username, password);
        Optional<User> fetchedUser = userRepository.findOneByUsername(username);

        assertTrue(fetchedUser.isPresent());
        assertEquals(newUser, fetchedUser.get());
    }

    @Test
    public void createUser_encodesUserPassword() {
        String username = generateRandomUsername();
        String password = generateRandomPassword();

        User newUser = userService.createUser(username, password);
        String savedPassword = newUser.getPassword();

        assertNotEquals(password, savedPassword);
        assertTrue(passwordEncoder.matches(password, savedPassword));
    }

    @Test
    public void createUser_addsInitialAuthorities() {
        String username = generateRandomUsername();
        String password = generateRandomPassword();

        User newUser = userService.createUser(username, password);
        Collection<?> authorities = newUser.getAuthorities();

        assertFalse(authorities.isEmpty());
    }

    @Test
    public void changePassword_savesNewPasswordInRepository() {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        User newUser = userService.createUser(username, password);

        String newPassword = generateRandomPassword();
        userService.changePassword(username, newPassword);

        Optional<User> fetchedUser = userRepository.findOneByUsername(username);
        assertTrue(fetchedUser.isPresent());

        String encodedOldPassword = newUser.getPassword();
        String encodedNewPassword = fetchedUser.get().getPassword();
        assertNotEquals(encodedOldPassword, encodedNewPassword);
    }

    @Test
    public void changePassword_encodesNewUserPassword() {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        userService.createUser(username, password);

        String newPassword = generateRandomPassword();
        userService.changePassword(username, newPassword);

        Optional<User> fetchedUser = userRepository.findOneByUsername(username);
        assertTrue(fetchedUser.isPresent());

        String encodedNewPassword = fetchedUser.get().getPassword();
        assertTrue(passwordEncoder.matches(newPassword, encodedNewPassword));
    }

    @Test
    public void deleteUser_removesUserFromRepository() {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        userService.changePassword(username, password);

        userService.deleteUser(username);
        Optional<User> fetchedUser = userRepository.findOneByUsername(username);

        assertFalse(fetchedUser.isPresent());
    }
}
