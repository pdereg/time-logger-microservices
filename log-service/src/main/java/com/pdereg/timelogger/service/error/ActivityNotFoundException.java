package com.pdereg.timelogger.service.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an activity for which to list log resources could not be found.
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Activity not found")
public class ActivityNotFoundException extends RuntimeException {
}
