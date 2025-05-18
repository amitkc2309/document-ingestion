package com.di.integration;

import com.di.dto.AuthResponse;
import com.di.dto.RegisterRequest;
import com.di.model.Role;
import com.di.model.User;
import com.di.repository.UserRepository;
import com.di.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

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

        AuthResponse adminResponse = authService.register(adminRequest);
        assertEquals(Role.ADMIN, adminResponse.getRole());

        // Verify user was saved with correct role
        User adminUser = userRepository.findByUsername("adminuser").orElse(null);
        assertNotNull(adminUser);
        assertEquals(Role.ADMIN, adminUser.getRole());

        // Test for EDITOR role
        RegisterRequest editorRequest = RegisterRequest.builder()
                .username("editoruser")
                .password("password")
                .email("editor@example.com")
                .fullName("Editor User")
                .role(Role.EDITOR)
                .build();

        AuthResponse editorResponse = authService.register(editorRequest);
        assertEquals(Role.EDITOR, editorResponse.getRole());

        // Verify user was saved with correct role
        User editorUser = userRepository.findByUsername("editoruser").orElse(null);
        assertNotNull(editorUser);
        assertEquals(Role.EDITOR, editorUser.getRole());

        // Test for VIEWER role
        RegisterRequest viewerRequest = RegisterRequest.builder()
                .username("vieweruser")
                .password("password")
                .email("viewer@example.com")
                .fullName("Viewer User")
                .role(Role.VIEWER)
                .build();

        AuthResponse viewerResponse = authService.register(viewerRequest);
        assertEquals(Role.VIEWER, viewerResponse.getRole());

        // Verify user was saved with correct role
        User viewerUser = userRepository.findByUsername("vieweruser").orElse(null);
        assertNotNull(viewerUser);
        assertEquals(Role.VIEWER, viewerUser.getRole());

        // Test default role (VIEWER)
        RegisterRequest defaultRequest = RegisterRequest.builder()
                .username("defaultuser")
                .password("password")
                .email("default@example.com")
                .fullName("Default User")
                .build(); // No role specified, should default to VIEWER

        AuthResponse defaultResponse = authService.register(defaultRequest);
        assertEquals(Role.VIEWER, defaultResponse.getRole());

        // Verify user was saved with default role
        User defaultUser = userRepository.findByUsername("defaultuser").orElse(null);
        assertNotNull(defaultUser);
        assertEquals(Role.VIEWER, defaultUser.getRole());
    }
}