package com.mentorhub.repository;

import com.mentorhub.entity.Notification;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * NotificationRepository - handles queries for the Notification entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Get all notifications for a teacher, most recent first. */
    List<Notification> findByTeacherOrderByCreatedAtDesc(User teacher);

    /** Count unread notifications for a teacher. */
    long countByTeacherAndRead(User teacher, boolean read);

    /** Get unread notifications for a teacher. */
    List<Notification> findByTeacherAndReadOrderByCreatedAtDesc(User teacher, boolean read);

    /**
     * Get not-yet-completed notifications for a student with the given action,
     * most recent first. Used to fetch only direct teacher-to-student messages
     * (action = "INFO"), excluding the "ADDED"/"UPDATED"/"DELETED" activity log
     * entries that are created for the teacher's benefit when the student edits
     * their own portfolio, and excluding ones the student has already marked done.
     */
    List<Notification> findByStudentAndActionAndCompletedOrderByCreatedAtDesc(User student, String action, boolean completed);

    /** Count unread, not-yet-completed notifications for a student with the given action. */
    long countByStudentAndActionAndReadAndCompleted(User student, String action, boolean read, boolean completed);

    /** Get unread, not-yet-completed notifications for a student with the given action. */
    List<Notification> findByStudentAndActionAndReadAndCompletedOrderByCreatedAtDesc(User student, String action, boolean read, boolean completed);
}
