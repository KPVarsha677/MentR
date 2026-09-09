package com.mentorhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * StudentProfileRequest DTO - data sent by student to update their profile.
 *
 * Academic and contact fields are required — a student profile isn't
 * useful to a teacher half-filled-in. careerGoal/about stay optional since
 * they're free-form bio text, not identifying information.
 */
@Data
public class StudentProfileRequest {

    @NotBlank(message = "Register number is required")
    private String registerNumber;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotBlank(message = "Section is required")
    private String section;

    @NotBlank(message = "Batch is required")
    private String batch;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "LinkedIn URL is required")
    private String linkedinUrl;

    @NotBlank(message = "GitHub URL is required")
    private String githubUrl;

    private String careerGoal;
    private String about;
}
