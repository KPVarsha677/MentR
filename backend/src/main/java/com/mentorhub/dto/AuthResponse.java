package com.mentorhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse DTO - the response sent to the frontend after successful login.
 *
 * WHY THIS EXISTS:
 * After login, the frontend needs:
 * 1. The JWT token (to send with future API requests)
 * 2. User's basic info (to show in the UI)
 * 3. Role (to redirect to the right dashboard)
 *
 * @AllArgsConstructor - generates a constructor with all fields
 * @NoArgsConstructor - generates a no-argument constructor
 * These are provided by Lombok.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    // The JWT token the client must store and use for future requests
    private String token;

    // User's unique ID (useful for API calls like /api/students/{id}/profile)
    private Long userId;

    // User's display name
    private String name;

    // User's email
    private String email;

    // The user's role: "ROLE_TEACHER" or "ROLE_STUDENT"
    private String role;
}
