package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.TestUtils;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.security.jwt.JwtHandler;
import com.pdereg.timelogger.service.AccountService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class TokenResourceIntTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtHandler jwtHandler;

    @MockBean
    private AccountService accountService;

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
    public void getToken_returnsOkIfAllCorrect() throws Exception {
        String username = "test";
        String password = "test";

        String basicToken = TestUtils.createBasicToken(username, password);
        Authentication authentication = createAuthentication(username);

        mockAccountService(authentication);

        MvcResult result = restAccountMockMvc.perform(
                get("/api/token")
                        .header("Authorization", "Basic " + basicToken))
                .andReturn();

        String expectedToken = jwtHandler.createToken(authentication);

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));
    }

    @Test
    public void getToken_returnsClientErrorIfTokenIsMalformed() throws Exception {
        String basicToken = TestUtils.generateRandomString(10);

        restAccountMockMvc.perform(
                get("/api/token")
                        .header("Authorization", "Basic " + basicToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getToken_returnsClientErrorIfExceptionIsThrownFromTheClient() throws Exception {
        String basicToken = TestUtils.createBasicToken("test", "test");
        mockAccountService(new RuntimeException());

        MvcResult result = restAccountMockMvc.perform(
                get("/api/token")
                        .header("Authorization", "Basic " + basicToken))
                .andReturn();

        restAccountMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isUnauthorized());
    }

    private Authentication createAuthentication(String username) {
        User user = new User(username, "", Collections.emptySet());
        return new UsernamePasswordAuthenticationToken(user, "", Collections.emptySet());
    }

    private void mockAccountService(Authentication authentication) {
        CompletableFuture<Authentication> response = CompletableFuture.completedFuture(authentication);

        when(accountService.authenticate(anyString(), anyString()))
                .thenReturn(response);
    }

    private void mockAccountService(RuntimeException throwable) {
        CompletableFuture<Authentication> response = CompletableFuture.supplyAsync(() -> {
            throw throwable;
        });

        when(accountService.authenticate(anyString(), anyString()))
                .thenReturn(response);
    }
}
