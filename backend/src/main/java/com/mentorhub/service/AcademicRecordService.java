package com.mentorhub.service;

import com.mentorhub.dto.AcademicRecordRequest;
import com.mentorhub.entity.AcademicRecord;
import com.mentorhub.entity.User;
import com.mentorhub.repository.AcademicRecordRepository;
import com.mentorhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AcademicRecordService - handles all academic marks CRUD operations for students.
 */
@Service
public class AcademicRecordService {

    @Autowired
    private AcademicRecordRepository academicRecordRepository;

    @Autowired
    private UserRepository userRepository;

    /** Get all records for a specific semester for the given student. */
    public List<AcademicRecord> getSemesterRecords(Long userId, Integer semester) {
        User student = getUser(userId);
        return academicRecordRepository.findByStudentAndSemesterOrderBySubjectNameAsc(student, semester);
    }

    /** Get all records across all semesters. */
    public List<AcademicRecord> getAllRecords(Long userId) {
        User student = getUser(userId);
        return academicRecordRepository.findAllByStudentOrderBySemesterAndSubjectName(student);
    }

    /** Add a new subject record. */
    public AcademicRecord addRecord(Long userId, AcademicRecordRequest request) {
        User student = getUser(userId);

        AcademicRecord record = new AcademicRecord();
        record.setStudent(student);
        mapFields(record, request);

        return academicRecordRepository.save(record);
    }

    /** Update an existing subject record. */
    public AcademicRecord updateRecord(Long userId, Long recordId, AcademicRecordRequest request) {
        AcademicRecord record = academicRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Academic record not found"));

        if (!record.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to edit this record");
        }

        mapFields(record, request);
        return academicRecordRepository.save(record);
    }

    /** Delete a subject record. */
    public void deleteRecord(Long userId, Long recordId) {
        AcademicRecord record = academicRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Academic record not found"));

        if (!record.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this record");
        }

        academicRecordRepository.delete(record);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void mapFields(AcademicRecord record, AcademicRecordRequest req) {
        record.setSemester(req.getSemester());
        record.setSubjectName(req.getSubjectName());
        record.setSubjectCode(req.getSubjectCode());
        record.setUnitTest1(req.getUnitTest1());
        record.setUnitTest2(req.getUnitTest2());
        record.setCat1(req.getCat1());
        record.setCat2(req.getCat2());
        record.setSemesterExam(req.getSemesterExam());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}
