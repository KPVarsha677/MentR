package com.mentorhub.repository;

import com.mentorhub.entity.StudentProfile;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * StudentProfileRepository - handles queries for student profile data.
 */
@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    /**
     * Find a student's profile by their user account.
     * Used when a student opens their profile page.
     */
    Optional<StudentProfile> findByUser(User user);

    /**
     * Search students by name, register number, department, year, or section.
     * This is the core search feature for teachers.
     *
     * @Query uses JPQL (Java Persistence Query Language) - similar to SQL but uses entity names.
     * The "%" wildcards allow partial matches, e.g., searching "john" finds "John Doe".
     * LOWER() makes the search case-insensitive.
     *
     * WHY JPQL instead of plain SQL:
     * JPQL works with entity objects. It is database-independent and
     * Spring Data JPA translates it to the appropriate SQL dialect automatically.
     */
    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:registerNumber IS NULL OR LOWER(sp.registerNumber) LIKE LOWER(CONCAT('%', :registerNumber, '%'))) AND " +
           "(:department IS NULL OR LOWER(sp.department) LIKE LOWER(CONCAT('%', :department, '%'))) AND " +
           "(:year IS NULL OR sp.year = :year) AND " +
           "(:section IS NULL OR LOWER(sp.section) LIKE LOWER(CONCAT('%', :section, '%')))")
    List<StudentProfile> searchStudents(String name, String registerNumber,
                                        String department, Integer year, String section);
}
