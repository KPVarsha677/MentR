package com.mentorhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * InternshipRequest DTO - data sent when a student adds or updates an internship.
 */
@Data
public class InternshipRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String role;
    private String description;
    private String startDate;
    private String endDate;
    private boolean ongoing;
    private String location;
    private String stipend;
}
