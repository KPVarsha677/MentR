package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

/**
 * StudentProfile Entity - stores student academic information.
 *
 * WHY THIS TABLE EXISTS:
 * Students have academic details like register number, department, year, section.
 * This data belongs to a separate table so the User table stays clean.
 *
 * RELATIONSHIPS:
 * - @OneToOne with User: one student has one user account
 * - @OneToMany with Project, Skill, Certification, etc. (in those entity classes)
 *
 * DATABASE TABLE: student_profiles
 */
@Entity
@Table(name = "student_profiles")
@Data
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @OneToOne with User
     * Each StudentProfile is linked to exactly one User account
     */
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Academic details
    private String registerNumber;  // e.g., "21CS001"
    private String department;       // e.g., "Computer Science"
    private Integer year;            // e.g., 2
    private String section;          // e.g., "A"
    private String batch;            // e.g., "2021-2025"

    // Personal details
    private String phone;
    private String address;
    private String linkedinUrl;
    private String githubUrl;

    // Career goal - short description of what the student wants to do
    private String careerGoal;

    // About section - brief bio
    @Column(columnDefinition = "TEXT")
    private String about;

    // Academic subject / specialization
    private String subject;
}
