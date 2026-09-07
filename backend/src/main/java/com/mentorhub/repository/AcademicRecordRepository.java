package com.mentorhub.repository;

import com.mentorhub.entity.AcademicRecord;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    /**
     * Find all records for a specific semester for a student, ordered by subject name.
     */
    List<AcademicRecord> findByStudentAndSemesterOrderBySubjectNameAsc(User student, Integer semester);

    /**
     * Find all records across all semesters for a student,
     * ordered by semester then subject name.
     *
     * Using @Query to avoid derived-query parsing ambiguity with
     * multi-property ORDER BY in Spring Data method names.
     */
    @Query("SELECT ar FROM AcademicRecord ar WHERE ar.student = :student " +
           "ORDER BY ar.semester ASC, ar.subjectName ASC")
    List<AcademicRecord> findAllByStudentOrderBySemesterAndSubjectName(User student);
}
