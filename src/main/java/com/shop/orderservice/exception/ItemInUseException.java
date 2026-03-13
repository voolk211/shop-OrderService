package com.shop.orderservice.exception;

import java.io.Serial;

public class ItemInUseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6222877106616333877L;

    public ItemInUseException() {
    }

    public ItemInUseException(Throwable cause) {
        super(cause);
    }

    public ItemInUseException(String message) {
        super(message);
    }

    public ItemInUseException(String message, Throwable cause) {
        super(message, cause);
    }

}