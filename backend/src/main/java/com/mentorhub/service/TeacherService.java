package com.mentorhub.service;

import com.mentorhub.dto.TeacherProfileRequest;
import com.mentorhub.dto.VerificationRequest;
import com.mentorhub.entity.*;
import com.mentorhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * TeacherService - handles teacher dashboard, verification, and search.
 *
 * WHY THIS EXISTS:
 * Teachers have very different needs from students.
 * They need to:
 * - See all students in their classrooms
 * - Search students
 * - View student portfolios
 * - Verify (approve/reject) items submitted by students
 * - See notifications
 * This service handles all of that.
 */
@Service
public class TeacherService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ClassroomMemberRepository classroomMemberRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    // ========== PROFILE ==========

    public TeacherProfile getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return teacherProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
    }

    public TeacherProfile updateProfile(Long userId, TeacherProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TeacherProfile profile = teacherProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        profile.setDepartment(request.getDepartment());
        profile.setEmployeeId(request.getEmployeeId());
        profile.setPhone(request.getPhone());
        profile.setDesignation(request.getDesignation());
        if (request.getSubject() != null) profile.setSubject(request.getSubject());
        if (request.getAbout()   != null) profile.setAbout(request.getAbout());

        return teacherProfileRepository.save(profile);
    }

    // ========== DASHBOARD STATS ==========

    /**
     * Get dashboard summary statistics for a teacher.
     * Returns counts of students, pending verifications, etc.
     * Used for the dashboard summary cards at the top of the teacher dashboard.
     */
    public Map<String, Long> getDashboardStats(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Get all classrooms belonging to this teacher
        List<Classroom> teacherClassrooms = classroomRepository.findByTeacher(teacher);
        long totalClassrooms = teacherClassrooms.size();

        // Get all classroom memberships that belong to THIS teacher's classrooms.
        // We filter by teacherId so stats only reflect the logged-in teacher's students.
        var classroomMembers = teacherClassrooms.stream()
                .flatMap(c -> classroomMemberRepository.findByClassroom(c).stream())
                .toList();

        long totalStudents = classroomMembers.stream()
                .map(m -> m.getStudent().getId())
                .distinct()
                .count();

        // Count pending verifications — only for students in THIS teacher's classrooms
        var studentIds = classroomMembers.stream()
                .map(m -> m.getStudent().getId())
                .distinct()
                .toList();

        long pendingProjects = studentIds.isEmpty() ? 0 :
                projectRepository.findByVerificationStatus("PENDING").stream()
                        .filter(p -> studentIds.contains(p.getStudent().getId())).count();
        long pendingCerts = studentIds.isEmpty() ? 0 :
                certificationRepository.findByVerificationStatus("PENDING").stream()
                        .filter(c -> studentIds.contains(c.getStudent().getId())).count();
        long pendingInternships = studentIds.isEmpty() ? 0 :
                internshipRepository.findByVerificationStatus("PENDING").stream()
                        .filter(i -> studentIds.contains(i.getStudent().getId())).count();
        long pendingAchievements = studentIds.isEmpty() ? 0 :
                achievementRepository.findByVerificationStatus("PENDING").stream()
                        .filter(a -> studentIds.contains(a.getStudent().getId())).count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("totalClassrooms", totalClassrooms);
        stats.put("pendingProjects", pendingProjects);
        stats.put("pendingCertifications", pendingCerts);
        stats.put("pendingInternships", pendingInternships);
        stats.put("pendingAchievements", pendingAchievements);
        stats.put("totalPending", pendingProjects + pendingCerts + pendingInternships + pendingAchievements);

        return stats;
    }

    // ========== SEARCH ==========

    /**
     * Search students by name, register number, department, year, or section.
     * All parameters are optional — if not provided, they act as wildcards.
     *
     * The actual query is in StudentProfileRepository using @Query annotation.
     */
    public List<StudentProfile> searchStudents(String name, String registerNumber,
                                               String department, Integer year, String section) {
        return studentProfileRepository.searchStudents(name, registerNumber, department, year, section);
    }

    // ========== VIEW STUDENT PORTFOLIO ==========

    /**
     * Get a full student portfolio (all profile data).
     * Used by teachers to view a student's complete profile.
     */
    public StudentProfile getStudentProfile(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return studentProfileRepository.findByUser(student)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
    }

    public List<Project> getStudentProjects(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return projectRepository.findByStudent(student);
    }

    public List<Certification> getStudentCertifications(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return certificationRepository.findByStudent(student);
    }

    public List<Internship> getStudentInternships(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return internshipRepository.findByStudent(student);
    }

    public List<Achievement> getStudentAchievements(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return achievementRepository.findByStudent(student);
    }

    public List<Skill> getStudentSkills(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return skillRepository.findByStudent(student);
    }

    // ========== VERIFICATION ==========

    /**
     * Verify (approve or reject) a project.
     *
     * HOW VERIFICATION WORKS:
     * 1. Teacher reviews the student's submission
     * 2. Teacher sets status to "APPROVED" or "REJECTED"
     * 3. Teacher optionally adds a comment explaining the decision
     * 4. The student can see this status and comment on their profile
     *
     * @param projectId - which project to verify
     * @param teacherId - which teacher is verifying (logged in teacher)
     * @param request   - status ("APPROVED"/"REJECTED") and optional comment
     */
    public Project verifyProject(Long projectId, Long teacherId, VerificationRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setVerificationStatus(request.getStatus());
        project.setTeacherComment(request.getComment());
        project.setVerifiedAt(LocalDateTime.now());
        project.setVerifiedBy(teacherId);

        return projectRepository.save(project);
    }

    public Certification verifyCertification(Long certId, Long teacherId, VerificationRequest request) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        cert.setVerificationStatus(request.getStatus());
        cert.setTeacherComment(request.getComment());
        cert.setVerifiedAt(LocalDateTime.now());
        cert.setVerifiedBy(teacherId);

        return certificationRepository.save(cert);
    }

    public Internship verifyInternship(Long internshipId, Long teacherId, VerificationRequest request) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        internship.setVerificationStatus(request.getStatus());
        internship.setTeacherComment(request.getComment());
        internship.setVerifiedAt(LocalDateTime.now());
        internship.setVerifiedBy(teacherId);

        return internshipRepository.save(internship);
    }

    public Achievement verifyAchievement(Long achievementId, Long teacherId, VerificationRequest request) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        achievement.setVerificationStatus(request.getStatus());
        achievement.setTeacherComment(request.getComment());
        achievement.setVerifiedAt(LocalDateTime.now());
        achievement.setVerifiedBy(teacherId);

        return achievementRepository.save(achievement);
    }

    public Skill verifySkill(Long skillId, Long teacherId, VerificationRequest request) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        skill.setVerificationStatus(request.getStatus());
        skill.setTeacherComment(request.getComment());
        skill.setVerifiedAt(LocalDateTime.now());
        skill.setVerifiedBy(teacherId);

        return skillRepository.save(skill);
    }
}
