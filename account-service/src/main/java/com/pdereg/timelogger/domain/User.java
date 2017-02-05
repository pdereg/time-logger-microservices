package com.pdereg.timelogger.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User domain object.
 */
@Document
public class User implements UserDetails {

    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9]+";
    public static final int MIN_USERNAME_SIZE = 3;
    public static final int MAX_USERNAME_SIZE = 30;
    public static final int PASSWORD_SIZE = 60;

    @Id
    private String id;

    @NotNull
    @Size(min = MIN_USERNAME_SIZE, max = MAX_USERNAME_SIZE)
    @Pattern(regexp = USERNAME_PATTERN)
    private String username;

    @JsonIgnore
    @NotNull
    @Size(min = PASSWORD_SIZE, max = PASSWORD_SIZE)
    private String password;

    @JsonIgnore
    private Set<GrantedAuthority> authorities = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableCollection(authorities);
    }

    /**
     * Adds provided {@code authority} to user's collection of authorities.
     *
     * @param authority New {@link GrantedAuthority} instance to add
     */
    public void addAuthority(@NotNull GrantedAuthority authority) {
        authorities.add(authority);
    }

    /**
     * Removes provided {@code authority} from user's collection of authorities.
     *
     * @param authority {@link GrantedAuthority} instance to remove
     * @return {@code true} if provided {@code authority} was removed; {@code false} otherwise
     */
    public boolean removeAuthority(GrantedAuthority authority) {
        return authorities.remove(authority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Sets user's password to a new value. Note that this method stores provided {@code password} as-is. It is
     * recommended to hash the password first before passing it to this method.
     *
     * @param password New password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Sets user's username to a new value.
     *
     * @param username New username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Method unused.
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Method unused.
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Method unused.
     */
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Method unused.
     */
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
