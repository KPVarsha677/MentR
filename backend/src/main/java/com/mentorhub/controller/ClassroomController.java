package com.mentorhub.controller;

import com.mentorhub.dto.ClassroomRequest;
import com.mentorhub.entity.Classroom;
import com.mentorhub.entity.ClassroomMember;
import com.mentorhub.security.SecurityUtils;
import com.mentorhub.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * ClassroomController - REST API for classroom management.
 *
 * TEACHER ENDPOINTS:
 * POST   /api/classrooms             → create classroom
 * PUT    /api/classrooms/{id}        → edit classroom
 * DELETE /api/classrooms/{id}        → delete classroom
 * GET    /api/classrooms/teacher/{id}→ get teacher's classrooms
 * GET    /api/classrooms/{id}/members→ get students in a classroom
 *
 * STUDENT ENDPOINTS:
 * POST   /api/classrooms/join        → join classroom by code
 * GET    /api/classrooms/student/{id}→ get student's classrooms
 * DELETE /api/classrooms/{id}/leave  → leave classroom
 *
 * @PreAuthorize("hasRole('TEACHER')") - only teachers can call this endpoint
 * @PreAuthorize("hasRole('STUDENT')") - only students can call this endpoint
 *
 * Every teacherId/studentId here denotes "the acting user" and is checked
 * against the caller's own authenticated id via SecurityUtils.requireSelf.
 * Without that, the ownership check inside ClassroomService (which compares
 * classroom.getTeacher().getId() against this same client-supplied teacherId)
 * is trivially bypassed — an attacker just supplies the real owner's id
 * instead of their own to pass it, since nothing tied the check to who was
 * actually logged in.
 */
@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    // ========== TEACHER ENDPOINTS ==========

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Classroom> createClassroom(@Valid @RequestBody ClassroomRequest request,
                                                      @RequestParam Long teacherId) {
        SecurityUtils.requireSelf(teacherId);
        return ResponseEntity.ok(classroomService.createClassroom(request, teacherId));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<Classroom>> getTeacherClassrooms(@PathVariable Long teacherId) {
        SecurityUtils.requireSelf(teacherId);
        return ResponseEntity.ok(classroomService.getTeacherClassrooms(teacherId));
    }

    @PutMapping("/{classroomId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Classroom> editClassroom(@PathVariable Long classroomId,
                                                    @Valid @RequestBody ClassroomRequest request,
                                                    @RequestParam Long teacherId) {
        SecurityUtils.requireSelf(teacherId);
        return ResponseEntity.ok(classroomService.editClassroom(classroomId, request, teacherId));
    }

    @DeleteMapping("/{classroomId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> deleteClassroom(@PathVariable Long classroomId,
                                                               @RequestParam Long teacherId) {
        SecurityUtils.requireSelf(teacherId);
        classroomService.deleteClassroom(classroomId, teacherId);
        return ResponseEntity.ok(Map.of("message", "Classroom deleted successfully"));
    }

    @GetMapping("/{classroomId}/members")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassroomMember>> getClassroomStudents(@PathVariable Long classroomId) {
        return ResponseEntity.ok(classroomService.getClassroomStudents(classroomId, SecurityUtils.getCurrentUserId()));
    }

    // ========== STUDENT ENDPOINTS ==========

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClassroomMember> joinClassroom(@RequestParam String joinCode,
                                                          @RequestParam Long studentId) {
        SecurityUtils.requireSelf(studentId);
        return ResponseEntity.ok(classroomService.joinClassroom(joinCode, studentId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ClassroomMember>> getStudentClassrooms(@PathVariable Long studentId) {
        SecurityUtils.requireSelf(studentId);
        return ResponseEntity.ok(classroomService.getStudentClassrooms(studentId));
    }

    @DeleteMapping("/{classroomId}/leave")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> leaveClassroom(@PathVariable Long classroomId,
                                                               @RequestParam Long studentId) {
        SecurityUtils.requireSelf(studentId);
        classroomService.leaveClassroom(classroomId, studentId);
        return ResponseEntity.ok(Map.of("message", "Left classroom successfully"));
    }
}
