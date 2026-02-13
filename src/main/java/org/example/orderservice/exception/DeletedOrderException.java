package org.example.orderservice.exception;

public class DeletedOrderException extends RuntimeException {
    public DeletedOrderException(String message) {
        super(message);
    }
}
