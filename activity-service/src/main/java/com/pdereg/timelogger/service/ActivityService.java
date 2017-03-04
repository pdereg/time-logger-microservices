package com.pdereg.timelogger.service;

import com.pdereg.timelogger.domain.Activity;
import com.pdereg.timelogger.repository.ActivityRepository;
import com.pdereg.timelogger.service.error.ActivityNameInUseException;
import com.pdereg.timelogger.service.error.ActivityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Provides an abstraction layer over {@link ActivityRepository}. Used for performing CRUD operations on
 * {@link Activity} instances.
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    /**
     * Creates and returns a new {@link Activity} instance.
     *
     * @param accountId        ID of the user associated with the activity
     * @param name             Name of the activity
     * @param requiredDuration Required duration (per day). Expressed in milliseconds
     * @param weekdays         An array of weekdays where element at each position describes whether the activity should be
     *                         performed on that day (starting from Monday)
     * @return New {@link Activity} instance
     * @see Activity
     */
    public CompletableFuture<Activity> createActivity(String accountId, String name, long requiredDuration,
                                                      boolean[] weekdays) {

        final Activity activity = new Activity(accountId, name);
        activity.setRequiredDuration(requiredDuration);
        activity.setWeekdays(weekdays);

        return findOneByAccountIdAndName(accountId, name)
                .thenAccept(activityOptional -> {
                    if (activityOptional.isPresent()) {
                        throw new ActivityNameInUseException();
                    }
                })
                .thenComposeAsync(unit ->
                        CompletableFuture.supplyAsync(() -> activityRepository.save(activity))
                );
    }

    /**
     * Fetches and returns all {@link Activity} instances from repository.
     *
     * @return A list of all {@link Activity} instances
     */
    public CompletableFuture<List<Activity>> findAll() {
        return CompletableFuture.supplyAsync(activityRepository::findAll);
    }

    /**
     * Fetches and returns all {@link Activity} instances for a given {@code accountId}.
     *
     * @param accountId ID of the account associated with the activities to return
     * @return A list of all {@link Activity} instances associated with provided {@code accountId}
     */
    public CompletableFuture<List<Activity>> findAllByAccountId(String accountId) {
        return CompletableFuture.supplyAsync(() -> activityRepository.findAllByAccountId(accountId));
    }

    /**
     * Fetches and returns an {@link Activity} instance for given {@code accountId} and {@code name}.
     *
     * @param accountId ID of the account associated with the activity to return
     * @param name      Name of the activity to return
     * @return Optional {@link Activity} instance for given {@code accountId} and {@code name}
     */
    public CompletableFuture<Optional<Activity>> findOneByAccountIdAndName(String accountId, String name) {
        return CompletableFuture.supplyAsync(() -> activityRepository.findOneByAccountIdAndName(accountId, name));
    }

    /**
     * Updates an existing {@link Activity} instance for given {@code accountId} and {@code name}.
     *
     * @param accountId        ID of the account associated with the activity to update
     * @param name             Name of the activity to update
     * @param requiredDuration New required duration to set
     * @param weekdays         New weekdays to set
     * @return Updated {@link Activity} instance
     */
    public CompletableFuture<Activity> updateActivity(String accountId, String name, long requiredDuration,
                                                      boolean[] weekdays) {

        return findOneByAccountIdAndName(accountId, name)
                .thenApply(activity -> activity.<ActivityNotFoundException>orElseThrow(ActivityNotFoundException::new))
                .thenApply(activity -> {
                    activity.setRequiredDuration(requiredDuration);
                    activity.setWeekdays(weekdays);
                    return activity;
                })
                .thenApply(activityRepository::save);
    }

    /**
     * Deletes activity of provided {@code accountId} and {@code name} from repository.
     *
     * @param accountId ID of the account associated with the activity to delete
     * @param name      Name of the activity to delete
     */
    public CompletableFuture<Void> deleteActivity(String accountId, String name) {
        return findOneByAccountIdAndName(accountId, name)
                .thenApply(activity -> activity.<ActivityNotFoundException>orElseThrow(ActivityNotFoundException::new))
                .thenAccept(activityRepository::delete);
    }
}
