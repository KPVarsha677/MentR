package com.mentorhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AchievementRequest DTO - data sent when a student adds or updates an achievement.
 */
@Data
public class AchievementRequest {

    @NotBlank(message = "Achievement title is required")
    private String title;

    private String category;
    private String description;
    private String achievementDate;
    private String issuingOrganization;
}
