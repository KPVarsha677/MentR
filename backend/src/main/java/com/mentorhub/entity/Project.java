package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Project Entity - represents a project done by a student.
 *
 * WHY THIS TABLE EXISTS:
 * Students need to show projects they have built or contributed to.
 * This is one section of their academic portfolio.
 * Each project belongs to one student.
 *
 * RELATIONSHIPS:
 * - @ManyToOne with User (student): one student can have many projects
 *
 * DATABASE TABLE: projects
 */
@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne: Many projects belong to one student.
     * This gives us a "student_id" foreign key column in the projects table.
     */
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Project name (e.g., "Library Management System")
    @Column(nullable = false)
    private String title;

    // Detailed description of the project
    @Column(columnDefinition = "TEXT")
    private String description;

    // Technologies used (e.g., "Java, Spring Boot, MySQL")
    private String techStack;

    // GitHub or deployment link for the project
    private String projectUrl;

    // When the project was started
    private String startDate;

    // When the project was completed
    private String endDate;

    // Verification status: "PENDING", "APPROVED", "REJECTED"
    // All new submissions default to PENDING
    @Column(nullable = false)
    private String verificationStatus = "PENDING";

    // Comment left by the teacher during verification
    @Column(columnDefinition = "TEXT")
    private String teacherComment;

    // When the teacher verified this project
    private LocalDateTime verifiedAt;

    // Who verified it (teacher's user ID)
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
