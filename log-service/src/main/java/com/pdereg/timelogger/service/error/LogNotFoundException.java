package com.pdereg.timelogger.service.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested log resource could not be found.
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Log not found")
public class LogNotFoundException extends RuntimeException {
}
