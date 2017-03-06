package com.pdereg.timelogger.service;

import com.pdereg.timelogger.domain.Log;
import com.pdereg.timelogger.repository.LogRepository;
import com.pdereg.timelogger.service.error.LogNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Provides an abstraction layer over {@link LogRepository}. used for performing CRUD operations on {@link Long}
 * instances.
 */
@Service
public class LogService {

    private final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Creates and returns a new {@link Log} instance.
     *
     * @param accountId  ID of the user account associated with the log
     * @param activityId ID of the activity associated with the log
     * @param duration   Duration of the log. Expressed in milliseconds
     * @return New {@link Log} instance
     * @see Log
     */
    public CompletableFuture<Log> createLog(String accountId, String activityId, long duration) {
        final long startTime = getStartTime(duration);
        final Log log = new Log(accountId, activityId, startTime, duration);

        return CompletableFuture.supplyAsync(() -> logRepository.save(log));
    }

    /**
     * Fetches and returns all {@link Log} instances with a given {@code accountId}.
     *
     * @param accountId ID of the user account associated with the logs to return
     * @return A list of all {@link Log} instances for provided {@code accountId}
     */
    public CompletableFuture<List<Log>> findAllByAccountId(String accountId) {
        return CompletableFuture.supplyAsync(() -> logRepository.findAllByAccountId(accountId));
    }

    /**
     * Fetches and returns all {@link Log} instances with given {@code accountID} and {@code activityId}.
     *
     * @param accountId  ID of the user account associated with the logs to return
     * @param activityId ID of the activity associated with the logs to return
     * @return A list of all {@link Log} instances for provided {@code accountId} and {@code activityId}
     */
    public CompletableFuture<List<Log>> findAllByAccountIdAndActivityId(String accountId, String activityId) {
        return CompletableFuture.supplyAsync(
                () -> logRepository.findAllByAccountIdAndActivityId(accountId, activityId)
        );
    }

    /**
     * Fetches and return a {@link Log} instance with a given {@code id}.
     *
     * @param id ID of the log to return
     * @return Optional {@link Log} instance with a given {@code id}
     */
    public CompletableFuture<Optional<Log>> findOneById(String id) {
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(logRepository.findOne(id)));
    }

    /**
     * Deletes an existing log with provided {@code id} from repository.
     *
     * @param id ID of the log to delete
     */
    public CompletableFuture<Void> deleteLog(String id) {
        return findOneById(id)
                .thenApply(log -> log.<LogNotFoundException>orElseThrow(LogNotFoundException::new))
                .thenAccept(logRepository::delete);
    }

    private long getStartTime(long duration) {
        final Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis() - duration;
    }
}
