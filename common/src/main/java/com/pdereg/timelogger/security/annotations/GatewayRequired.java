package com.pdereg.timelogger.security.annotations;

import com.pdereg.timelogger.security.Authorities;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('" + Authorities.GATEWAY + "')")
public @interface GatewayRequired {
}
