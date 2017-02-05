package com.pdereg.timelogger.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown during account creation if provided username is already in use.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Username already in use")
public class UsernameInUseException extends RuntimeException {

}
