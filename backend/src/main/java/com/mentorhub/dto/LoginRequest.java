package com.mentorhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LoginRequest DTO - carries login credentials from the frontend.
 *
 * Simple two-field DTO: email + password.
 * On successful login, the server returns a LoginResponse with the JWT token.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
