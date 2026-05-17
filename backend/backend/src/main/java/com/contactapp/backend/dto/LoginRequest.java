package com.contactapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email or phone is required")
    private String identifier; // can be email or phone

    @NotBlank(message = "Password is required")
    private String password;
}