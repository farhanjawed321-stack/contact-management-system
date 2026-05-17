package com.contactapp.backend.service;

import com.contactapp.backend.dto.*;
import com.contactapp.backend.entity.User;
import com.contactapp.backend.exception.AppException;
import com.contactapp.backend.repository.UserRepository;
import com.contactapp.backend.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// @ExtendWith tells JUnit to use Mockito
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // @Mock creates a fake version of these classes
    // We don't want to use real database in tests!
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // @InjectMocks creates AuthService and injects
    // the mocks above into it automatically
    @InjectMocks
    private AuthService authService;

    // ─── REGISTER TESTS ──────────────────────────────────────────

    @Test
    @DisplayName("✅ Register - Success with email")
    void register_WithEmail_ShouldReturnToken() {
        // ARRANGE - set up the test data
        RegisterRequest request = new RegisterRequest(
            "Farhan", "Jawed",
            "farhan@test.com", null, "password123");

        // Tell mock what to return when called
        when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(new User());
        when(jwtUtil.generateToken(anyString()))
            .thenReturn("fake.jwt.token");

        // ACT - call the method we're testing
        String token = authService.register(request);

        // ASSERT - check the result
        assertNotNull(token);
        assertEquals("fake.jwt.token", token);

        // Verify save was called exactly once
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    @DisplayName("✅ Register - Success with phone")
    void register_WithPhone_ShouldReturnToken() {
        // ARRANGE
        RegisterRequest request = new RegisterRequest(
            "Farhan", "Jawed",
            null, "+923001234567", "password123");

        when(userRepository.existsByPhone(anyString()))
            .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(new User());
        when(jwtUtil.generateToken(anyString()))
            .thenReturn("fake.jwt.token");

        // ACT
        String token = authService.register(request);

        // ASSERT
        assertNotNull(token);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("❌ Register - Email already exists")
    void register_EmailAlreadyExists_ShouldThrowException() {
        // ARRANGE
        RegisterRequest request = new RegisterRequest(
            "Farhan", "Jawed",
            "farhan@test.com", null, "password123");

        // Mock returns true = email already in use
        when(userRepository.existsByEmail(anyString()))
            .thenReturn(true);

        // ASSERT - expect exception to be thrown
        AppException exception = assertThrows(
            AppException.class,
            () -> authService.register(request));

        assertEquals("Email already registered",
            exception.getMessage());

        // Verify save was NEVER called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("❌ Register - No email or phone provided")
    void register_NoEmailOrPhone_ShouldThrowException() {
        // ARRANGE - both email and phone are null
        RegisterRequest request = new RegisterRequest(
            "Farhan", "Jawed",
            null, null, "password123");

        // ASSERT
        AppException exception = assertThrows(
            AppException.class,
            () -> authService.register(request));

        assertEquals("Email or phone number is required",
            exception.getMessage());
    }

    // ─── LOGIN TESTS ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ Login - Success")
    void login_ValidCredentials_ShouldReturnToken() {
        // ARRANGE
        LoginRequest request = new LoginRequest(
            "farhan@test.com", "password123");

        User mockUser = User.builder()
            .id(1L)
            .email("farhan@test.com")
            .password("encodedPassword")
            .build();

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString()))
            .thenReturn(true);
        when(jwtUtil.generateToken(anyString()))
            .thenReturn("fake.jwt.token");

        // ACT
        String token = authService.login(request);

        // ASSERT
        assertNotNull(token);
        assertEquals("fake.jwt.token", token);
        verify(jwtUtil, times(1)).generateToken(anyString());
    }

    @Test
    @DisplayName("❌ Login - User not found")
    void login_UserNotFound_ShouldThrowException() {
        // ARRANGE
        LoginRequest request = new LoginRequest(
            "notfound@test.com", "password123");

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString()))
            .thenReturn(Optional.empty());

        // ASSERT
        AppException exception = assertThrows(
            AppException.class,
            () -> authService.login(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("❌ Login - Wrong password")
    void login_WrongPassword_ShouldThrowException() {
        // ARRANGE
        LoginRequest request = new LoginRequest(
            "farhan@test.com", "wrongpassword");

        User mockUser = User.builder()
            .id(1L)
            .email("farhan@test.com")
            .password("encodedPassword")
            .build();

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        // Password doesn't match!
        when(passwordEncoder.matches(anyString(), anyString()))
            .thenReturn(false);

        // ASSERT
        AppException exception = assertThrows(
            AppException.class,
            () -> authService.login(request));

        assertEquals("Invalid credentials",
            exception.getMessage());
    }

    // ─── CHANGE PASSWORD TESTS ───────────────────────────────────

    @Test
    @DisplayName("✅ Change Password - Success")
    void changePassword_ValidRequest_ShouldSucceed() {
        // ARRANGE
        ChangePasswordRequest request =
            new ChangePasswordRequest("oldPass123", "newPass123");

        User mockUser = User.builder()
            .id(1L)
            .email("farhan@test.com")
            .password("encodedOldPassword")
            .build();

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString()))
            .thenReturn(true);
        when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(mockUser);

        // ACT - should not throw any exception
        assertDoesNotThrow(() ->
            authService.changePassword(
                "farhan@test.com", request));

        // Verify password was saved
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("❌ Change Password - Wrong current password")
    void changePassword_WrongCurrentPassword_ShouldThrow() {
        // ARRANGE
        ChangePasswordRequest request =
            new ChangePasswordRequest("wrongPass", "newPass123");

        User mockUser = User.builder()
            .id(1L)
            .email("farhan@test.com")
            .password("encodedPassword")
            .build();

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString()))
            .thenReturn(false);

        // ASSERT
        AppException exception = assertThrows(
            AppException.class,
            () -> authService.changePassword(
                "farhan@test.com", request));

        assertEquals("Current password is incorrect",
            exception.getMessage());
    }
}