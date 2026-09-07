package com.mentorhub.service;

import com.mentorhub.dto.*;
import com.mentorhub.entity.*;
import com.mentorhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * StudentService - handles all student profile operations.
 *
 * WHY THIS EXISTS:
 * Students maintain a digital portfolio with multiple sections.
 * This service handles CRUD operations for all of them:
 * - Personal/academic profile
 * - Projects
 * - Skills
 * - Certifications
 * - Internships
 * - Achievements
 *
 * IMPORTANT PATTERN:
 * Every time a student adds/updates/deletes an item,
 * we call notificationService.createNotification() to alert the teacher.
 */
@Service
public class StudentService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserRepository userRepository;

    // ========== PROFILE ==========

    /**
     * Get a student's profile.
     * Used on the profile view page.
     */
    public StudentProfile getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    /**
     * Update a student's personal and academic info.
     */
    public StudentProfile updateProfile(Long userId, StudentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Update all the fields from the request
        profile.setRegisterNumber(request.getRegisterNumber());
        profile.setDepartment(request.getDepartment());
        profile.setYear(request.getYear());
        profile.setSection(request.getSection());
        profile.setBatch(request.getBatch());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setCareerGoal(request.getCareerGoal());
        profile.setAbout(request.getAbout());

        return studentProfileRepository.save(profile);
    }

    // ========== PROJECTS ==========

    public List<Project> getProjects(Long userId) {
        User user = getUser(userId);
        return projectRepository.findByStudent(user);
    }

    public Project addProject(Long userId, ProjectRequest request) {
        User student = getUser(userId);

        Project project = new Project();
        project.setStudent(student);
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setProjectUrl(request.getProjectUrl());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setVerificationStatus("PENDING");

        return projectRepository.save(project);
    }

    public Project updateProject(Long userId, Long projectId, ProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Security check: ensure this project belongs to the requesting student
        if (!project.getStudent().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to edit this project");
        }

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setProjectUrl(request.getProjectUrl());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        // Reset verification status when updated — teacher must re-verify
        project.setVerificationStatus("PENDING");

        return projectRepository.save(project);
    }

    public void deleteProject(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getStudent().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this project");
        }

        projectRepository.delete(project);
    }

    // ========== SKILLS ==========

    public List<Skill> getSkills(Long userId) {
        return skillRepository.findByStudent(getUser(userId));
    }

    public Skill addSkill(Long userId, SkillRequest request) {
        User student = getUser(userId);

        Skill skill = new Skill();
        skill.setStudent(student);
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setProficiencyLevel(request.getProficiencyLevel());
        skill.setVerificationStatus("PENDING");

        return skillRepository.save(skill);
    }

    public Skill updateSkill(Long userId, Long skillId, SkillRequest request) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setProficiencyLevel(request.getProficiencyLevel());
        skill.setVerificationStatus("PENDING");

        return skillRepository.save(skill);
    }

    public void deleteSkill(Long userId, Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        skillRepository.delete(skill);
    }

    // ========== CERTIFICATIONS ==========

    public List<Certification> getCertifications(Long userId) {
        return certificationRepository.findByStudent(getUser(userId));
    }

    public Certification addCertification(Long userId, CertificationRequest request) {
        User student = getUser(userId);

        Certification cert = new Certification();
        cert.setStudent(student);
        cert.setName(request.getName());
        cert.setIssuingOrganization(request.getIssuingOrganization());
        cert.setIssueDate(request.getIssueDate());
        cert.setExpirationDate(request.getExpirationDate());
        cert.setCredentialUrl(request.getCredentialUrl());
        cert.setCredentialId(request.getCredentialId());
        cert.setVerificationStatus("PENDING");

        return certificationRepository.save(cert);
    }

    public Certification updateCertification(Long userId, Long certId, CertificationRequest request) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        if (!cert.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        cert.setName(request.getName());
        cert.setIssuingOrganization(request.getIssuingOrganization());
        cert.setIssueDate(request.getIssueDate());
        cert.setExpirationDate(request.getExpirationDate());
        cert.setCredentialUrl(request.getCredentialUrl());
        cert.setCredentialId(request.getCredentialId());
        cert.setVerificationStatus("PENDING");

        return certificationRepository.save(cert);
    }

    public void deleteCertification(Long userId, Long certId) {
        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        if (!cert.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        certificationRepository.delete(cert);
    }

    // ========== INTERNSHIPS ==========

    public List<Internship> getInternships(Long userId) {
        return internshipRepository.findByStudent(getUser(userId));
    }

    public Internship addInternship(Long userId, InternshipRequest request) {
        User student = getUser(userId);

        Internship internship = new Internship();
        internship.setStudent(student);
        internship.setCompanyName(request.getCompanyName());
        internship.setRole(request.getRole());
        internship.setDescription(request.getDescription());
        internship.setStartDate(request.getStartDate());
        internship.setEndDate(request.getEndDate());
        internship.setOngoing(request.isOngoing());
        internship.setLocation(request.getLocation());
        internship.setStipend(request.getStipend());
        internship.setVerificationStatus("PENDING");

        return internshipRepository.save(internship);
    }

    public Internship updateInternship(Long userId, Long internshipId, InternshipRequest request) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        if (!internship.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        internship.setCompanyName(request.getCompanyName());
        internship.setRole(request.getRole());
        internship.setDescription(request.getDescription());
        internship.setStartDate(request.getStartDate());
        internship.setEndDate(request.getEndDate());
        internship.setOngoing(request.isOngoing());
        internship.setLocation(request.getLocation());
        internship.setStipend(request.getStipend());
        internship.setVerificationStatus("PENDING");

        return internshipRepository.save(internship);
    }

    public void deleteInternship(Long userId, Long internshipId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        if (!internship.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        internshipRepository.delete(internship);
    }

    // ========== ACHIEVEMENTS ==========

    public List<Achievement> getAchievements(Long userId) {
        return achievementRepository.findByStudent(getUser(userId));
    }

    public Achievement addAchievement(Long userId, AchievementRequest request) {
        User student = getUser(userId);

        Achievement achievement = new Achievement();
        achievement.setStudent(student);
        achievement.setTitle(request.getTitle());
        achievement.setCategory(request.getCategory());
        achievement.setDescription(request.getDescription());
        achievement.setAchievementDate(request.getAchievementDate());
        achievement.setIssuingOrganization(request.getIssuingOrganization());
        achievement.setVerificationStatus("PENDING");

        return achievementRepository.save(achievement);
    }

    public Achievement updateAchievement(Long userId, Long achievementId, AchievementRequest request) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        if (!achievement.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        achievement.setTitle(request.getTitle());
        achievement.setCategory(request.getCategory());
        achievement.setDescription(request.getDescription());
        achievement.setAchievementDate(request.getAchievementDate());
        achievement.setIssuingOrganization(request.getIssuingOrganization());
        achievement.setVerificationStatus("PENDING");

        return achievementRepository.save(achievement);
    }

    public void deleteAchievement(Long userId, Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        if (!achievement.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        achievementRepository.delete(achievement);
    }

    // ========== HELPER ==========

    /**
     * Helper method to load a User by ID.
     * Avoids repeating the same code in every method.
     */
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}
