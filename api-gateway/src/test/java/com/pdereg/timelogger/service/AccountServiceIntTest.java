package com.pdereg.timelogger.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pdereg.timelogger.Application;
import com.pdereg.timelogger.TestUtils;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.security.Authorities;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class AccountServiceIntTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().bindAddress("localhost").port(8081));

    @Autowired
    @Qualifier("authorization")
    private String authorizationToken;

    @Autowired
    private AccountService accountService;

    @Test
    public void authenticate_returnsCorrectSetOfAuthorities() throws Exception {
        final Set<String> authorities = Collections.singleton(Authorities.USER);
        mockAccountService(authorities, 200);

        final Authentication authentication = accountService.authenticate("test", "test").get();
        final Set<String> fetchedAuthorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::toString)
                .collect(Collectors.toSet());

        assertTrue(fetchedAuthorities.containsAll(authorities));
    }

    @Test(expected = ExecutionException.class)
    public void authenticate_throwsExceptionIfCredentialsAreIncorrect() throws Exception {
        final Set<String> authorities = Collections.emptySet();
        mockAccountService(authorities, 400);

        accountService.authenticate("test", "test").get();
    }

    private void mockAccountService(Set<String> authorities, int status) {
        final byte[] body = TestUtils.toJson(authorities);

        stubFor(get(urlMatching("/api/accounts/([a-zA-Z0-9]+)/authenticate\\?password=(.+)"))
                .withHeader("Authorization", equalTo(authorizationToken))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }
}
