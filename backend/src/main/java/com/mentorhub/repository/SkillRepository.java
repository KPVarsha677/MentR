package com.mentorhub.repository;

import com.mentorhub.entity.Skill;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * SkillRepository - handles database queries for skills.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByStudent(User student);
    List<Skill> findByVerificationStatus(String status);
    long countByStudentAndVerificationStatus(User student, String status);
}
