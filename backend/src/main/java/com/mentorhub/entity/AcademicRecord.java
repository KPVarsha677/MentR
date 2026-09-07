package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AcademicRecord Entity - stores per-subject marks for a student's academic record.
 *
 * Each record represents one subject in one semester.
 * Students can add/edit/delete records for each of the 8 semesters.
 *
 * DATABASE TABLE: academic_records
 */
@Entity
@Table(name = "academic_records")
@Data
public class AcademicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Semester number (1 through 8)
    @Column(nullable = false)
    private Integer semester;

    // Subject name (mandatory)
    @Column(nullable = false)
    private String subjectName;

    // Subject code (mandatory)
    @Column(nullable = false)
    private String subjectCode;

    // Unit Test marks (0–100)
    private Integer unitTest1;
    private Integer unitTest2;

    // CAT (Continuous Assessment Test) marks (0–100)
    private Integer cat1;
    private Integer cat2;

    // Semester examination marks (0–100)
    private Integer semesterExam;

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
