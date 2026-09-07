package com.mentorhub.repository;

import com.mentorhub.entity.Achievement;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * AchievementRepository - handles database queries for achievements.
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByStudent(User student);
    List<Achievement> findByVerificationStatus(String status);
    long countByStudentAndVerificationStatus(User student, String status);
}
