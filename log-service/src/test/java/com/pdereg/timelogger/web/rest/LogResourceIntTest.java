package com.pdereg.timelogger.web.rest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.network.model.Activity;
import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.service.ActivityService;
import com.pdereg.timelogger.service.LogService;
import com.pdereg.timelogger.web.web.rest.model.CreateLogRequest;
import org.junit.Before;
import org.junit.Rule;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.pdereg.timelogger.TestUtils.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class LogResourceIntTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().bindAddress("localhost").port(8083));

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LogService logService;

    private MockMvc logRestMockMvc;

    @Before
    public void setUp() {
        initializeRestLogMockMvc();
    }

    private void initializeRestLogMockMvc() {
        this.logRestMockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void createLog_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        byte[] requestBody = createLogRequest(activityId, duration);

        mockActivityService(accountId, activityId, 200);

        MvcResult result = logRestMockMvc.perform(
                post("/api/logs")
                        .header("Authorization", generateRandomAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        logRestMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(accountId)))
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(activityId)))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.activityId").value(activityId))
                .andExpect(jsonPath("$.duration").value(duration))
                .andExpect(jsonPath("$.startTime").isNumber());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void createLog_returnsClientErrorIfActivityDoesNotExist() throws Exception {
        String accountId = "user";
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        byte[] requestBody = createLogRequest(activityId, duration);

        mockActivityService(accountId, activityId, 404);

        MvcResult result = logRestMockMvc.perform(
                post("/api/logs")
                        .header("Authorization", generateRandomAuthHeader())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        logRestMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    private void mockActivityService(String accountId, String activityName, int status) {
        Activity activity = createActivity(accountId, activityName);
        byte[] body = toJson(activity);

        stubFor(get(urlMatching("/api/activities/([a-zA-Z0-9]+)/([a-zA-Z0-9]+)"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private Activity createActivity(String accountId, String activityName) {
        Activity activity = new Activity();
        activity.setAccountId(accountId);
        activity.setName(activityName);
        return activity;
    }

    private byte[] createLogRequest(String activityName, long duration) {
        CreateLogRequest createLogRequest = new CreateLogRequest();
        createLogRequest.setActivityName(activityName);
        createLogRequest.setDuration(duration);
        return toJson(createLogRequest);
    }
}
