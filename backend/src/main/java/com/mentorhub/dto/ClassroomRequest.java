package com.mentorhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ClassroomRequest DTO - data sent when creating or editing a classroom.
 */
@Data
public class ClassroomRequest {

    @NotBlank(message = "Classroom name is required")
    private String name;

    private String description;
    private String subject;
    private String academicYear;
}
