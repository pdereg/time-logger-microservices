package com.pdereg.timelogger.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested account resource could not be found.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Account not found")
public class AccountNotFoundException extends RuntimeException {

}
