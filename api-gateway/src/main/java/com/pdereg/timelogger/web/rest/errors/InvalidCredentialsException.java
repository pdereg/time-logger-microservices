package com.pdereg.timelogger.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown during authorization when provided username or password is incorrect.
 */
@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Invalid username or password")
public class InvalidCredentialsException extends RuntimeException {
}
