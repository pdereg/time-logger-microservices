package com.pdereg.timelogger.service;

import com.pdereg.timelogger.Application;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.domain.User;
import com.pdereg.timelogger.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.pdereg.timelogger.TestUtils.generateRandomPassword;
import static com.pdereg.timelogger.TestUtils.generateRandomUsername;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class UserServiceIntTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        mongoTemplate.dropCollection(User.class);
    }

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(User.class);
    }

    @Test
    public void createUser_savesNewUserInRepository() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();

        User newUser = userService.createUser(username, password).get();
        Optional<User> fetchedUser = userRepository.findOneByUsername(username);

        assertTrue(fetchedUser.isPresent());
        assertEquals(newUser, fetchedUser.get());
    }

    @Test
    public void createUser_encodesUserPassword() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();

        User newUser = userService.createUser(username, password).get();
        String savedPassword = newUser.getPassword();

        assertNotEquals(password, savedPassword);
        assertTrue(passwordEncoder.matches(password, savedPassword));
    }

    @Test
    public void createUser_addsInitialAuthorities() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();

        User newUser = userService.createUser(username, password).get();
        Collection<?> authorities = newUser.getAuthorities();

        assertFalse(authorities.isEmpty());
    }

    @Test
    public void findAll_returnsAllUsersFromRepository() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        userService.createUser(username, password).get();

        List<User> users = userService.findAll().get();
        List<User> fetchedUsers = userRepository.findAll();

        assertEquals(fetchedUsers, users);
    }

    @Test
    public void findOneByUsername_returnsCorrectUserIfExists() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();

        User user = userService.createUser(username, password).get();
        Optional<User> fetchedUser = userService.findOneByUsername(username).get();

        assertTrue(fetchedUser.isPresent());
        assertEquals(user, fetchedUser.get());
    }

    @Test
    public void changePassword_savesNewPasswordInRepository() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        User newUser = userService.createUser(username, password).get();

        String newPassword = generateRandomPassword();
        userService.changePassword(username, newPassword).get();

        Optional<User> fetchedUser = userRepository.findOneByUsername(username);
        assertTrue(fetchedUser.isPresent());

        String encodedOldPassword = newUser.getPassword();
        String encodedNewPassword = fetchedUser.get().getPassword();
        assertNotEquals(encodedOldPassword, encodedNewPassword);
    }

    @Test
    public void changePassword_encodesNewUserPassword() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        userService.createUser(username, password).get();

        String newPassword = generateRandomPassword();
        userService.changePassword(username, newPassword).get();

        Optional<User> fetchedUser = userRepository.findOneByUsername(username);
        assertTrue(fetchedUser.isPresent());

        String encodedNewPassword = fetchedUser.get().getPassword();
        assertTrue(passwordEncoder.matches(newPassword, encodedNewPassword));
    }

    @Test
    public void checkPassword_returnsTrueIfProvidedPasswordIsCorrect() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        User newUser = userService.createUser(username, password).get();

        assertTrue(userService.checkPassword(newUser, password));
    }

    @Test
    public void checkPassword_returnsFalseIfProvidedPasswordIsIncorrect() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        User newUser = userService.createUser(username, password).get();

        String incorrectPassword = generateRandomPassword();
        assertFalse(userService.checkPassword(newUser, incorrectPassword));
    }

    @Test
    public void deleteUser_removesUserFromRepository() throws Exception {
        String username = generateRandomUsername();
        String password = generateRandomPassword();
        userService.changePassword(username, password).get();

        userService.deleteUser(username).get();
        Optional<User> fetchedUser = userRepository.findOneByUsername(username);

        assertFalse(fetchedUser.isPresent());
    }
}
