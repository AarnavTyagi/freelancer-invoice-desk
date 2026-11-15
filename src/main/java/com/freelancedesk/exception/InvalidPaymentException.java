package com.freelancedesk.exception;

/** Thrown when a payment breaks a rule, such as exceeding the outstanding balance. */
public class InvalidPaymentException extends ValidationException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
