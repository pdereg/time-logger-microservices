package com.pdereg.timelogger.web.rest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.domain.Log;
import com.pdereg.timelogger.network.model.Activity;
import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.service.ActivityService;
import com.pdereg.timelogger.service.LogService;
import com.pdereg.timelogger.web.web.rest.model.CreateLogRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private MongoTemplate mongoTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LogService logService;

    private MockMvc logRestMockMvc;

    @Before
    public void setUp() {
        mongoTemplate.dropCollection(Log.class);
        initializeMockMvc();
    }

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(Log.class);
    }

    private void initializeMockMvc() {
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

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void findAllByAccountId_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        createLog(accountId, activityId, duration);

        MvcResult result = logRestMockMvc.perform(
                get("/api/logs/{username}", accountId))
                .andReturn();

        logRestMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountId", equalTo(accountId)))
                .andExpect(jsonPath("$[0].activityId", equalTo(activityId)));
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void findAllByAccountId_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        createLog(accountId, activityId, duration);

        logRestMockMvc.perform(
                get("/api/logs/{username}", accountId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void findAllByAccountIdAndActivityId_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String activityId = generateRandomString(10);
        String otherActivityId = generateRandomString(10);
        long duration = generateDuration();

        createLog(accountId, activityId, duration);
        createLog(accountId, otherActivityId, duration);

        MvcResult result = logRestMockMvc.perform(
                get("/api/logs/{username}/{activityId}", accountId, activityId))
                .andReturn();

        logRestMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountId", equalTo(accountId)))
                .andExpect(jsonPath("$[0].activityId", equalTo(activityId)));
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void findAllByAccountIdAndActivityId_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        createLog(accountId, activityId, duration);

        logRestMockMvc.perform(
                get("/api/logs/{username}/{activityId}", accountId, activityId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void findOneById_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String activityId = generateRandomString(10);
        long duration = generateDuration();

        Log log = createLog(accountId, activityId, duration);
        String id = log.getId();

        MvcResult result = logRestMockMvc.perform(
                get("/api/logs/{username}/{activityId}/{id}", accountId, activityId, id))
                .andReturn();

        logRestMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", equalTo(accountId)))
                .andExpect(jsonPath("$.activityId", equalTo(activityId)))
                .andExpect(jsonPath("$.id", equalTo(id)));
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void findOneById_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();

        Log log = createLog(accountId, activityId, duration);
        String id = log.getId();

        logRestMockMvc.perform(
                get("/api/logs/{username}/{activityId}/{id}", accountId, activityId, id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void findOneById_returnsClientErrorIfLogDoesNotExist() throws Exception {
        String accountId = "user";
        String activityId = generateRandomString(10);
        String id = generateRandomString(10);

        MvcResult result = logRestMockMvc.perform(
                get("/api/logs/{username}/{activityId}/{id}", accountId, activityId, id))
                .andReturn();

        logRestMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    private void mockActivityService(String accountId, String activityName, int status) {
        Activity activity = createActivity(accountId, activityName);
        byte[] body = toJson(activity);

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlMatching("/api/activities/([a-zA-Z0-9]+)/([a-zA-Z0-9]+)"))
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

    private Log createLog(String accountId, String activityId, long duration) throws Exception {
        return logService.createLog(accountId, activityId, duration).get();
    }
}
