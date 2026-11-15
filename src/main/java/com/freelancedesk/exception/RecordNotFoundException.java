package com.freelancedesk.exception;

/** Thrown when a freelancer, client, service or invoice cannot be found. */
public class RecordNotFoundException extends Exception {
    public RecordNotFoundException(String message) {
        super(message);
    }
}
