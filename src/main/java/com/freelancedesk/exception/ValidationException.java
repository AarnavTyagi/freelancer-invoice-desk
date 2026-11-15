package com.freelancedesk.exception;

/** Thrown when user input or a business rule check fails. */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
