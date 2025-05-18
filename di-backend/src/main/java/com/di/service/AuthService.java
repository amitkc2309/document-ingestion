package com.di.service;

import com.di.dto.AuthResponse;
import com.di.dto.LoginRequest;
import com.di.dto.RegisterRequest;

public interface AuthService {
    
    /**
     * Register a new user
     * @param request Registration details
     * @return Authentication response with JWT token
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Authenticate a user
     * @param request Login credentials
     * @return Authentication response with JWT token
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Invalidate a user's token
     * @param token JWT token to invalidate
     */
    void logout(String token);
}