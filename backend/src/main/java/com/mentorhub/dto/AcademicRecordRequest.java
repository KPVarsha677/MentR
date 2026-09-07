package com.mentorhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AcademicRecordRequest DTO - data sent when a student adds or updates a subject mark record.
 */
@Data
public class AcademicRecordRequest {

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be between 1 and 8")
    @Max(value = 8, message = "Semester must be between 1 and 8")
    private Integer semester;

    @NotBlank(message = "Subject name is required")
    private String subjectName;

    @NotBlank(message = "Subject code is required")
    private String subjectCode;

    @Min(value = 0, message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Integer unitTest1;

    @Min(value = 0, message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Integer unitTest2;

    @Min(value = 0, message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Integer cat1;

    @Min(value = 0, message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Integer cat2;

    @Min(value = 0, message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Integer semesterExam;
}
