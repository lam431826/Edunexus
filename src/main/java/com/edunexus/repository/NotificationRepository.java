package com.edunexus.repository;

import com.edunexus.domain.Notification;
import com.edunexus.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByClassEntity_IdOrderByCreatedAtDesc(Long classId);

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
}
