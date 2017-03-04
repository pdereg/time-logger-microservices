package com.pdereg.timelogger.web.rest.model;

import com.pdereg.timelogger.domain.Activity;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * An HTTP request body for activity update.
 */
public class UpdateActivityRequest {

    private static final int WEEKDAYS_COUNT = 7;

    @Min(value = Activity.MIN_REQUIRED_DURATION)
    private long requiredDuration;

    @Size(min = WEEKDAYS_COUNT, max = WEEKDAYS_COUNT)
    private boolean[] weekdays;

    public long getRequiredDuration() {
        return requiredDuration;
    }

    public void setRequiredDuration(long requiredDuration) {
        this.requiredDuration = requiredDuration;
    }

    public boolean[] getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(boolean[] weekdays) {
        this.weekdays = weekdays;
    }
}
