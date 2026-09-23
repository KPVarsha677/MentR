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
     *
     * WHY EVERY OCCURRENCE OF EVERY NULLABLE PARAMETER IS WRAPPED IN CAST(... AS ...):
     * Each named parameter here is written once in JPQL but appears TWICE in the query
     * text — once in its "IS NULL" check, once inside the LIKE/equality comparison.
     * Hibernate 6 translates each occurrence to its own native "?" bind position, and
     * against real PostgreSQL, a bind position gets no usable type from being compared
     * only to "IS NULL", and (perhaps more surprisingly) also gets no usable type from
     * appearing only inside a '%' || ? || '%' concatenation — PostgreSQL's parameter
     * type inference does not treat either position as forcing text, and it silently
     * falls back to "bytea" for the parameter's type instead of raising a signature
     * error at bind time. That produces, at query time:
     *   "ERROR: function lower(bytea) does not exist"
     * This broke EVERY call to this method in production — including calls with every
     * filter left null — and was silently swallowed by the frontend, which looked like
     * "search doesn't work" / "no student found" even though the students exist.
     * H2 does not reproduce this (it doesn't use PostgreSQL's wire protocol), which is
     * why it was missed before; see StudentProfileRepositorySearchTest, which reproduces
     * it against a real embedded PostgreSQL instance and verifies this fix. Casting
     * EVERY occurrence — both the "IS NULL" check and the value fed into
     * LIKE/CONCAT/equality — gives PostgreSQL an explicit type everywhere so it never
     * has to guess.
     */
    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u WHERE " +
           "(CAST(:name AS string) IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
           "(CAST(:registerNumber AS string) IS NULL OR LOWER(sp.registerNumber) LIKE LOWER(CONCAT('%', CAST(:registerNumber AS string), '%'))) AND " +
           "(CAST(:department AS string) IS NULL OR LOWER(sp.department) LIKE LOWER(CONCAT('%', CAST(:department AS string), '%'))) AND " +
           "(CAST(:year AS integer) IS NULL OR sp.year = :year) AND " +
           "(CAST(:section AS string) IS NULL OR LOWER(sp.section) LIKE LOWER(CONCAT('%', CAST(:section AS string), '%')))")
    List<StudentProfile> searchStudents(String name, String registerNumber,
                                        String department, Integer year, String section);
}
