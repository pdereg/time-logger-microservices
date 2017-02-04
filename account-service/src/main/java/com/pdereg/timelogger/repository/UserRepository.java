package com.pdereg.timelogger.repository;

import com.pdereg.timelogger.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Provides a communication interface to MongoDB for {@link User} domain objects.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds and returns a {@link User} instance of given {@code username}.
     *
     * @param username Name of the user to return
     * @return An optional {@link User} instance
     */
    Optional<User> findOneByUsername(String username);
}
