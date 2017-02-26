package com.pdereg.timelogger.web.rest.errors;

import com.pdereg.timelogger.domain.User;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * An HTTP request body for user account creation.
 */
public class CreateAccountRequest {

    @NotNull
    @Size(min = User.MIN_USERNAME_SIZE, max = User.MAX_USERNAME_SIZE)
    @Pattern(regexp = User.USERNAME_PATTERN)
    private String username;

    @NotNull
    @Size(min = 8, max = User.PASSWORD_SIZE)
    private String password;

    public CreateAccountRequest() {

    }

    public CreateAccountRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
