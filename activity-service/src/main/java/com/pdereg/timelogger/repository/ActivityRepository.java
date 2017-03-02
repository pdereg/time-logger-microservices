package com.pdereg.timelogger.repository;

import com.pdereg.timelogger.domain.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides a communication interface to MongoDB for {@link Activity} domain objects.
 */
@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

    /**
     * Finds and returns all {@link Activity} instances for given {@code accountId}.
     *
     * @param accountId ID of the user associated with activities to return
     * @return A list of {@link Activity} instances
     */
    CompletableFuture<List<Activity>> findAllByAccountId(String accountId);
}
