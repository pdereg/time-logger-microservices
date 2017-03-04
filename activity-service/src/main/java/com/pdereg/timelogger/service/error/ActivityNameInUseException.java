package com.pdereg.timelogger.service.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown during activity creation if provided activity name already exists for a given account.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Activity for provided name and account already exists")
public class ActivityNameInUseException extends RuntimeException {
}
