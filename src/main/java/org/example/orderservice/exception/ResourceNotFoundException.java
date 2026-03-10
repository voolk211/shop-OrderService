package org.example.orderservice.exception;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1391436076543825030L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
