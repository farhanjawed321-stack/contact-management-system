package com.contactapp.backend.service;

import com.contactapp.backend.dto.*;
import com.contactapp.backend.entity.User;
import com.contactapp.backend.exception.AppException;
import com.contactapp.backend.repository.UserRepository;
import com.contactapp.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log =
        LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (request.getEmail() == null && request.getPhone() == null) {
            throw new AppException(
                "Email or phone number is required",
                HttpStatus.BAD_REQUEST);
        }

        if (request.getEmail() != null &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                "Email already registered", HttpStatus.CONFLICT);
        }

        if (request.getPhone() != null &&
            userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(
                "Phone already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getEmail());

        String identifier = request.getEmail() != null ?
            request.getEmail() : request.getPhone();
        return jwtUtil.generateToken(identifier);
    }

    public String login(LoginRequest request) {
        log.info("Login attempt: {}", request.getIdentifier());

        User user = userRepository
            .findByEmail(request.getIdentifier())
            .orElseGet(() -> userRepository
                .findByPhone(request.getIdentifier())
                .orElseThrow(() -> new AppException(
                    "User not found", HttpStatus.NOT_FOUND)));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new AppException(
                "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        log.info("Login successful: {}", request.getIdentifier());
        String identifier = user.getEmail() != null ?
            user.getEmail() : user.getPhone();
        return jwtUtil.generateToken(identifier);
    }

    public void changePassword(String email,
                               ChangePasswordRequest request) {
        log.info("Password change for: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException(
                "User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(
                "Current password is incorrect",
                HttpStatus.BAD_REQUEST);
        }

        user.setPassword(
            passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for: {}", email);
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException(
                "User not found", HttpStatus.NOT_FOUND));

        return UserResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .build();
    }
}