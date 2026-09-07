package com.mentorhub.repository;

import com.mentorhub.entity.Classroom;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * ClassroomRepository - handles database queries for the Classroom entity.
 *
 * WHY THIS EXISTS:
 * Teachers need to see classrooms they created.
 * Students need to find classrooms by joining code.
 * These custom methods handle these specific queries.
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    /**
     * Find all classrooms created by a specific teacher.
     * Used on the teacher's "My Classrooms" page.
     */
    List<Classroom> findByTeacher(User teacher);

    /**
     * Find a classroom by its unique join code.
     * Used when a student enters a join code to join a classroom.
     */
    Optional<Classroom> findByJoinCode(String joinCode);
}
