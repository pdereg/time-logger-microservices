package com.pdereg.timelogger.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * Logs errors and incoming HTTP requests.
 */
@Component
@Aspect
public class LogComponent {

    private final Logger log = LoggerFactory.getLogger(LogComponent.class);

    @Pointcut("within(com.pdereg.timelogger.security..*) || within(com.pdereg.timelogger.service..*) || within(com.pdereg.timelogger.web..*)")
    public void errorLoggingPointcut() {

    }

    /**
     * Logs exceptions thrown from security, service and web layers.
     */
    @AfterThrowing(value = "errorLoggingPointcut()", throwing = "throwable")
    public void logExceptions(JoinPoint joinPoint, Throwable throwable) {
        final Signature signature = joinPoint.getSignature();
        final String type = signature.getDeclaringTypeName();
        final String method = signature.getName();

        log.error(
                "An exception was thrown in {}#{}() with cause = {} and message = {}",
                type, method, throwable.getCause(), throwable.getLocalizedMessage()
        );
    }

    /**
     * Logs serviced HTTP requests.
     *
     * @param event HTTP request event data
     */
    @EventListener(ServletRequestHandledEvent.class)
    public void logHandledRequests(ServletRequestHandledEvent event) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("url=[").append(event.getRequestUrl()).append("]; ");
        stringBuilder.append("client=[").append(event.getClientAddress()).append("]; ");
        stringBuilder.append("method=[").append(event.getMethod()).append("]; ");
        stringBuilder.append("user=[").append(event.getUserName()).append("]; ");
        stringBuilder.append("time=[").append(event.getProcessingTimeMillis()).append("ms]; ");
        stringBuilder.append("status=[");

        Throwable throwable = event.getFailureCause();
        if (throwable == null) {
            stringBuilder.append("OK");
        } else {
            stringBuilder.append("failed: ").append(throwable);
        }
        stringBuilder.append(']');

        log.info("Request handled: {}", stringBuilder.toString());
    }
}
