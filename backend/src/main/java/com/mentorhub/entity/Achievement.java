package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Achievement Entity - represents any achievement, award, or honor received by a student.
 *
 * WHY THIS TABLE EXISTS:
 * Students participate in hackathons, coding contests, sports, cultural events, etc.
 * These achievements need to be tracked and verified.
 *
 * DATABASE TABLE: achievements
 */
@Entity
@Table(name = "achievements")
@Data
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Title of the achievement (e.g., "First Place - Hackathon 2024")
    @Column(nullable = false)
    private String title;

    // Category (e.g., "Technical", "Sports", "Cultural", "Academic")
    private String category;

    // Description of what was achieved
    @Column(columnDefinition = "TEXT")
    private String description;

    // Date when the achievement was received
    private String achievementDate;

    // Name of the organization that gave this award
    private String issuingOrganization;

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
