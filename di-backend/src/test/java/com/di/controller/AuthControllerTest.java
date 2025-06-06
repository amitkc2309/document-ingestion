package com.di.controller;

import com.di.dto.AuthResponse;
import com.di.dto.RegisterRequest;
import com.di.model.Role;
import com.di.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testRegisterWithRole() {
        // Test for ADMIN role
        RegisterRequest adminRequest = RegisterRequest.builder()
                .username("adminuser")
                .password("password")
                .email("admin@example.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build();

        AuthResponse adminResponse = AuthResponse.builder()
                .username("adminuser")
                .email("admin@example.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .token("admin-token")
                .build();

        when(authService.register(adminRequest)).thenReturn(adminResponse);

        ResponseEntity<AuthResponse> adminResult = authController.register(adminRequest);
        assertEquals(Role.ADMIN, adminResult.getBody().getRole());
    }
}