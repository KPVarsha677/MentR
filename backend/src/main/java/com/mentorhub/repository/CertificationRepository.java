package com.mentorhub.repository;

import com.mentorhub.entity.Certification;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * CertificationRepository - handles database queries for certifications.
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    List<Certification> findByStudent(User student);
    List<Certification> findByVerificationStatus(String status);
    long countByStudentAndVerificationStatus(User student, String status);
}
