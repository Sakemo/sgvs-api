package com.flick.business.exception;

/**
 * Exceção lançada quando um token OAuth (Google) é inválido ou expirado
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
