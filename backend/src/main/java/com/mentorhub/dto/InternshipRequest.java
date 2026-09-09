package com.mentorhub.dto;

import jakarta.validation.constraints.AssertTrue;
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

    @NotBlank(message = "Start date is required")
    private String startDate;

    private String endDate;
    private boolean ongoing;
    private String location;
    private String stipend;

    // End date is required unless the internship is marked "Currently
    // ongoing" — you can't have an end date for something still in progress.
    @AssertTrue(message = "End date is required unless the internship is currently ongoing")
    private boolean isEndDateValid() {
        return ongoing || (endDate != null && !endDate.isBlank());
    }
}
