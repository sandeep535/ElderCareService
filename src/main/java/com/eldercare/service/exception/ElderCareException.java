package com.eldercare.service.exception;

public class ElderCareException extends RuntimeException {

    public ElderCareException(String message) {
        super(message);
    }

    public ElderCareException(String message, Throwable cause) {
        super(message, cause);
    }
}
