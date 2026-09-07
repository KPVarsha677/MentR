package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Classroom Entity - represents a mentor classroom created by a teacher.
 *
 * WHY THIS TABLE EXISTS:
 * A classroom is the core unit of this application.
 * Teachers create classrooms, students join them.
 * This table stores all classroom data including the unique join code.
 *
 * RELATIONSHIPS:
 * - @ManyToOne with User (teacher): many classrooms can belong to one teacher
 * - @OneToMany with ClassroomMember: one classroom has many student members
 *
 * DATABASE TABLE: classrooms
 */
@Entity
@Table(name = "classrooms")
@Data
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the classroom (e.g., "CS 2nd Year - Section A")
    @Column(nullable = false)
    private String name;

    // Description of the classroom
    private String description;

    // Subject or course this classroom is for
    private String subject;

    // Academic year (e.g., "2024-2025")
    private String academicYear;

    /**
     * @ManyToOne: Many classrooms can be created by one teacher.
     * @JoinColumn: The classrooms table stores "teacher_id" as a foreign key.
     *
     * WHY: We need to know which teacher owns each classroom.
     */
    // @JsonIgnoreProperties: when Jackson serializes this Classroom object and reaches
    // the nested 'teacher' (User), only serialize safe fields — never recurse back into
    // classrooms, profiles, or any other relationship that could cause infinite recursion.
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    /**
     * joinCode - a short random code (e.g., "ABC123") that students use to join.
     * unique = true means each classroom has a globally unique code.
     *
     * WHY: Instead of sharing a link, teachers share this code.
     * Students enter the code and get added to the classroom.
     */
    @Column(nullable = false, unique = true)
    private String joinCode;

    // Whether the classroom is currently accepting new students
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
