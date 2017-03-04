package com.pdereg.timelogger.service.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested activity resource could not be found
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Activity not found")
public class ActivityNotFoundException extends RuntimeException {
}
