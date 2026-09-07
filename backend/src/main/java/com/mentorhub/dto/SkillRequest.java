package com.mentorhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SkillRequest DTO - data sent when a student adds or updates a skill.
 */
@Data
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    private String name;

    private String category;
    private String proficiencyLevel;
}
