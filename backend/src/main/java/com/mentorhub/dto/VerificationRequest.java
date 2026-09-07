package com.mentorhub.dto;

import lombok.Data;

/**
 * VerificationRequest DTO - data sent by a teacher to approve/reject an item.
 *
 * WHY THIS EXISTS:
 * When a teacher reviews a project, certificate, etc., they need to:
 * 1. Set a status: "APPROVED" or "REJECTED"
 * 2. Optionally leave a comment explaining the decision
 *
 * This DTO carries that decision from the frontend to the backend.
 */
@Data
public class VerificationRequest {

    // Either "APPROVED" or "REJECTED"
    private String status;

    // Optional comment explaining the decision
    private String comment;
}
