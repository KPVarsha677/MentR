package com.mentorhub.dto;

import lombok.Data;

/**
 * TeacherProfileRequest DTO - data sent by teacher to update their profile.
 */
@Data
public class TeacherProfileRequest {
    private String department;
    private String employeeId;
    private String phone;
    private String designation;
    private String subject;
    private String about;
}
