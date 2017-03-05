package com.pdereg.timelogger.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Calendar;

/**
 * Log domain object.
 */
@Document
public class Log {

    public static final long MIN_START_TIME = 0L;
    public static final long MIN_DURATION = 1000L;

    private static final long MILLIS_IN_A_DAY = 1000L * 60 * 60 * 24;
    @Indexed
    @NotNull
    private final String accountId;
    @Indexed
    @NotNull
    private final String activityId;
    @Min(value = MIN_START_TIME)
    private final long startTime;
    @Min(value = MIN_DURATION)
    private final long duration;
    @Id
    private String id;

    public Log(String accountId, String activityId, long startTime, long duration) {
        this.accountId = accountId;
        this.activityId = activityId;
        this.startTime = startTime;
        this.duration = duration;

        if (!isDurationAllowed()) {
            throw new IllegalArgumentException("Duration cannot span to another day");
        }
    }

    /**
     * @return Log's unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * @return ID of the user account associated with this {@link Log} instance
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @return ID of the activity associated with this {@link Log} instance
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * @return Time at which logging started (as UNIX timestamp in milliseconds)
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return Duration of the log (in milliseconds)
     */
    public long getDuration() {
        return duration;
    }

    private boolean isDurationAllowed() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);

        final long millisElapsed = calendar.get(Calendar.MILLISECOND);
        return millisElapsed + duration <= MILLIS_IN_A_DAY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Log log = (Log) o;

        if (startTime != log.startTime) return false;
        if (duration != log.duration) return false;
        if (id != null ? !id.equals(log.id) : log.id != null) return false;
        if (accountId != null ? !accountId.equals(log.accountId) : log.accountId != null) return false;
        return activityId != null ? activityId.equals(log.activityId) : log.activityId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + (activityId != null ? activityId.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id='" + id + '\'' +
                ", accountId='" + accountId + '\'' +
                ", activityId='" + activityId + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}
