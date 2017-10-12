package eu.kandru.luna.controller;

import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.concurrent.TimeoutException;

/**
 * Created by jko on 15.04.2017.
 */
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JoseException.class)
    public void handleJoseException(JoseException e) {
        log.error("unexpected error while creating jwt", e);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidJwtException.class)
    public void handleInvalidJwt(InvalidJwtException e) {
        log.warn("a given jwt was invalid", e);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(MalformedClaimException.class)
    public void handleMalformedClaim(MalformedClaimException e) {
        log.warn("failed to parse jwt", e);
    }

    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    @ExceptionHandler(TimeoutException.class)
    public void handleTimeoutException(TimeoutException e) {
        log.error("connection to ts3 timed out", e);
    }
}
