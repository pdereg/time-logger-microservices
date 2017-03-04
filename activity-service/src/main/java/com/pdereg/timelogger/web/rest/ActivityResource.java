package com.pdereg.timelogger.web.rest;

import com.pdereg.timelogger.domain.Activity;
import com.pdereg.timelogger.security.annotations.UserRequired;
import com.pdereg.timelogger.service.ActivityService;
import com.pdereg.timelogger.web.rest.model.CreateActivityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class ActivityResource {

    private final ActivityService activityService;

    @Autowired
    public ActivityResource(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/activities")
    @UserRequired
    public CompletableFuture<HttpEntity<Activity>> createActivity(@RequestBody @Valid CreateActivityRequest request,
                                                                  Principal principal) {

        final String accountId = principal.getName();
        final String name = request.getName();
        final long requiredTime = request.getRequiredTime();
        final boolean[] weekdays = request.getWeekdays();

        return activityService
                .createActivity(accountId, name, requiredTime, weekdays)
                .thenApply(this::createActivityResponse);
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
