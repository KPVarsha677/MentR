package com.mentorhub.controller;

import com.mentorhub.dto.TeacherProfileRequest;
import com.mentorhub.dto.VerificationRequest;
import com.mentorhub.entity.*;
import com.mentorhub.service.NotificationService;
import com.mentorhub.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * TeacherController - REST API for teacher dashboard operations.
 * Includes notification endpoints restored after demo phase.
 */
@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private NotificationService notificationService;

    // ========== PROFILE ==========

    @GetMapping("/{teacherId}/profile")
    public ResponseEntity<TeacherProfile> getProfile(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getProfile(teacherId));
    }

    @PutMapping("/{teacherId}/profile")
    public ResponseEntity<TeacherProfile> updateProfile(@PathVariable Long teacherId,
                                                         @RequestBody TeacherProfileRequest request) {
        return ResponseEntity.ok(teacherService.updateProfile(teacherId, request));
    }

    // ========== DASHBOARD ==========

    @GetMapping("/{teacherId}/dashboard")
    public ResponseEntity<Map<String, Long>> getDashboardStats(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getDashboardStats(teacherId));
    }

    // ========== SEARCH ==========

    /**
     * GET /api/teacher/{id}/students/search
     *
     * All parameters are optional query parameters.
     * Example: /api/teacher/1/students/search?name=john&department=CS
     *
     * @RequestParam(required = false) means the parameter is optional
     */
    @GetMapping("/{teacherId}/students/search")
    public ResponseEntity<List<StudentProfile>> searchStudents(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String registerNumber,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String section) {

        return ResponseEntity.ok(teacherService.searchStudents(name, registerNumber, department, year, section));
    }

    // ========== VIEW STUDENT PORTFOLIO ==========

    @GetMapping("/students/{studentId}/profile")
    public ResponseEntity<StudentProfile> getStudentProfile(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentProfile(studentId));
    }

    @GetMapping("/students/{studentId}/projects")
    public ResponseEntity<List<Project>> getStudentProjects(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentProjects(studentId));
    }

    @GetMapping("/students/{studentId}/certifications")
    public ResponseEntity<List<Certification>> getStudentCertifications(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentCertifications(studentId));
    }

    @GetMapping("/students/{studentId}/internships")
    public ResponseEntity<List<Internship>> getStudentInternships(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentInternships(studentId));
    }

    @GetMapping("/students/{studentId}/achievements")
    public ResponseEntity<List<Achievement>> getStudentAchievements(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentAchievements(studentId));
    }

    @GetMapping("/students/{studentId}/skills")
    public ResponseEntity<List<Skill>> getStudentSkills(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentSkills(studentId));
    }

    // ========== VERIFICATION ==========

    @PostMapping("/verify/project/{projectId}")
    public ResponseEntity<Project> verifyProject(@PathVariable Long projectId,
                                                   @RequestParam Long teacherId,
                                                   @RequestBody VerificationRequest request) {
        return ResponseEntity.ok(teacherService.verifyProject(projectId, teacherId, request));
    }

    @PostMapping("/verify/certification/{certId}")
    public ResponseEntity<Certification> verifyCertification(@PathVariable Long certId,
                                                               @RequestParam Long teacherId,
                                                               @RequestBody VerificationRequest request) {
        return ResponseEntity.ok(teacherService.verifyCertification(certId, teacherId, request));
    }

    @PostMapping("/verify/internship/{internshipId}")
    public ResponseEntity<Internship> verifyInternship(@PathVariable Long internshipId,
                                                        @RequestParam Long teacherId,
                                                        @RequestBody VerificationRequest request) {
        return ResponseEntity.ok(teacherService.verifyInternship(internshipId, teacherId, request));
    }

    @PostMapping("/verify/achievement/{achievementId}")
    public ResponseEntity<Achievement> verifyAchievement(@PathVariable Long achievementId,
                                                          @RequestParam Long teacherId,
                                                          @RequestBody VerificationRequest request) {
        return ResponseEntity.ok(teacherService.verifyAchievement(achievementId, teacherId, request));
    }

    @PostMapping("/verify/skill/{skillId}")
    public ResponseEntity<Skill> verifySkill(@PathVariable Long skillId,
                                              @RequestParam Long teacherId,
                                              @RequestBody VerificationRequest request) {
        return ResponseEntity.ok(teacherService.verifySkill(skillId, teacherId, request));
    }

    // ========== NOTIFICATIONS ==========

    /**
     * GET /api/teacher/{teacherId}/notifications
     * Returns all notifications for the teacher, newest first.
     */
    @GetMapping("/{teacherId}/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long teacherId) {
        return ResponseEntity.ok(notificationService.getTeacherNotifications(teacherId));
    }

    /**
     * GET /api/teacher/{teacherId}/notifications/count
     * Returns the count of unread notifications.
     */
    @GetMapping("/{teacherId}/notifications/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long teacherId) {
        return ResponseEntity.ok(Map.of("unread", notificationService.getUnreadCount(teacherId)));
    }

    /**
     * PUT /api/teacher/{teacherId}/notifications/{notificationId}/read
     * Mark a single notification as read.
     */
    @PutMapping("/{teacherId}/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markOneRead(@PathVariable Long teacherId,
                                                            @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    /**
     * PUT /api/teacher/{teacherId}/notifications/read
     * Mark all notifications for this teacher as read.
     */
    @PutMapping("/{teacherId}/notifications/read")
    public ResponseEntity<Map<String, String>> markAllRead(@PathVariable Long teacherId) {
        notificationService.markAllAsRead(teacherId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    /**
     * POST /api/teacher/{teacherId}/notifications/send
     * Teacher sends a direct notification to a specific student.
     */
    @PostMapping("/{teacherId}/notifications/send")
    public ResponseEntity<Notification> sendNotification(@PathVariable Long teacherId,
                                                          @RequestBody Map<String, Object> body) {
        Long studentId = Long.valueOf(body.get("studentId").toString());
        String message  = body.getOrDefault("message",  "").toString();
        String itemType = body.getOrDefault("itemType", "GENERAL").toString();
        String action   = body.getOrDefault("action",   "INFO").toString();

        Notification notification = notificationService.createDirectNotification(
                teacherId, studentId, itemType, action, message);

        if (notification == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(notification);
    }
}
