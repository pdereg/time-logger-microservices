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

import static com.pdereg.timelogger.TestUtils.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = DatabaseConfiguration.class)
public class ActivityTest {

    @Autowired
    private LocalValidatorFactoryBean validatorFactoryBean;

    private Activity activity;

    @Before
    public void setUp() {
        String accountId = TestUtils.generateRandomString(10);
        String name = generateRandomActivityName();
        activity = new Activity(accountId, name);
    }

    @Test
    public void constructor_setsAttributesCorrectly() {
        String accountId = TestUtils.generateRandomString(10);
        String name = generateRandomActivityName();
        activity = new Activity(accountId, name);

        assertEquals(accountId, activity.getAccountId());
        assertEquals(name, activity.getName());
    }

    @Test
    public void constructor_generatesConstraintViolationIfNameIsTooShort() {
        String accountId = TestUtils.generateRandomString(10);
        String name = TestUtils.generateRandomString(Activity.MIN_NAME_SIZE - 1);
        activity = new Activity(accountId, name);

        Set<ConstraintViolation<Activity>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void constructor_generatesConstraintViolationIfNameIsTooLong() {
        String accountId = TestUtils.generateRandomString(10);
        String name = TestUtils.generateRandomString(Activity.MIN_NAME_SIZE + 1);
        activity = new Activity(accountId, name);

        Set<ConstraintViolation<Activity>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void setRequiredDuration_setsNewValueCorrectly() {
        long requiredDuration = generateActivityDuration();
        activity.setRequiredDuration(requiredDuration);

        assertEquals(requiredDuration, activity.getRequiredDuration());
    }

    @Test
    public void setRequiredDuration_generatesConstraintViolationIfNewValueIsTooShort() {
        long requiredDuration = Activity.MIN_REQUIRED_DURATION - 1;
        activity.setRequiredDuration(requiredDuration);

        Set<ConstraintViolation<Activity>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void setWeekdays_setsNewValueCorrectly() {
        boolean[] weekdays = generateActivityWeekdays();
        activity.setWeekdays(weekdays);

        assertArrayEquals(weekdays, activity.getWeekdays());
    }

    @Test
    public void setWeekdays_doesNothingIfNewWeekdaysHasDifferentLengthThan7() {
        boolean[] weekdays = new boolean[]{false, true, true};
        activity.setWeekdays(weekdays);

        assertNotEquals(weekdays, activity.getWeekdays());
    }

    @Test
    @SuppressWarnings("EqualsWithItself")
    public void equals_returnTrueIfObjectsAreSame() {
        assertTrue(activity.equals(activity));
    }

    @Test
    public void equals_returnsTrueIfObjectsHaveEqualAccountIdAndName() {
        String originalAccountId = activity.getAccountId();
        String originalName = activity.getName();

        Activity newActivity = new Activity(originalAccountId, originalName);

        assertTrue(activity.equals(newActivity));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void equals_returnsFalseForNullValue() {
        assertFalse(activity.equals(null));
    }

    private Set<ConstraintViolation<Activity>> getConstraintViolations() {
        return validatorFactoryBean.validate(activity);
    }
}
