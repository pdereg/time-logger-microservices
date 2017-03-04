package com.pdereg.timelogger.web.rest.model;

import com.pdereg.timelogger.domain.Activity;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An HTTP request body for activity creation.
 */
public class CreateActivityRequest {

    private static final int WEEKDAYS_COUNT = 7;

    @NotNull
    @Size(min = Activity.MIN_NAME_SIZE, max = Activity.MAX_NAME_SIZE)
    private String name;

    @Min(value = Activity.MIN_REQUIRED_DURATION)
    private long requiredDuration;

    @Size(min = WEEKDAYS_COUNT, max = WEEKDAYS_COUNT)
    private boolean[] weekdays;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
