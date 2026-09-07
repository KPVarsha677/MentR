package com.mentorhub.controller;

import com.mentorhub.entity.*;
import com.mentorhub.repository.*;
import com.mentorhub.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * ReportController - generates reports for teachers.
 *
 * WHY THIS EXISTS:
 * Teachers need a way to generate summaries of their students' portfolios.
 * We keep reports simple — just JSON data that the frontend can render as HTML.
 * The frontend has a print button that lets users print/save as PDF.
 *
 * REPORTS AVAILABLE:
 * GET /api/reports/student/{studentId}/portfolio     → full portfolio
 * GET /api/reports/student/{studentId}/certifications → cert report
 * GET /api/reports/student/{studentId}/internships   → internship report
 * GET /api/reports/student/{studentId}/achievements  → achievement report
 * GET /api/reports/classroom/{classroomId}/summary   → classroom summary
 * GET /api/reports/pending-verifications             → pending items
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('TEACHER')")
public class ReportController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ClassroomMemberRepository classroomMemberRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    /**
     * Full student portfolio report.
     * Returns all profile data for one student.
     * The frontend can print this as a PDF.
     */
    @GetMapping("/student/{studentId}/portfolio")
    public ResponseEntity<Map<String, Object>> getStudentPortfolio(@PathVariable Long studentId) {
        Map<String, Object> portfolio = new LinkedHashMap<>();

        portfolio.put("profile", teacherService.getStudentProfile(studentId));
        portfolio.put("projects", teacherService.getStudentProjects(studentId));
        portfolio.put("skills", teacherService.getStudentSkills(studentId));
        portfolio.put("certifications", teacherService.getStudentCertifications(studentId));
        portfolio.put("internships", teacherService.getStudentInternships(studentId));
        portfolio.put("achievements", teacherService.getStudentAchievements(studentId));

        return ResponseEntity.ok(portfolio);
    }

    /**
     * Certification report - all certifications for a student.
     */
    @GetMapping("/student/{studentId}/certifications")
    public ResponseEntity<List<Certification>> getCertificationReport(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentCertifications(studentId));
    }

    /**
     * Internship report - all internships for a student.
     */
    @GetMapping("/student/{studentId}/internships")
    public ResponseEntity<List<Internship>> getInternshipReport(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentInternships(studentId));
    }

    /**
     * Achievement report - all achievements for a student.
     */
    @GetMapping("/student/{studentId}/achievements")
    public ResponseEntity<List<Achievement>> getAchievementReport(@PathVariable Long studentId) {
        return ResponseEntity.ok(teacherService.getStudentAchievements(studentId));
    }

    /**
     * Classroom summary report.
     * Shows all students in a classroom with their verification counts.
     */
    @GetMapping("/classroom/{classroomId}/summary")
    public ResponseEntity<Map<String, Object>> getClassroomSummary(@PathVariable Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        List<ClassroomMember> members = classroomMemberRepository.findByClassroom(classroom);

        List<Map<String, Object>> studentSummaries = new ArrayList<>();
        for (ClassroomMember member : members) {
            User student = member.getStudent();
            // Get the student's register number from their profile
            String registerNumber = studentProfileRepository.findByUser(student)
                    .map(sp -> sp.getRegisterNumber())
                    .orElse(null);
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("studentId", student.getId());
            summary.put("studentName", student.getName());
            summary.put("registerNumber", registerNumber);
            summary.put("email", student.getEmail());
            summary.put("projects", projectRepository.findByStudent(student).size());
            summary.put("certifications", certificationRepository.findByStudent(student).size());
            summary.put("internships", internshipRepository.findByStudent(student).size());
            summary.put("achievements", achievementRepository.findByStudent(student).size());
            studentSummaries.add(summary);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("classroom", classroom.getName());
        report.put("subject", classroom.getSubject());
        report.put("academicYear", classroom.getAcademicYear());
        report.put("totalStudents", members.size());
        report.put("students", studentSummaries);

        return ResponseEntity.ok(report);
    }

    /**
     * Pending verifications report.
     * Shows all items that are still in PENDING status.
     */
    @GetMapping("/pending-verifications")
    public ResponseEntity<Map<String, Object>> getPendingVerifications() {
        Map<String, Object> pending = new LinkedHashMap<>();
        pending.put("projects", projectRepository.findByVerificationStatus("PENDING"));
        pending.put("certifications", certificationRepository.findByVerificationStatus("PENDING"));
        pending.put("internships", internshipRepository.findByVerificationStatus("PENDING"));
        pending.put("achievements", achievementRepository.findByVerificationStatus("PENDING"));
        return ResponseEntity.ok(pending);
    }
}
