package com.pdereg.timelogger.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Activity domain object.
 */
@Document
@CompoundIndexes(
        @CompoundIndex(name = "compound_index", def = "{'accountId': 1, 'name': 1}", unique = true)
)
public class Activity {

    public static final int MIN_NAME_SIZE = 3;
    public static final int MAX_NAME_SIZE = 30;
    public static final int MIN_REQUIRED_TIME = 60000;

    @Indexed
    @NotNull
    private final String accountId;

    @Indexed
    @NotNull
    @Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE)
    private final String name;

    @Min(MIN_REQUIRED_TIME)
    private long requiredTime;

    @JsonIgnore
    @Min(0)
    private byte weekdays;

    public Activity(String accountId, String name) {
        this.accountId = accountId;
        this.name = name;
    }

    /**
     * @return ID of the user account associated with this {@link Activity} instance
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @return Activity's name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Required time (per day) for the duration of the activity. Expressed in milliseconds
     */
    public long getRequiredTime() {
        return requiredTime;
    }

    /**
     * Sets a new required time for the duration of the activity.
     *
     * @param requiredTime New required time to set
     */
    public void setRequiredTime(long requiredTime) {
        this.requiredTime = requiredTime;
    }

    /**
     * @return An array of weekdays where element at each position describes whether the activity should be performed
     * on that day (starting from Monday)
     */
    @JsonProperty("weekdays")
    public boolean[] getWeekdays() {
        final boolean[] weekdaysArray = new boolean[7];

        for (int i = 0; i < 7; ++i) {
            byte weekday = 0x01;
            weekday <<= i;

            if ((weekday & weekdays) > 0) {
                weekdaysArray[i] = true;
            }
        }

        return weekdaysArray;
    }

    /**
     * Sets a new array of weekdays describing on which days the activity should be performed (starting from Monday)
     *
     * @param weekdaysArray New weekdays to set
     */
    public void setWeekdays(boolean[] weekdaysArray) {
        if (weekdaysArray.length != 7) {
            return;
        }

        for (int i = 0; i < 7; ++i) {
            byte weekday = 0x01;
            weekday <<= i;

            if (weekdaysArray[i]) {
                weekdays ^= weekday;
            } else {
                weekdays &= ~weekday;
            }
        }
    }
}
