package com.pdereg.timelogger.network;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Network client for the account-service.
 */
@Component
@FeignClient("account-service")
public interface AccountClient {

    /**
     * Authenticates user with provided {@code username}.
     *
     * @param authorizationHeaderValue Gateway's authorization header
     * @param username                 Name of the user to authenticate
     * @param password                 User's raw password
     * @return A set of user's authorities upon successful authentication or error
     */
    @RequestMapping(method = RequestMethod.GET, value = "/api/accounts/{username}/authenticate")
    Set<String> authenticate(@RequestHeader("Authorization") String authorizationHeaderValue,
                             @PathVariable("username") String username, @RequestParam("password") String password);
}
