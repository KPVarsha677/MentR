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
}
