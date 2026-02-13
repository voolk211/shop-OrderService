package org.example.orderservice.exception;

public class ItemInUseException extends RuntimeException {
    public ItemInUseException(String message) {
        super(message);
    }
}
