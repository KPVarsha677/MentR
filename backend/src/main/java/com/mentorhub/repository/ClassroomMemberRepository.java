package com.mentorhub.repository;

import com.mentorhub.entity.Classroom;
import com.mentorhub.entity.ClassroomMember;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * ClassroomMemberRepository - handles database queries for classroom memberships.
 *
 * WHY THIS EXISTS:
 * We need to know:
 * 1. Which students are in a specific classroom (for the teacher view)
 * 2. Which classrooms a specific student has joined (for the student view)
 * 3. Whether a student has already joined a classroom (to prevent duplicates)
 */
@Repository
public interface ClassroomMemberRepository extends JpaRepository<ClassroomMember, Long> {

    /**
     * Find all members (students) of a specific classroom.
     * Used by teachers to see their student list.
     */
    List<ClassroomMember> findByClassroom(Classroom classroom);

    /**
     * Find all classrooms a specific student has joined.
     * Used on the student's "My Classrooms" page.
     */
    List<ClassroomMember> findByStudent(User student);

    /**
     * Check if a student already joined a specific classroom.
     * Prevents a student from joining the same classroom twice.
     */
    boolean existsByClassroomAndStudent(Classroom classroom, User student);

    /**
     * Find the specific membership record for a student in a classroom.
     * Used when a student wants to leave a classroom.
     */
    Optional<ClassroomMember> findByClassroomAndStudent(Classroom classroom, User student);
}
