package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.domain.Activity;
import com.pdereg.timelogger.security.annotations.AdminOrAccountOwnerRequired;
import com.pdereg.timelogger.service.ActivityService;
import com.pdereg.timelogger.service.error.ActivityNotFoundException;
import com.pdereg.timelogger.web.rest.model.CreateActivityRequest;
import com.pdereg.timelogger.web.rest.model.UpdateActivityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for activities.
 */
@RestController
@RequestMapping("/api")
public class ActivityResource {

    private final ActivityService activityService;

    @Autowired
    public ActivityResource(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Creates a new {@link Activity}.
     *
     * @param request   HTTP request body which contains data for activity creation
     * @param principal Currently logged in user
     * @return Newly created {@link Activity} instance
     */
    @PostMapping("/activities")
    public CompletableFuture<HttpEntity<Activity>> createActivity(@RequestBody @Valid CreateActivityRequest request,
                                                                  Principal principal) {

        final String accountId = principal.getName();
        final String name = request.getName();
        final long requiredTime = request.getRequiredDuration();
        final boolean[] weekdays = request.getWeekdays();

        return activityService
                .createActivity(accountId, name, requiredTime, weekdays)
                .thenApply(this::createActivityResponse);
    }

    /**
     * Fetches and returns all {@link Activity} instances for the current user.
     *
     * @param principal Currently logged in user
     * @return A list of all {@link Activity} instances for the current user
     */
    @GetMapping("/activities")
    public CompletableFuture<List<Activity>> getAllActivities(Principal principal) {
        final String accountId = principal.getName();
        return activityService.findAllByAccountId(accountId);
    }

    /**
     * Fetches and returns all {@link Activity} instances for an account with provided {@code username}.
     *
     * @param username Name of the account associated with the activities to return
     * @return A list of all {@link Activity} instances for an account with provided {@code username}
     */
    @GetMapping("/activities/{username}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<List<Activity>> getAllActivitiesForAccount(@PathVariable String username) {
        return activityService.findAllByAccountId(username);
    }

    /**
     * Fetches and returns an {@link Activity} instance with given {@code username} and {@code name}.
     *
     * @param username Name of the account associated with the activity to return
     * @param name     Name of the activity to return
     * @return {@link Activity} instance for given {@code username} and {@code name}
     */
    @GetMapping("/activities/{username}/{name}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<Activity> getActivity(@PathVariable String username, @PathVariable String name) {
        return activityService
                .findOneByAccountIdAndName(username, name)
                .thenApply(activity -> activity.<ActivityNotFoundException>orElseThrow(ActivityNotFoundException::new));
    }

    /**
     * Updates an existing {@link Activity}.
     *
     * @param username Name of the account associated with the activity to update
     * @param name     Name of the activity to update
     * @param request  HTTP request body which contains data for update
     * @return Updated {@link Activity} instance
     */
    @PutMapping("/activities/{username}/{name}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<Activity> updateActivity(@PathVariable String username, @PathVariable String name,
                                                      @RequestBody @Valid UpdateActivityRequest request) {

        final long requiredDuration = request.getRequiredDuration();
        final boolean[] weekdays = request.getWeekdays();

        return activityService.updateActivity(username, name, requiredDuration, weekdays);
    }

    /**
     * Deletes an existing {@link Activity} instance with given {@code username} and {@code name}.
     *
     * @param username Name of the account associated with the activity to delete
     * @param name     Name of the activity to delete
     */
    @DeleteMapping("/activities/{username}/{name}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<Void> deleteActivity(@PathVariable String username, @PathVariable String name) {
        return activityService.deleteActivity(username, name);
    }

    private HttpEntity<Activity> createActivityResponse(Activity activity) {
        final URI activityUri = createActivityUri(activity);

        return ResponseEntity
                .created(activityUri)
                .body(activity);
    }

    private URI createActivityUri(Activity activity) {
        try {
            return new URI("/api/activities/" + activity.getAccountId() + "/" + activity.getName());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
