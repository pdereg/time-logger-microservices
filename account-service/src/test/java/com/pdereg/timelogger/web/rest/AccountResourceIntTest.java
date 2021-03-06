package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.TestUtils;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.service.UserService;
import com.pdereg.timelogger.web.rest.model.CreateAccountRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class AccountResourceIntTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    private MockMvc restAccountMockMvc;

    @Before
    public void setUp() {
        initializeRestAccountMockMvc();
    }

    private void initializeRestAccountMockMvc() {
        this.restAccountMockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = {Authorities.USER, Authorities.ADMIN})
    public void createAccount_returnsOkIfAllCorrect() throws Exception {
        String username = TestUtils.generateRandomUsername();
        byte[] requestBody = createAccountRequest(username);

        MvcResult result = restAccountMockMvc.perform(
                post("/api/accounts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(username)))
                .andExpect(jsonPath("$.username").value(equalTo(username)));
    }

    @Test
    @WithMockUser(authorities = {Authorities.USER, Authorities.ADMIN})
    public void createAccount_returnsClientErrorForEmptyBody() throws Exception {
        restAccountMockMvc.perform(
                post("/api/accounts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {Authorities.USER, Authorities.ADMIN})
    public void createAccount_returnsClientErrorIfAccountExists() throws Exception {
        String username = TestUtils.generateRandomUsername();
        createAccount(username);

        byte[] requestBody = createAccountRequest(username);
        MvcResult result = restAccountMockMvc.perform(
                post("/api/accounts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void createAccount_returnsClientErrorIfUserIsNotAdmin() throws Exception {
        String username = TestUtils.generateRandomUsername();
        createAccount(username);

        byte[] requestBody = createAccountRequest(username);
        restAccountMockMvc.perform(
                post("/api/accounts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(content().string(equalTo("")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {Authorities.USER, Authorities.ADMIN})
    public void getAllAccounts_returnsOkIfAllCorrect() throws Exception {
        String username1 = TestUtils.generateRandomUsername();
        createAccount(username1);

        String username2 = TestUtils.generateRandomUsername();
        createAccount(username2);

        MvcResult result = restAccountMockMvc.perform(
                get("/api/accounts"))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder(username1, username2)));
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void getAllAccounts_returnsClientErrorIfUserIsNotAdmin() throws Exception {
        restAccountMockMvc.perform(get("/api/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void getAccount_returnsOkIfAllCorrect() throws Exception {
        String username = "user";
        createAccount(username);

        MvcResult result = restAccountMockMvc.perform(
                get("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", equalTo(username)));
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void getAccount_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String username = TestUtils.generateRandomUsername();
        createAccount(username);

        restAccountMockMvc.perform(
                get("/api/accounts/{username}", username))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {Authorities.USER, Authorities.ADMIN})
    public void getAccount_returnsClientErrorForNonExistingUser() throws Exception {
        String username = TestUtils.generateRandomUsername();

        MvcResult result = restAccountMockMvc.perform(
                get("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAccount_returnsClientErrorIfUserIsNotAuthorized() throws Exception {
        String username = TestUtils.generateRandomUsername();

        restAccountMockMvc.perform(
                get("/api/accounts/{username}", username))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", authorities = {Authorities.USER, Authorities.GATEWAY})
    public void authenticate_returnsOkIfAllCorrect() throws Exception {
        String username = TestUtils.generateRandomUsername();
        String password = TestUtils.generateRandomPassword();
        createAccount(username, password);

        MvcResult result = restAccountMockMvc.perform(
                get("/api/accounts/{username}/authenticate?password={password}", username, password))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(containsInAnyOrder(Authorities.USER)));
    }

    @Test
    @WithMockUser(username = "user", authorities = {Authorities.USER, Authorities.GATEWAY})
    public void authenticate_returnsClientErrorIfPasswordIsIncorrect() throws Exception {
        String username = TestUtils.generateRandomUsername();
        createAccount(username);

        String incorrectPassword = TestUtils.generateRandomPassword();

        MvcResult result = restAccountMockMvc.perform(
                get("/api/accounts/{username}/authenticate?password={password}", username, incorrectPassword))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", authorities = {Authorities.USER, Authorities.ADMIN})
    public void authenticate_returnsClientErrorIfUserIsNotGateway() throws Exception {
        String username = TestUtils.generateRandomUsername();
        String password = TestUtils.generateRandomPassword();
        createAccount(username, password);

        restAccountMockMvc.perform(
                get("/api/accounts/{username}/authenticate?password={password}", username, password))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void deleteAccount_returnsOkIfAllCorrect() throws Exception {
        String username = "user";
        createAccount(username);

        MvcResult result = restAccountMockMvc.perform(
                delete("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @WithMockUser(authorities = {Authorities.USER, Authorities.ADMIN})
    public void deleteAccount_returnsClientErrorForNonExistingUser() throws Exception {
        String username = TestUtils.generateRandomUsername();

        MvcResult result = restAccountMockMvc.perform(
                delete("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void deleteAccount_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String username = TestUtils.generateRandomUsername();

        restAccountMockMvc.perform(
                delete("/api/accounts/{username}", username))
                .andExpect(status().isForbidden());
    }

    private byte[] createAccountRequest(String username) {
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setUsername(username);

        String password = TestUtils.generateRandomPassword();
        createAccountRequest.setPassword(password);

        return TestUtils.toJson(createAccountRequest);
    }

    private void createAccount(String username) {
        String password = TestUtils.generateRandomPassword();
        createAccount(username, password);
    }

    private void createAccount(String username, String password) {
        try {
            userService.createUser(username, password).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
