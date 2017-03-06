package com.pdereg.timelogger.network.model;

/**
 * An HTTP response body for activity domain objects.
 */
public class Activity {

    private String accountId;
    private String name;
    private long requiredDuration;
    private boolean[] weekdays;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

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
