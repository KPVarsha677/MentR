package com.mentorhub.repository;

import com.mentorhub.entity.Internship;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * InternshipRepository - handles database queries for internships.
 */
@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    List<Internship> findByStudent(User student);
    List<Internship> findByVerificationStatus(String status);
    long countByStudentAndVerificationStatus(User student, String status);
}
