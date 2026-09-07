package com.mentorhub.repository;

import com.mentorhub.entity.TeacherProfile;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * TeacherProfileRepository - handles queries for teacher profile data.
 */
@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {

    /**
     * Find a teacher's profile by their user account.
     */
    Optional<TeacherProfile> findByUser(User user);
}
