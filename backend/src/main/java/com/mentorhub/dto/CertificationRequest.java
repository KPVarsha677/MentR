package com.mentorhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * CertificationRequest DTO - data sent when a student adds or updates a certification.
 */
@Data
public class CertificationRequest {

    @NotBlank(message = "Certification name is required")
    private String name;

    private String issuingOrganization;
    private String issueDate;
    private String expirationDate;
    private String credentialUrl;
    private String credentialId;
}
