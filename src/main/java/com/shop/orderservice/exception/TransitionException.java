package com.shop.orderservice.exception;

import java.io.Serial;

public class TransitionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6202313497919438853L;

    public TransitionException() {
    }

    public TransitionException(Throwable cause) {
        super(cause);
    }

    public TransitionException(String message) {
        super(message);
    }

    public TransitionException(String message, Throwable cause) {
        super(message, cause);
    }

}