package com.mentorhub.service;

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
     * Mark a single notification as read.
     *
     * @param notificationId ID of the notification to mark
     */
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
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
}
