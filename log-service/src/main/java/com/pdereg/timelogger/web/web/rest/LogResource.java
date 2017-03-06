package com.pdereg.timelogger.web.web.rest;

import com.pdereg.timelogger.domain.Log;
import com.pdereg.timelogger.security.annotations.AdminOrAccountOwnerRequired;
import com.pdereg.timelogger.service.ActivityService;
import com.pdereg.timelogger.service.LogService;
import com.pdereg.timelogger.service.error.LogNotFoundException;
import com.pdereg.timelogger.web.web.rest.model.CreateLogRequest;
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

    /**
     * Creates a enw {@link Log}.
     *
     * @param authorizationHeader User's authentication token for whom to create new log
     * @param request             HTTP request body which contains data for log creation
     * @return Newly created {@link Log} instance
     */
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

    /**
     * Fetches and returns all {@link Log} instances for an account with provided {@code username}.
     *
     * @param username Name of the account associated with the logs to return
     * @return A list of all {@link Log} instances for an account with provided {@code username}
     */
    @GetMapping("/logs/{username}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<List<Log>> findAllByAccountId(@PathVariable String username) {
        return logService.findAllByAccountId(username);
    }

    /**
     * Fetches and returns all {@link Log} instances for an account with provided {@code username} and
     * {@code activityId}.
     *
     * @param username   Name of the account associated with the logs to return
     * @param activityId ID of the activity associated with the logs to return
     * @return A list of all {@link Log} instances for an account with provided {@code username} and {@code activityId}
     */
    @GetMapping("/logs/{username}/{activityId}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<List<Log>> findAllByAccountIdAndActivityName(@PathVariable String username,
                                                                          @PathVariable String activityId) {

        return logService.findAllByAccountIdAndActivityId(username, activityId);
    }

    /**
     * Fetches and returns a {@link Log} instance with given {@code id} for an account with provided {@code username}
     * and {@code activityId}.
     *
     * @param username   Name of the account associated with the log to return
     * @param activityId ID of the activity associated with the log to return
     * @param id         ID of the log to return
     * @return {@link Log} instance
     */
    @GetMapping("/logs/{username}/{activityId}/{id}")
    @AdminOrAccountOwnerRequired
    public CompletableFuture<Log> findOneById(@PathVariable String username, @PathVariable String activityId,
                                              @PathVariable String id) {
        return logService
                .findAllByAccountIdAndActivityId(username, activityId)
                .thenApply(logs -> logs.stream()
                        .filter(log -> log.getId().equals(id))
                        .findAny()
                )
                .thenApply(log -> log.<LogNotFoundException>orElseThrow(LogNotFoundException::new));
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
