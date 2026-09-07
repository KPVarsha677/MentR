package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Internship Entity - represents an internship experience of a student.
 *
 * WHY THIS TABLE EXISTS:
 * Internships are an important part of a student's academic portfolio.
 * Teachers verify that students actually did internships.
 * This table stores all internship records.
 *
 * DATABASE TABLE: internships
 */
@Entity
@Table(name = "internships")
@Data
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Company where the student did the internship
    @Column(nullable = false)
    private String companyName;

    // Job title or role (e.g., "Software Development Intern")
    private String role;

    // Brief description of what the student worked on
    @Column(columnDefinition = "TEXT")
    private String description;

    // When the internship started
    private String startDate;

    // When the internship ended
    private String endDate;

    // Whether the internship is currently ongoing
    private boolean ongoing = false;

    // Location (e.g., "Bangalore, India" or "Remote")
    private String location;

    // Stipend per month (optional)
    private String stipend;

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
