package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

/**
 * TeacherProfile Entity - stores teacher-specific information.
 *
 * WHY THIS TABLE EXISTS:
 * We separate the teacher's academic/professional details from login data.
 * This keeps the User table clean and focused on authentication.
 *
 * RELATIONSHIPS:
 * - @OneToOne with User: one teacher has one user account
 * - @OneToMany with Classroom: one teacher can create many classrooms
 *
 * DATABASE TABLE: teacher_profiles
 */
@Entity
@Table(name = "teacher_profiles")
@Data
public class TeacherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @OneToOne: One TeacherProfile belongs to exactly one User.
     * @JoinColumn: The teacher_profiles table will have a column "user_id"
     * that stores a foreign key pointing to the users table.
     */
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Department where the teacher works (e.g., "Computer Science")
    private String department;

    // Teacher's employee ID or staff number
    private String employeeId;

    // Teacher's phone number
    private String phone;

    // Teacher's designation (e.g., "Assistant Professor")
    private String designation;

    // Teacher's subject specialization
    private String subject;

    // About / bio
    @Column(columnDefinition = "TEXT")
    private String about;
}
