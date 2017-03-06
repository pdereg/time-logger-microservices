package com.pdereg.timelogger.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pdereg.timelogger.Application;
import com.pdereg.timelogger.config.CommonConfiguration;
import com.pdereg.timelogger.domain.Log;
import com.pdereg.timelogger.repository.LogRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.pdereg.timelogger.TestUtils.generateDuration;
import static com.pdereg.timelogger.TestUtils.generateRandomString;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {CommonConfiguration.SECRET_ENV_KEY + "=test1234"})
public class LogServiceIntTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().bindAddress("localhost").port(8083));

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogService logService;

    @Before
    public void setUp() {
        mongoTemplate.dropCollection(Log.class);
    }

    @After
    public void tearDown() {
        mongoTemplate.dropCollection(Log.class);
    }

    @Test
    public void createLog_savesNewLogInRepository() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();

        Log newLog = logService.createLog(accountId, activityId, duration).get();
        Log fetchedLog = logRepository.findOne(newLog.getId());

        assertNotNull(fetchedLog);
        assertEquals(newLog, fetchedLog);
    }

    @Test
    public void createLog_createsCorrectStartTime() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();

        Calendar calendar = Calendar.getInstance();
        long expectedStartTime = calendar.getTimeInMillis() - duration;

        Log newLog = logService.createLog(accountId, activityId, duration).get();
        long startTime = newLog.getStartTime();

        assertTrue(Math.abs(startTime - expectedStartTime) < 1000L);
    }

    @Test
    public void findAllByAccountId_returnsAllLogsByAccountIdFromRepository() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        logService.createLog(accountId, activityId, duration).get();

        List<Log> logs = logService.findAllByAccountId(accountId).get();
        List<Log> fetchedLogs = logService.findAllByAccountId(accountId).get();

        assertEquals(fetchedLogs, logs);
    }

    @Test
    public void findAllByAccountIdAndActivityId_returnsAllLogsByAccountIdAndActivityIdFromRepository() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        logService.createLog(accountId, activityId, duration).get();

        List<Log> logs = logService.findAllByAccountIdAndActivityId(accountId, activityId).get();
        List<Log> fetchedLogs = logService.findAllByAccountIdAndActivityId(accountId, activityId).get();

        assertEquals(fetchedLogs, logs);
    }

    @Test
    public void findOneById_returnsCorrectLogIfExists() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();

        Log log = logService.createLog(accountId, activityId, duration).get();
        Optional<Log> fetchedLog = logService.findOneById(log.getId()).get();

        assertTrue(fetchedLog.isPresent());
        assertEquals(log, fetchedLog.get());
    }

    @Test
    public void deleteLog_removesLogFromRepository() throws Exception {
        String accountId = generateRandomString(10);
        String activityId = generateRandomString(10);
        long duration = generateDuration();
        Log log = logService.createLog(accountId, activityId, duration).get();

        logService.deleteLog(log.getId()).get();
        Log fetchedLog = logRepository.findOne(log.getId());

        assertNull(fetchedLog);
    }

    @Test(expected = Exception.class)
    public void deleteLog_throwsExceptionIfLogDoesNotExist() throws Exception {
        String id = generateRandomString(10);
        logService.deleteLog(id).get();
    }
}
