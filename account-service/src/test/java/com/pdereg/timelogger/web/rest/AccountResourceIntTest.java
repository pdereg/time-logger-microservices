package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.TestUtils;
import com.pdereg.timelogger.service.UserService;
import com.pdereg.timelogger.web.rest.model.CreateAccountRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AccountResourceIntTest {

    @Autowired
    private UserService userService;

    private MockMvc restAccountMockMvc;

    @Before
    public void setUp() {
        initializeRestAccountMockMvc();
    }

    private void initializeRestAccountMockMvc() {
        AccountResource accountResource = new AccountResource(userService);

        this.restAccountMockMvc = MockMvcBuilders
                .standaloneSetup(accountResource)
                .build();
    }

    @Test
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
    public void createAccount_returnsClientErrorForEmptyBody() throws Exception {
        restAccountMockMvc.perform(
                post("/api/accounts")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
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
    public void getAccount_returnsOkIfAllCorrect() throws Exception {
        String username = TestUtils.generateRandomUsername();
        createAccount(username);

        MvcResult result = restAccountMockMvc.perform(
                get("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", equalTo(username)));
    }

    @Test
    public void getAccount_returnsClientErrorForNonExistingUser() throws Exception {
        String username = TestUtils.generateRandomUsername();

        MvcResult result = restAccountMockMvc.perform(
                get("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteAccount_returnsOkIfAllCorrect() throws Exception {
        String username = TestUtils.generateRandomUsername();
        createAccount(username);

        MvcResult result = restAccountMockMvc.perform(
                delete("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    public void deleteAccount_returnsClientErrorForNonExistingUser() throws Exception {
        String username = TestUtils.generateRandomUsername();

        MvcResult result = restAccountMockMvc.perform(
                delete("/api/accounts/{username}", username))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
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

        try {
            userService.createUser(username, password).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
