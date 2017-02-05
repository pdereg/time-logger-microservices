package com.pdereg.timelogger.domain;

import com.pdereg.timelogger.config.DatabaseConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.Set;

import static com.pdereg.timelogger.TestUtils.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = DatabaseConfiguration.class)
public class UserTest {

    @Autowired
    private LocalValidatorFactoryBean validatorFactoryBean;

    private User user;

    @Before
    public void setUp() {
        user = new User();

        String username = generateRandomUsername();
        user.setUsername(username);
    }

    @Test
    public void setUsername_setsNewValueCorrectly() {
        String username = generateRandomUsername();
        user.setUsername(username);

        assertEquals(username, user.getUsername());
    }

    @Test
    public void setUsername_generatesConstraintViolationIfNewValueIsTooShort() {
        String username = generateRandomString(User.MIN_USERNAME_SIZE - 1);
        user.setUsername(username);

        Set<ConstraintViolation<User>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void setUsername_generatesConstraintViolationIfNewValueIsTooLong() {
        String username = generateRandomString(User.MAX_USERNAME_SIZE + 1);
        user.setUsername(username);

        Set<ConstraintViolation<User>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void setPassword_setsNewValueCorrectly() {
        String password = generateRandomPassword();
        user.setPassword(password);

        assertEquals(password, user.getPassword());
    }

    @Test
    public void setPassword_generatesConstraintViolationIfNewValueIsNull() {
        user.setPassword(null);

        Set<ConstraintViolation<User>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void setPassword_generatesConstraintViolationIfNewValueHasIncorrectSize() {
        String password = generateRandomString(User.PASSWORD_SIZE + 1);
        user.setPassword(password);

        Set<ConstraintViolation<User>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void addAuthority_addsNewValueCorrectly() {
        GrantedAuthority authority = generateAuthority();
        user.addAuthority(authority);

        Collection<?> grantedAuthorities = user.getAuthorities();
        assertTrue(grantedAuthorities.contains(authority));
    }

    @Test
    public void addAuthority_generatesConstraintViolationIfNewValueIsNull() {
        user.addAuthority(null);

        Set<ConstraintViolation<User>> violations = getConstraintViolations();
        assertFalse(violations.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAuthorities_returnsImmutableCollection() {
        GrantedAuthority authority = generateAuthority();
        user.addAuthority(authority);

        Collection<?> grantedAuthorities = user.getAuthorities();
        grantedAuthorities.clear();
    }

    @Test
    public void removeAuthority_removesValueCorrectly() {
        GrantedAuthority authority = generateAuthority();
        user.addAuthority(authority);

        boolean result = user.removeAuthority(authority);
        assertTrue(result);

        Collection<?> grantedAuthorities = user.getAuthorities();
        assertFalse(grantedAuthorities.contains(authority));
    }

    @Test
    public void removeAuthority_returnsFalseIfAuthorityDoesNotExist() {
        GrantedAuthority authority = generateAuthority();
        assertFalse(user.removeAuthority(authority));
    }

    @Test
    public void isAccountNonExpired_returnsTrue() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    public void isAccountNonLocked_returnsTrue() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    public void isCredentialsNonExpired_returnsTrue() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    public void isEnabled_returnsTrue() {
        assertTrue(user.isEnabled());
    }

    @Test
    @SuppressWarnings("EqualsWithItself")
    public void equals_returnsTrueIfObjectsAreSame() {
        assertTrue(user.equals(user));
    }

    @Test
    public void equals_returnsTrueIfObjectsHaveEqualUsernames() {
        String originalUsername = user.getUsername();

        User newUser = new User();
        newUser.setUsername(originalUsername);

        assertTrue(user.equals(newUser));
    }

    @Test
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    public void equals_returnsFalseForNullValue() {
        assertFalse(user.equals(false));
    }

    @Test
    public void toString_returnsTextThatDoesNotContainUserPassword() {
        String password = generateRandomPassword();
        user.setPassword(password);

        String userAsString = user.toString();
        assertFalse(userAsString.contains(password));
    }

    private Set<ConstraintViolation<User>> getConstraintViolations() {
        return validatorFactoryBean.validate(user);
    }

    private GrantedAuthority generateAuthority() {
        String authorityName = generateRandomString(10);
        return new SimpleGrantedAuthority(authorityName);
    }
}
