package com.barbershop.user_service.exception;

/**
 * Exception thrown when attempting to register with an existing account
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause){
      super(message, cause);
    }
}
