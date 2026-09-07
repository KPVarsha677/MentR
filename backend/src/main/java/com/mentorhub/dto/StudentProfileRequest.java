package com.mentorhub.dto;

import lombok.Data;

/**
 * StudentProfileRequest DTO - data sent by student to update their profile.
 * All fields are optional — student can update partial sections.
 */
@Data
public class StudentProfileRequest {
    private String registerNumber;
    private String department;
    private Integer year;
    private String section;
    private String batch;
    private String phone;
    private String address;
    private String linkedinUrl;
    private String githubUrl;
    private String careerGoal;
    private String about;
}
