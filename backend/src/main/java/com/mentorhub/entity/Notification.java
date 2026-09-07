package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Notification Entity - stores notifications sent to teachers when students
 * update their portfolio items.
 *
 * DATABASE TABLE: notifications
 */
@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 500)
    private String message;

    private String itemType;

    private String action;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
