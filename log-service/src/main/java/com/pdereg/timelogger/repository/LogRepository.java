package com.pdereg.timelogger.repository;

import com.pdereg.timelogger.domain.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Provides a communication interface to MongoDB for {@link Log} domain objects.
 */
@Repository
public interface LogRepository extends MongoRepository<Log, String> {

    /**
     * Finds and returns all {@link Log} instances for given {@code accountId}.
     *
     * @param accountId ID of the user account associated with logs to return
     * @return A list of {@link Log} instances
     */
    List<Log> findAllByAccountId(String accountId);

    /**
     * Finds and returns all {@link Log} instances for given {@code accountId} and {@code activityId}.
     *
     * @param accountId  ID of the user account associated with logs to return
     * @param activityId ID of the activity associated with the logs to return
     * @return A list of {@link Log} instances
     */
    List<Log> findAllByAccountIdAndActivityId(String accountId, String activityId);
}
