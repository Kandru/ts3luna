package eu.kandru.luna.controller;

import lombok.extern.slf4j.Slf4j;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by jko on 15.04.2017.
 */
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(JoseException.class)
    public void handleJoseException(JoseException e) {
        log.error("jwt error", e);
    }
}
