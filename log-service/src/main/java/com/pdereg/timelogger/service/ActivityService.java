package com.pdereg.timelogger.service;

import com.pdereg.timelogger.network.ActivityClient;
import com.pdereg.timelogger.network.model.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Provides an abstraction layer over {@link ActivityClient}. Used for retrieving activities.
 */
@Service
public class ActivityService {

    private final ActivityClient activityClient;

    @Autowired
    public ActivityService(ActivityClient activityClient) {
        this.activityClient = activityClient;
    }

    /**
     * Retrieves activity with provided {@code accountId} and {@code name}.
     *
     * @param authorizationHeader HTTP authorization header for authenticating with activity-service
     * @param accountId           ID of the user account associated with the activity
     * @param name                Name of the activity to retrieve
     * @return Fetched activity
     */
    public CompletableFuture<Activity> getActivity(String authorizationHeader, String accountId, String name) {
        return CompletableFuture.supplyAsync(() -> activityClient.getActivity(authorizationHeader, accountId, name));
    }
}
