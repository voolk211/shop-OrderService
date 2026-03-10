package org.example.orderservice.exception;

import java.io.Serial;

public class ItemInUseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6222877106616333877L;

    public ItemInUseException(String message) {
        super(message);
    }
}
