package com.di.exception;

import lombok.Getter;

/**
 * Exception thrown when authentication fails.
 */
@Getter
public class AuthenticationException extends RuntimeException {
    
    private final String username;
    
    public AuthenticationException(String message) {
        super(message);
        this.username = null;
    }
    
    public AuthenticationException(String message, String username) {
        super(message);
        this.username = username;
    }
}