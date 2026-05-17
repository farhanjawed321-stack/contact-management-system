package com.contactapp.backend.controller;

import com.contactapp.backend.dto.ChangePasswordRequest;
import com.contactapp.backend.dto.LoginRequest;
import com.contactapp.backend.dto.RegisterRequest;
import com.contactapp.backend.dto.UserResponse;
import com.contactapp.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("✅ register returns token response")
    void register_returnsTokenResponse() {
        RegisterRequest request = new RegisterRequest(
            "Jane", "Doe", "jane@example.com", null, "password123");

        when(authService.register(request)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.register(request);

        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("token")).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("✅ changePassword invokes service and returns success")
    void changePassword_invokesServiceAndReturnsSuccess() {
        UserDetails userDetails = User.withUsername("jane@example.com")
            .password("irrelevant")
            .authorities("ROLE_USER")
            .build();
        ChangePasswordRequest request = new ChangePasswordRequest("old-pass", "new-pass123");

        ResponseEntity<?> response = authController.changePassword(userDetails, request);

        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("message")).isEqualTo("Password changed successfully");
        verify(authService).changePassword("jane@example.com", request);
    }

    @Test
    @DisplayName("✅ Login - Success")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest(
            "farhan@test.com", "password123");

        when(authService.login(request)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("token")).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("✅ getProfile returns user response")
    void getProfile_returnsUserResponse() {
        UserDetails userDetails = User.withUsername("jane@example.com")
            .password("irrelevant")
            .authorities("ROLE_USER")
            .build();
        UserResponse expected = UserResponse.builder()
            .id(1L)
            .email("jane@example.com")
            .firstName("Jane")
            .lastName("Doe")
            .build();

        when(authService.getProfile("jane@example.com")).thenReturn(expected);

        ResponseEntity<?> response = authController.getProfile(userDetails);

        assertThat(response.getBody()).isEqualTo(expected);
    }
}