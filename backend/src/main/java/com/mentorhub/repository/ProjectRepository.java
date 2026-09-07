package com.mentorhub.repository;

import com.mentorhub.entity.Project;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * ProjectRepository - handles database queries for projects.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find all projects belonging to a specific student.
     * Used on the student's profile page.
     */
    List<Project> findByStudent(User student);

    /**
     * Find all projects with a specific verification status.
     * Used on the teacher's dashboard to show pending projects.
     */
    List<Project> findByVerificationStatus(String status);

    /**
     * Count projects by student and verification status.
     * Used for dashboard summary cards.
     */
    long countByStudentAndVerificationStatus(User student, String status);
}
