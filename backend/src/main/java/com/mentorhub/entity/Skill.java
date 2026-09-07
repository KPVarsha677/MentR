package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Skill Entity - represents a technical or soft skill a student has.
 *
 * WHY THIS TABLE EXISTS:
 * Recruiters and mentors want to know what skills a student has.
 * Skills are stored separately so students can add multiple skills easily.
 *
 * DATABASE TABLE: skills
 */
@Entity
@Table(name = "skills")
@Data
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Name of the skill (e.g., "Java", "React", "Problem Solving")
    @Column(nullable = false)
    private String name;

    // Category of the skill (e.g., "Programming", "Soft Skills", "Tools")
    private String category;

    // Proficiency level (e.g., "Beginner", "Intermediate", "Advanced")
    private String proficiencyLevel;

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
