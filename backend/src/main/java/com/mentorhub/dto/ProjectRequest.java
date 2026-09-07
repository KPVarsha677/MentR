package com.mentorhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ProjectRequest DTO - data sent when a student creates or updates a project.
 */
@Data
public class ProjectRequest {

    @NotBlank(message = "Project title is required")
    private String title;

    private String description;
    private String techStack;
    private String projectUrl;
    private String startDate;
    private String endDate;
}
