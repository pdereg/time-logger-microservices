package com.pdereg.timelogger.network;

import com.pdereg.timelogger.network.model.Activity;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Network client for the activity-service.
 */
@Component
@FeignClient("activity-service")
public interface ActivityClient {

    /**
     * Retrieves activity with provided {@code accountId} and {@code name}.
     *
     * @param authorizationHeader HTTP authorization header for authenticating with activity-service
     * @param accountId           ID of the user account associated with the activity
     * @param name                Name of the activity to retrieve
     * @return Fetched activity
     */
    @RequestMapping(method = RequestMethod.GET, value = "/api/activities/{accountId}/{name}")
    Activity getActivity(@RequestHeader("Authorization") String authorizationHeader,
                         @PathVariable("accountId") String accountId,
                         @PathVariable("name") String name);
}
