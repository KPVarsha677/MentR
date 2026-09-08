package com.mentorhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RegisterRequest DTO - carries registration data from the frontend to the backend.
 *
 * WHY USE A DTO INSTEAD OF THE ENTITY DIRECTLY?
 * The User entity is a database model — it maps directly to a database table.
 * If we expose it directly to the API, we expose database details.
 * Also, the frontend might send different fields than what the entity needs.
 * DTOs are a clean boundary between the API layer and the database layer.
 *
 * VALIDATION ANNOTATIONS:
 * @NotBlank: field must not be null or empty string
 * @Email: field must be a valid email format
 * @Size: field must have characters within the defined range
 *
 * These run automatically when @Valid is used on the controller method.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Role must be either "ROLE_TEACHER" or "ROLE_STUDENT"
    @NotBlank(message = "Role is required")
    private String role;

    // Required only when role = ROLE_TEACHER — checked against
    // app.teacher-invite-code in AuthService.register(). Without this,
    // anyone could self-register as a teacher and get full access to every
    // student's data and verification powers.
    private String teacherInviteCode;
}
