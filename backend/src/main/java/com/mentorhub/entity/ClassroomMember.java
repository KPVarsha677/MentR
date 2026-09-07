package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ClassroomMember Entity - tracks which students are in which classroom.
 *
 * WHY THIS TABLE EXISTS:
 * A student can join multiple classrooms (e.g., one for each subject/year).
 * A classroom can have many students.
 * This is a Many-to-Many relationship.
 * Instead of using @ManyToMany (which creates a hidden table), we create
 * this explicit join table so we can add extra columns like joinedAt.
 *
 * RELATIONSHIPS:
 * - @ManyToOne with Classroom: many memberships belong to one classroom
 * - @ManyToOne with User (student): many memberships belong to one student
 *
 * DATABASE TABLE: classroom_members
 */
@Entity
@Table(name = "classroom_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "student_id"}))
@Data
public class ClassroomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Which classroom this membership is for.
     * @ManyToOne: Many memberships → one classroom
     *
     * @JsonIgnoreProperties: stops Jackson from recursing into Classroom's list of members
     * (if any back-reference existed), preventing infinite serialization loops.
     */
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /**
     * Which student this membership belongs to.
     * @ManyToOne: Many memberships → one student
     *
     * @JsonIgnoreProperties: stops Jackson from recursing into any back-references on User.
     */
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // When the student joined this classroom
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}
