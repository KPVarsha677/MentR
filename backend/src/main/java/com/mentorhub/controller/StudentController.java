package com.mentorhub.controller;

import com.mentorhub.dto.*;
import com.mentorhub.entity.*;
import com.mentorhub.security.SecurityUtils;
import com.mentorhub.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * StudentController - REST API for all student profile operations.
 *
 * ALL ENDPOINTS require ROLE_STUDENT, AND the {userId} in the path must
 * match the caller's own authenticated id (enforced via
 * SecurityUtils.requireSelf) — otherwise one student could read or modify
 * another student's portfolio just by changing the id in the URL.
 * Base path: /api/student
 *
 * Endpoints:
 * GET/PUT  /api/student/{id}/profile          → profile
 * GET/POST /api/student/{id}/projects          → projects
 * PUT/DEL  /api/student/{id}/projects/{pid}   → specific project
 * GET/POST /api/student/{id}/skills            → skills
 * PUT/DEL  /api/student/{id}/skills/{sid}     → specific skill
 * GET/POST /api/student/{id}/certifications    → certifications
 * PUT/DEL  /api/student/{id}/certifications/{cid} → specific cert
 * GET/POST /api/student/{id}/internships       → internships
 * PUT/DEL  /api/student/{id}/internships/{iid}→ specific internship
 * GET/POST /api/student/{id}/achievements      → achievements
 * PUT/DEL  /api/student/{id}/achievements/{aid}→ specific achievement
 */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // ========== PROFILE ==========

    @GetMapping("/{userId}/profile")
    public ResponseEntity<StudentProfile> getProfile(@PathVariable Long userId) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<StudentProfile> updateProfile(@PathVariable Long userId,
                                                         @RequestBody StudentProfileRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.updateProfile(userId, request));
    }

    // ========== PROJECTS ==========

    @GetMapping("/{userId}/projects")
    public ResponseEntity<List<Project>> getProjects(@PathVariable Long userId) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.getProjects(userId));
    }

    @PostMapping("/{userId}/projects")
    public ResponseEntity<Project> addProject(@PathVariable Long userId,
                                               @Valid @RequestBody ProjectRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.addProject(userId, request));
    }

    @PutMapping("/{userId}/projects/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable Long userId,
                                                  @PathVariable Long projectId,
                                                  @Valid @RequestBody ProjectRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.updateProject(userId, projectId, request));
    }

    @DeleteMapping("/{userId}/projects/{projectId}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long userId,
                                                              @PathVariable Long projectId) {
        SecurityUtils.requireSelf(userId);
        studentService.deleteProject(userId, projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted"));
    }

    // ========== SKILLS ==========

    @GetMapping("/{userId}/skills")
    public ResponseEntity<List<Skill>> getSkills(@PathVariable Long userId) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.getSkills(userId));
    }

    @PostMapping("/{userId}/skills")
    public ResponseEntity<Skill> addSkill(@PathVariable Long userId,
                                           @Valid @RequestBody SkillRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.addSkill(userId, request));
    }

    @PutMapping("/{userId}/skills/{skillId}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long userId,
                                              @PathVariable Long skillId,
                                              @Valid @RequestBody SkillRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.updateSkill(userId, skillId, request));
    }

    @DeleteMapping("/{userId}/skills/{skillId}")
    public ResponseEntity<Map<String, String>> deleteSkill(@PathVariable Long userId,
                                                            @PathVariable Long skillId) {
        SecurityUtils.requireSelf(userId);
        studentService.deleteSkill(userId, skillId);
        return ResponseEntity.ok(Map.of("message", "Skill deleted"));
    }

    // ========== CERTIFICATIONS ==========

    @GetMapping("/{userId}/certifications")
    public ResponseEntity<List<Certification>> getCertifications(@PathVariable Long userId) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.getCertifications(userId));
    }

    @PostMapping("/{userId}/certifications")
    public ResponseEntity<Certification> addCertification(@PathVariable Long userId,
                                                           @Valid @RequestBody CertificationRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.addCertification(userId, request));
    }

    @PutMapping("/{userId}/certifications/{certId}")
    public ResponseEntity<Certification> updateCertification(@PathVariable Long userId,
                                                              @PathVariable Long certId,
                                                              @Valid @RequestBody CertificationRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.updateCertification(userId, certId, request));
    }

    @DeleteMapping("/{userId}/certifications/{certId}")
    public ResponseEntity<Map<String, String>> deleteCertification(@PathVariable Long userId,
                                                                    @PathVariable Long certId) {
        SecurityUtils.requireSelf(userId);
        studentService.deleteCertification(userId, certId);
        return ResponseEntity.ok(Map.of("message", "Certification deleted"));
    }

    // ========== INTERNSHIPS ==========

    @GetMapping("/{userId}/internships")
    public ResponseEntity<List<Internship>> getInternships(@PathVariable Long userId) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.getInternships(userId));
    }

    @PostMapping("/{userId}/internships")
    public ResponseEntity<Internship> addInternship(@PathVariable Long userId,
                                                     @Valid @RequestBody InternshipRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.addInternship(userId, request));
    }

    @PutMapping("/{userId}/internships/{internshipId}")
    public ResponseEntity<Internship> updateInternship(@PathVariable Long userId,
                                                        @PathVariable Long internshipId,
                                                        @Valid @RequestBody InternshipRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.updateInternship(userId, internshipId, request));
    }

    @DeleteMapping("/{userId}/internships/{internshipId}")
    public ResponseEntity<Map<String, String>> deleteInternship(@PathVariable Long userId,
                                                                 @PathVariable Long internshipId) {
        SecurityUtils.requireSelf(userId);
        studentService.deleteInternship(userId, internshipId);
        return ResponseEntity.ok(Map.of("message", "Internship deleted"));
    }

    // ========== ACHIEVEMENTS ==========

    @GetMapping("/{userId}/achievements")
    public ResponseEntity<List<Achievement>> getAchievements(@PathVariable Long userId) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.getAchievements(userId));
    }

    @PostMapping("/{userId}/achievements")
    public ResponseEntity<Achievement> addAchievement(@PathVariable Long userId,
                                                       @Valid @RequestBody AchievementRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.addAchievement(userId, request));
    }

    @PutMapping("/{userId}/achievements/{achievementId}")
    public ResponseEntity<Achievement> updateAchievement(@PathVariable Long userId,
                                                          @PathVariable Long achievementId,
                                                          @Valid @RequestBody AchievementRequest request) {
        SecurityUtils.requireSelf(userId);
        return ResponseEntity.ok(studentService.updateAchievement(userId, achievementId, request));
    }

    @DeleteMapping("/{userId}/achievements/{achievementId}")
    public ResponseEntity<Map<String, String>> deleteAchievement(@PathVariable Long userId,
                                                                   @PathVariable Long achievementId) {
        SecurityUtils.requireSelf(userId);
        studentService.deleteAchievement(userId, achievementId);
        return ResponseEntity.ok(Map.of("message", "Achievement deleted"));
    }
}
