package com.mentorhub.service;

import com.mentorhub.entity.Classroom;
import com.mentorhub.entity.ClassroomMember;
import com.mentorhub.entity.Notification;
import com.mentorhub.entity.User;
import com.mentorhub.repository.ClassroomMemberRepository;
import com.mentorhub.repository.ClassroomRepository;
import com.mentorhub.repository.NotificationRepository;
import com.mentorhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NotificationService - creates and retrieves notifications for teachers.
 *
 * WHEN A NOTIFICATION IS CREATED:
 * Every time a student adds/updates/deletes a portfolio item, this service
 * finds the teacher(s) whose classrooms include the student and creates a
 * notification record for each.
 *
 * Teachers can also send direct notifications to individual students.
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ClassroomMemberRepository classroomMemberRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a notification for every teacher whose classroom contains this student.
     *
     * @param student   the student who performed the action
     * @param itemType  type of item changed (e.g., "PROJECT", "CERTIFICATION")
     * @param action    action performed ("ADDED", "UPDATED", "DELETED")
     * @param itemTitle the title/name of the item for display in the notification
     */
    public void createNotification(User student, String itemType, String action, String itemTitle) {
        // Find all classrooms this student is a member of
        classroomMemberRepository.findByStudent(student).forEach(membership -> {
            User teacher = membership.getClassroom().getTeacher();
            if (teacher == null) return;

            Notification notification = new Notification();
            notification.setTeacher(teacher);
            notification.setStudent(student);
            notification.setItemType(itemType);
            notification.setAction(action);
            notification.setMessage(student.getName() + " " + action.toLowerCase() + " a " + itemType.toLowerCase()
                    + (itemTitle != null && !itemTitle.isBlank() ? ": " + itemTitle : ""));
            notificationRepository.save(notification);
        });
    }

    /**
     * Create a direct notification from a teacher to a student (and also stored
     * under the teacher so it appears in their notification list).
     *
     * @param teacherId teacher sending the notification
     * @param studentId student being notified
     * @param itemType  category label
     * @param action    action label
     * @param message   the message text
     * @return the saved Notification, or null if teacher/student not found
     */
    public Notification createDirectNotification(Long teacherId, Long studentId,
                                                  String itemType, String action, String message) {
        User teacher = userRepository.findById(teacherId).orElse(null);
        User student = userRepository.findById(studentId).orElse(null);
        if (teacher == null || student == null) return null;

        Notification notification = new Notification();
        notification.setTeacher(teacher);
        notification.setStudent(student);
        notification.setItemType(itemType);
        notification.setAction(action);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    /**
     * Send a direct notification to every student in a classroom.
     *
     * @param teacherId   teacher sending the notification — must own the classroom,
     *                    otherwise a teacher could message another teacher's class
     *                    just by guessing its id
     * @param classroomId the classroom whose members should be notified
     * @param itemType    category label
     * @param action      action label
     * @param message     the message text
     * @return the number of students notified
     */
    public int createClassroomNotification(Long teacherId, Long classroomId,
                                            String itemType, String action, String message) {
        Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
        if (classroom == null) return 0;
        if (!classroom.getTeacher().getId().equals(teacherId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to message this classroom");
        }
        User teacher = classroom.getTeacher();
        List<ClassroomMember> members = classroomMemberRepository.findByClassroom(classroom);

        List<Notification> notifications = members.stream().map(member -> {
            Notification notification = new Notification();
            notification.setTeacher(teacher);
            notification.setStudent(member.getStudent());
            notification.setItemType(itemType);
            notification.setAction(action);
            notification.setMessage(message);
            return notification;
        }).toList();

        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    /**
     * Mark a single notification as read.
     *
     * @param notificationId ID of the notification to mark
     * @param teacherId      the authenticated caller — must own the notification,
     *                       otherwise a teacher could mark (or probe the existence
     *                       of) another teacher's notification just by guessing its id
     */
    public void markAsRead(Long notificationId, Long teacherId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (!n.getTeacher().getId().equals(teacherId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "You are not authorized to access this notification");
            }
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Delete a notification from a teacher's inbox — used when the teacher
     * dismisses a "student completed this task" confirmation they've acted on.
     *
     * @param notificationId ID of the notification to delete
     * @param teacherId      the authenticated caller — must own the notification,
     *                       otherwise a teacher could delete (or probe the
     *                       existence of) another teacher's notification just by
     *                       guessing its id
     */
    public void deleteTeacherNotification(Long notificationId, Long teacherId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (!n.getTeacher().getId().equals(teacherId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "You are not authorized to access this notification");
            }
            notificationRepository.delete(n);
        });
    }

    /**
     * Delete every notification in a teacher's inbox — lets a teacher clear out
     * old/stale notifications in one action.
     *
     * @param teacherId the teacher's user ID
     */
    public void deleteAllTeacherNotifications(Long teacherId) {
        User teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null) return;
        notificationRepository.deleteAll(notificationRepository.findByTeacherOrderByCreatedAtDesc(teacher));
    }

    /**
     * Get all notifications for a teacher, newest first.
     *
     * @param teacherId the teacher's user ID
     * @return list of notifications
     */
    public List<Notification> getTeacherNotifications(Long teacherId) {
        User teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null) return List.of();
        return notificationRepository.findByTeacherOrderByCreatedAtDesc(teacher);
    }

    /**
     * Count unread notifications for a teacher.
     *
     * @param teacherId the teacher's user ID
     * @return count of unread notifications
     */
    public long getUnreadCount(Long teacherId) {
        User teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null) return 0L;
        return notificationRepository.countByTeacherAndRead(teacher, false);
    }

    /**
     * Mark all notifications for a teacher as read.
     *
     * @param teacherId the teacher's user ID
     */
    public void markAllAsRead(Long teacherId) {
        User teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null) return;
        List<Notification> unread = notificationRepository.findByTeacherAndReadOrderByCreatedAtDesc(teacher, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // Action used for direct teacher-to-student messages (see createDirectNotification).
    // Auto-generated activity log entries use "ADDED"/"UPDATED"/"DELETED" instead and
    // are meant for the teacher's inbox only, so the student's inbox filters on this.
    private static final String DIRECT_MESSAGE_ACTION = "INFO";

    // Action used for the notification sent back to the teacher when a student
    // marks a direct message as done.
    private static final String COMPLETED_ACTION = "COMPLETED";

    /**
     * Get direct notifications sent to a student by a teacher that the student
     * hasn't marked done yet, newest first.
     *
     * @param studentId the student's user ID
     * @return list of notifications
     */
    public List<Notification> getStudentNotifications(Long studentId) {
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) return List.of();
        return notificationRepository.findByStudentAndActionAndCompletedOrderByCreatedAtDesc(
                student, DIRECT_MESSAGE_ACTION, false);
    }

    /**
     * Count unread direct notifications for a student.
     *
     * @param studentId the student's user ID
     * @return count of unread notifications
     */
    public long getStudentUnreadCount(Long studentId) {
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) return 0L;
        return notificationRepository.countByStudentAndActionAndReadAndCompleted(
                student, DIRECT_MESSAGE_ACTION, false, false);
    }

    /**
     * Mark a single notification as read on behalf of a student.
     *
     * @param notificationId ID of the notification to mark
     * @param studentId      the authenticated caller — must own the notification,
     *                       otherwise a student could mark (or probe the existence
     *                       of) another student's notification just by guessing its id
     */
    public void markAsReadForStudent(Long notificationId, Long studentId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (!n.getStudent().getId().equals(studentId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "You are not authorized to access this notification");
            }
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Mark all notifications for a student as read.
     *
     * @param studentId the student's user ID
     */
    public void markAllAsReadForStudent(Long studentId) {
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) return;
        List<Notification> unread = notificationRepository.findByStudentAndActionAndReadAndCompletedOrderByCreatedAtDesc(
                student, DIRECT_MESSAGE_ACTION, false, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Mark a direct notification as done on behalf of a student: hides it from
     * the student's inbox and flips the same notification over to the teacher's
     * side as a "completed" confirmation, instead of creating a second row.
     *
     * @param notificationId ID of the notification the student is completing
     * @param studentId      the authenticated caller — must own the notification,
     *                       otherwise a student could complete (or probe the
     *                       existence of) another student's notification just by
     *                       guessing its id
     */
    public void completeNotification(Long notificationId, Long studentId) {
        Notification n = notificationRepository.findById(notificationId).orElse(null);
        if (n == null) return;
        if (!n.getStudent().getId().equals(studentId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to access this notification");
        }
        n.setMessage(n.getStudent().getName() + " completed: " + n.getMessage());
        n.setAction(COMPLETED_ACTION);
        n.setCompleted(true);
        n.setRead(false);
        notificationRepository.save(n);
    }
}
