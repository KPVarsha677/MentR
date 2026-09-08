package com.mentorhub.service;

import com.mentorhub.dto.ClassroomRequest;
import com.mentorhub.entity.Classroom;
import com.mentorhub.entity.ClassroomMember;
import com.mentorhub.entity.User;
import com.mentorhub.repository.ClassroomMemberRepository;
import com.mentorhub.repository.ClassroomRepository;
import com.mentorhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * ClassroomService - handles all classroom-related operations.
 *
 * WHY THIS EXISTS:
 * The controller receives HTTP requests and delegates to this service.
 * The service contains the actual business rules, like:
 * - Generating unique join codes
 * - Preventing duplicate joins
 * - Verifying ownership before editing/deleting
 */
@Service
public class ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ClassroomMemberRepository classroomMemberRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new classroom for a teacher.
     *
     * The key operation here is generating a unique join code.
     * We use UUID to generate a random string and take the first 8 characters.
     * UUID.randomUUID() produces something like "3f2504e0-4f89-11d3-9a0c-0305e82c3301"
     * We take first 8 chars: "3f2504e0"
     *
     * IMPORTANT: We check that the code is unique (no duplicates).
     *
     * @param request   - classroom details (name, description, etc.)
     * @param teacherId - ID of the teacher creating the classroom
     * @return the saved Classroom entity
     */
    public Classroom createClassroom(ClassroomRequest request, Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Classroom classroom = new Classroom();
        classroom.setName(request.getName());
        classroom.setDescription(request.getDescription());
        classroom.setSubject(request.getSubject());
        classroom.setAcademicYear(request.getAcademicYear());
        classroom.setTeacher(teacher);

        // Generate a unique join code
        // Loop until we find a code that isn't already used
        String joinCode;
        do {
            joinCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (classroomRepository.findByJoinCode(joinCode).isPresent());

        classroom.setJoinCode(joinCode);
        return classroomRepository.save(classroom);
    }

    /**
     * Get all classrooms created by a teacher.
     * Used on the teacher's "My Classrooms" page.
     */
    public List<Classroom> getTeacherClassrooms(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return classroomRepository.findByTeacher(teacher);
    }

    /**
     * Edit an existing classroom.
     * Only the teacher who created the classroom can edit it.
     *
     * @param classroomId - which classroom to edit
     * @param request     - new data for the classroom
     * @param teacherId   - teacher making the request (for ownership check)
     */
    public Classroom editClassroom(Long classroomId, ClassroomRequest request, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Ownership check: ensure the teacher owns this classroom
        if (!classroom.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You are not authorized to edit this classroom");
        }

        classroom.setName(request.getName());
        classroom.setDescription(request.getDescription());
        classroom.setSubject(request.getSubject());
        classroom.setAcademicYear(request.getAcademicYear());
        return classroomRepository.save(classroom);
    }

    /**
     * Delete a classroom.
     * Only the teacher who created it can delete it.
     */
    public void deleteClassroom(Long classroomId, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!classroom.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You are not authorized to delete this classroom");
        }

        classroomRepository.delete(classroom);
    }

    /**
     * Student joins a classroom using a join code.
     *
     * VALIDATION STEPS:
     * 1. Find the classroom with this code
     * 2. Make sure the student hasn't already joined
     * 3. Create a ClassroomMember record linking student to classroom
     */
    public ClassroomMember joinClassroom(String joinCode, Long studentId) {
        Classroom classroom = classroomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Invalid join code. No classroom found."));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Prevent duplicate memberships
        if (classroomMemberRepository.existsByClassroomAndStudent(classroom, student)) {
            throw new RuntimeException("You have already joined this classroom");
        }

        ClassroomMember member = new ClassroomMember();
        member.setClassroom(classroom);
        member.setStudent(student);
        return classroomMemberRepository.save(member);
    }

    /**
     * Get all classrooms a student has joined.
     */
    public List<ClassroomMember> getStudentClassrooms(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return classroomMemberRepository.findByStudent(student);
    }

    /**
     * Student leaves a classroom.
     */
    public void leaveClassroom(Long classroomId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ClassroomMember member = classroomMemberRepository.findByClassroomAndStudent(classroom, student)
                .orElseThrow(() -> new RuntimeException("You are not a member of this classroom"));

        classroomMemberRepository.delete(member);
    }

    /**
     * Get all students in a specific classroom.
     * Used by teachers to see their student roster.
     *
     * @param teacherId the authenticated caller — must own the classroom,
     *                  otherwise any teacher could view another teacher's
     *                  student roster just by guessing a classroom id
     */
    public List<ClassroomMember> getClassroomStudents(Long classroomId, Long teacherId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!classroom.getTeacher().getId().equals(teacherId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to view this classroom's roster");
        }

        return classroomMemberRepository.findByClassroom(classroom);
    }
}
