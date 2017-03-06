package com.pdereg.timelogger.service;

import com.pdereg.timelogger.network.ActivityClient;
import com.pdereg.timelogger.network.model.Activity;
import com.pdereg.timelogger.service.error.ActivityNotFoundException;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        return CompletableFuture.supplyAsync(() -> activityClient.getActivity(authorizationHeader, accountId, name))
                .exceptionally(this::rethrowCorrectError);
    }

    private Activity rethrowCorrectError(Throwable throwable) {
        final Throwable cause = findCausingThrowable(throwable);

        if (cause instanceof FeignException) {
            final int status = ((FeignException) cause).status();

            if (status == HttpStatus.NOT_FOUND.value()) {
                throw new ActivityNotFoundException();
            }
        }

        throw new RuntimeException(throwable);
    }

    private Throwable findCausingThrowable(Throwable throwable) {
        while (throwable != null && throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        return throwable;
    }
}
