package com.pdereg.timelogger.web.web.rest.model;

import com.pdereg.timelogger.domain.Log;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * An HTTP request body for log creation.
 */
public class CreateLogRequest {

    @NotNull
    private String activityName;

    @Min(Log.MIN_DURATION)
    private long duration;

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
