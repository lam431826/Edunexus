package com.edunexus.service;

import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Notification;
import com.edunexus.domain.User;
import com.edunexus.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** In-app only notifications for a Class (whole-class broadcast when recipient is null). */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getByClass(Long classId) {
        return notificationRepository.findByClassEntity_IdOrderByCreatedAtDesc(classId);
    }

    public List<Notification> getByRecipient(User recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    @Transactional
    public Notification send(ClassEntity classEntity, User sender, User recipient, String message) {
        Notification notification = Notification.builder()
                .classEntity(classEntity)
                .sender(sender)
                .recipient(recipient)
                .message(message)
                .build();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markRead(Notification notification) {
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
