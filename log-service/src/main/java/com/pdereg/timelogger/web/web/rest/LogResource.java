package com.pdereg.timelogger.web.web.rest;

import com.pdereg.timelogger.domain.Log;
import com.pdereg.timelogger.service.ActivityService;
import com.pdereg.timelogger.service.LogService;
import com.pdereg.timelogger.web.web.rest.model.CreateLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for logs.
 */
@RestController
@RequestMapping("/api")
public class LogResource {

    private final ActivityService activityService;
    private final LogService logService;

    @Autowired
    public LogResource(ActivityService activityService, LogService logService) {
        this.activityService = activityService;
        this.logService = logService;
    }

    @PostMapping("/logs")
    public CompletableFuture<HttpEntity<Log>> createLog(@RequestHeader("Authorization") String authorizationHeader,
                                                        @RequestBody @Valid CreateLogRequest request,
                                                        Principal principal) {

        final String accountId = principal.getName();
        final String activityId = request.getActivityName();

        return activityService
                .getActivity(authorizationHeader, accountId, activityId)
                .thenComposeAsync(unit -> logService.createLog(accountId, activityId, request.getDuration()))
                .thenApply(this::createLogResponse);
    }

    private HttpEntity<Log> createLogResponse(Log log) {
        final URI logUri = createLogUri(log);

        return ResponseEntity
                .created(logUri)
                .body(log);
    }

    private URI createLogUri(Log log) {
        try {
            return new URI("/api/logs/" + log.getAccountId() + "/" + log.getActivityId() + "/" + log.getId());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
