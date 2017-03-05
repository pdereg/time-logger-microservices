package com.pdereg.timelogger.domain;

import com.pdereg.timelogger.TestUtils;
import com.pdereg.timelogger.config.DatabaseConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = DatabaseConfiguration.class)
public class LogTest {

    @Autowired
    private LocalValidatorFactoryBean validatorFactoryBean;

    private Log log;

    @Before
    public void setUp() {
        String accountId = TestUtils.generateRandomString(10);
        String activityId = TestUtils.generateRandomString(10);
        long duration = TestUtils.generateDuration();
        long startTime = TestUtils.generateStartTime(duration);

        log = new Log(accountId, activityId, startTime, duration);
    }

    @Test
    public void constructor_setsAttributesCorrectly() {
        String accountId = TestUtils.generateRandomString(10);
        String activityId = TestUtils.generateRandomString(10);
        long duration = TestUtils.generateDuration();
        long startTime = TestUtils.generateStartTime(duration);
        log = new Log(accountId, activityId, startTime, duration);

        assertEquals(accountId, log.getAccountId());
        assertEquals(activityId, log.getActivityId());
        assertEquals(startTime, log.getStartTime());
        assertEquals(duration, log.getDuration());
    }

    @Test
    public void constructor_generatesConstraintViolationIfStartTimeIsTooShort() {
        String accountId = TestUtils.generateRandomString(10);
        String activityId = TestUtils.generateRandomString(10);
        long duration = TestUtils.generateDuration();
        long startTime = Log.MIN_START_TIME - 1;
        log = new Log(accountId, activityId, startTime, duration);

        Set<ConstraintViolation<Log>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void constructor_generatesConstraintViolationIfDurationIsTooShort() {
        String accountId = TestUtils.generateRandomString(10);
        String activityId = TestUtils.generateRandomString(10);
        long duration = Log.MIN_DURATION - 1;
        long startTime = TestUtils.generateStartTime(duration);
        log = new Log(accountId, activityId, startTime, duration);

        Set<ConstraintViolation<Log>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsExceptionIfDurationSpansToAnotherDay() {
        String accountId = TestUtils.generateRandomString(10);
        String activityId = TestUtils.generateRandomString(10);
        long duration = 1000L * 60 * 60 * 24 + 1;
        long startTime = TestUtils.generateStartTime(duration);

        log = new Log(accountId, activityId, startTime, duration);
    }

    @Test
    @SuppressWarnings("EqualsWithItself")
    public void equals_returnsTrueIfObjectsAreSame() {
        assertTrue(log.equals(log));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void equals_returnsFalseForNullValue() {
        assertFalse(log.equals(null));
    }

    private Set<ConstraintViolation<Log>> getConstraintViolations() {
        return validatorFactoryBean.validate(log);
    }
}
