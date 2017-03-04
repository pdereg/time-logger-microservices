package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.domain.Activity;
import com.pdereg.timelogger.security.Authorities;
import com.pdereg.timelogger.service.ActivityService;
import com.pdereg.timelogger.web.rest.model.CreateActivityRequest;
import com.pdereg.timelogger.web.rest.model.UpdateActivityRequest;
import org.junit.After;
import org.junit.Before;
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

import static com.pdereg.timelogger.TestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class ActivityResourceIntTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ActivityService activityService;

    private MockMvc restActivityMockMvc;

    @Before
    public void setUp() {
        mongoTemplate.dropCollection(Activity.class);
        initializeMockMvc();
    }

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(Activity.class);
    }

    private void initializeMockMvc() {
        this.restActivityMockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void createActivity_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        byte[] requestBody = createActivityRequest(name, requiredDuration, weekdays);

        MvcResult result = restActivityMockMvc.perform(
                post("/api/activities")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(name)))
                .andExpect(jsonPath("$.accountId").value(equalTo(accountId)))
                .andExpect(jsonPath("$.name").value(equalTo(name)))
                .andExpect(jsonPath("$.requiredDuration").value(requiredDuration))
                .andExpect(jsonPath("$.weekdays").isArray());
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void createActivity_returnsClientErrorForEmptyBody() throws Exception {
        restActivityMockMvc.perform(
                post("/api/activities")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void createActivity_returnsClientErrorIfActivityExists() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        byte[] requestBody = createActivityRequest(name, requiredDuration, weekdays);
        MvcResult result = restActivityMockMvc.perform(
                post("/api/activities")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void getAllActivities_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String otherAccountId = "user2";
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();

        createActivity(accountId, name, requiredDuration, weekdays);
        createActivity(otherAccountId, name, requiredDuration, weekdays);

        MvcResult result = restActivityMockMvc.perform(
                get("/api/activities"))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountId", equalTo(accountId)));
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void getAllActivitiesForAccount_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        MvcResult result = restActivityMockMvc.perform(
                get("/api/activities/{username}", accountId))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountId", equalTo(accountId)));
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void getAllActivitiesForAccount_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        restActivityMockMvc.perform(
                get("/api/activities/{username}", accountId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void getActivity_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        MvcResult result = restActivityMockMvc.perform(
                get("/api/activities/{username}/{name}", accountId, name)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(equalTo(accountId)))
                .andExpect(jsonPath("$.name").value(equalTo(name)))
                .andExpect(jsonPath("$.requiredDuration").value(requiredDuration))
                .andExpect(jsonPath("$.weekdays").isArray());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void getActivity_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        restActivityMockMvc.perform(
                get("/api/activities/{username}/{name}", accountId, name)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void getActivity_returnsClientErrorForNonExistingActivity() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();

        MvcResult result = restActivityMockMvc.perform(
                get("/api/activities/{username}/{name}", accountId, name)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void update_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        long newRequiredDuration = requiredDuration * 2;

        boolean[] newWeekdays = new boolean[weekdays.length];
        for (int i = 0; i < weekdays.length; ++i) {
            newWeekdays[i] = !weekdays[i];
        }

        byte[] requestBody = updateActivityRequest(newRequiredDuration, newWeekdays);
        MvcResult result = restActivityMockMvc.perform(
                put("/api/activities/{username}/{name}", accountId, name)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(equalTo(accountId)))
                .andExpect(jsonPath("$.name").value(equalTo(name)))
                .andExpect(jsonPath("$.requiredDuration").value(newRequiredDuration));
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void deleteActivity_returnsOkIfAllCorrect() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        MvcResult result = restActivityMockMvc.perform(
                delete("/api/activities/{username}/{name}", accountId, name))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyString()));
    }

    @Test
    @WithMockUser(authorities = Authorities.USER)
    public void deleteActivity_returnsClientErrorIfUserIsNotOwner() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        createActivity(accountId, name, requiredDuration, weekdays);

        restActivityMockMvc.perform(
                delete("/api/activities/{username}/{name}", accountId, name))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = Authorities.USER)
    public void deleteActivity_returnsClientErrorForNonExistingActivity() throws Exception {
        String accountId = "user";
        String name = generateRandomActivityName();

        MvcResult result = restActivityMockMvc.perform(
                delete("/api/activities/{username}/{name}", accountId, name))
                .andReturn();

        restActivityMockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound());
    }

    private byte[] createActivityRequest(String name, long requiredDuration, boolean[] weekdays) {
        CreateActivityRequest createActivityRequest = new CreateActivityRequest();
        createActivityRequest.setName(name);
        createActivityRequest.setRequiredDuration(requiredDuration);
        createActivityRequest.setWeekdays(weekdays);
        return toJson(createActivityRequest);
    }

    private void createActivity(String accountId, String name, long requiredDuration, boolean[] weekdays)
            throws Exception {

        activityService.createActivity(accountId, name, requiredDuration, weekdays).get();
    }

    private byte[] updateActivityRequest(long requiredDuration, boolean[] weekdays) {
        UpdateActivityRequest updateActivityRequest = new UpdateActivityRequest();
        updateActivityRequest.setRequiredDuration(requiredDuration);
        updateActivityRequest.setWeekdays(weekdays);
        return toJson(updateActivityRequest);
    }
}
