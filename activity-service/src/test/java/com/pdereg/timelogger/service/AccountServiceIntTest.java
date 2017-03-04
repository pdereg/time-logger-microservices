package com.pdereg.timelogger.service;

import com.pdereg.timelogger.Application;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.domain.Activity;
import com.pdereg.timelogger.repository.ActivityRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static com.pdereg.timelogger.TestUtils.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class AccountServiceIntTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityService activityService;

    @Before
    public void setUp() {
        mongoTemplate.dropCollection(Activity.class);
    }

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(Activity.class);
    }

    @Test
    public void createActivity_savesNewActivityInRepository() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();

        Activity newActivity = activityService.createActivity(accountId, name, requiredDuration, weekdays).get();
        Optional<Activity> fetchedActivity = activityRepository.findOneByAccountIdAndName(accountId, name);

        assertTrue(fetchedActivity.isPresent());
        assertEquals(newActivity, fetchedActivity.get());
    }

    @Test
    public void createActivity_setsRequiredDurationCorrectly() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();

        Activity newActivity = activityService.createActivity(accountId, name, requiredDuration, weekdays).get();

        assertEquals(requiredDuration, newActivity.getRequiredDuration());
    }

    @Test
    public void createActivity_setsWeekdaysCorrectly() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();

        Activity newActivity = activityService.createActivity(accountId, name, requiredDuration, weekdays).get();

        assertArrayEquals(weekdays, newActivity.getWeekdays());
    }

    @Test(expected = Exception.class)
    public void createActivity_throwsExceptionIfActivityAlreadyExists() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();

        for (int i = 0; i < 2; ++i) {
            activityService.createActivity(accountId, name, requiredDuration, weekdays).get();
        }
    }

    @Test
    public void findAll_returnsAllActivitiesFromRepository() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        activityService.createActivity(accountId, name, requiredDuration, weekdays).get();

        List<Activity> activities = activityService.findAll().get();
        List<Activity> fetchedActivities = activityRepository.findAll();

        assertEquals(fetchedActivities, activities);
    }

    @Test
    public void findAllByAccountId_returnsAllActivitiesByAccountIdFromRepository() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        activityService.createActivity(accountId, name, requiredDuration, weekdays).get();

        List<Activity> activities = activityService.findAllByAccountId(accountId).get();
        List<Activity> fetchedActivities = activityRepository.findAllByAccountId(accountId);

        assertEquals(fetchedActivities, activities);
    }

    @Test
    public void findOneByAccountIdAndName_returnsCorrectActivityIfExists() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();

        Activity activity = activityService.createActivity(accountId, name, requiredDuration, weekdays).get();
        Optional<Activity> fetchedActivity = activityService.findOneByAccountIdAndName(accountId, name).get();

        assertTrue(fetchedActivity.isPresent());
        assertEquals(activity, fetchedActivity.get());
    }

    @Test
    public void deleteActivity_removesActivityFromRepository() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        long requiredDuration = generateActivityDuration();
        boolean[] weekdays = generateActivityWeekdays();
        activityService.createActivity(accountId, name, requiredDuration, weekdays).get();

        activityService.deleteActivity(accountId, name).get();
        Optional<Activity> fetchedActivity = activityRepository.findOneByAccountIdAndName(accountId, name);

        assertFalse(fetchedActivity.isPresent());
    }

    @Test(expected = Exception.class)
    public void deleteActivity_throwsExceptionIfActivityDoesNotExist() throws Exception {
        String accountId = generateRandomString(10);
        String name = generateRandomActivityName();
        activityService.deleteActivity(accountId, name).get();
    }
}
