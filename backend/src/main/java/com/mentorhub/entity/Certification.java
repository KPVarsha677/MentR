package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Certification Entity - represents a certificate earned by a student.
 *
 * WHY THIS TABLE EXISTS:
 * Students earn certifications from online platforms like Coursera, NPTEL, AWS, etc.
 * These need to be recorded and verified by their mentor teacher.
 *
 * DATABASE TABLE: certifications
 */
@Entity
@Table(name = "certifications")
@Data
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Name of the certification (e.g., "AWS Cloud Practitioner")
    @Column(nullable = false)
    private String name;

    // Organization that issued this certificate (e.g., "Amazon Web Services")
    private String issuingOrganization;

    // Date when the certificate was issued (stored as a string for simplicity)
    private String issueDate;

    // Date when the certificate expires (optional)
    private String expirationDate;

    // URL to view/verify the certificate online
    private String credentialUrl;

    // Unique ID of the credential (provided by the issuing organization)
    private String credentialId;

    @Column(nullable = false)
    private String verificationStatus = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String teacherComment;

    private LocalDateTime verifiedAt;
    private Long verifiedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
