package com.mentorhub.controller;

import com.mentorhub.dto.AcademicRecordRequest;
import com.mentorhub.entity.AcademicRecord;
import com.mentorhub.security.SecurityUtils;
import com.mentorhub.service.AcademicRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AcademicRecordController - REST API for student academic marks management.
 *
 * All endpoints require ROLE_STUDENT, and {userId} must match the caller's
 * own authenticated id (SecurityUtils.requireSelf) — otherwise any student
 * could read or edit another student's grades just by changing the id in
 * the URL.
 * Base path: /api/student/{userId}/academic-records
 *
 * GET    /api/student/{userId}/academic-records           → all records
 * GET    /api/student/{userId}/academic-records?semester=N → records for one semester
 * POST   /api/student/{userId}/academic-records           → add a subject record
 * PUT    /api/student/{userId}/academic-records/{id}      → update a record
 * DELETE /api/student/{userId}/academic-records/{id}      → delete a record
 */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class AcademicRecordController {

    @Autowired
    private AcademicRecordService academicRecordService;

    @GetMapping("/{userId}/academic-records")
    public ResponseEntity<List<AcademicRecord>> getRecords(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer semester) {

        SecurityUtils.requireSelf(userId);
        if (semester != null) {
            return ResponseEntity.ok(academicRecordService.getSemesterRecords(userId, semester));
        }
        return ResponseEntity.ok(academicRecordService.getAllRecords(userId));
    }

    @PostMapping("/{userId}/academic-records")
    public ResponseEntity<AcademicRecord> addRecord(
            @PathVariable Long userId,
            @Valid @RequestBody AcademicRecordRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(academicRecordService.addRecord(userId, request));
    }

    @PutMapping("/{userId}/academic-records/{recordId}")
    public ResponseEntity<AcademicRecord> updateRecord(
            @PathVariable Long userId,
            @PathVariable Long recordId,
            @Valid @RequestBody AcademicRecordRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(academicRecordService.updateRecord(userId, recordId, request));
    }

    @DeleteMapping("/{userId}/academic-records/{recordId}")
    public ResponseEntity<Map<String, String>> deleteRecord(
            @PathVariable Long userId,
            @PathVariable Long recordId) {
        SecurityUtils.requireSelf(userId);
        academicRecordService.deleteRecord(userId, recordId);
        return ResponseEntity.ok(Map.of("message", "Record deleted"));
    }
}
