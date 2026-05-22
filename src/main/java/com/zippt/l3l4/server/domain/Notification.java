package com.zippt.l3l4.server.domain;

import com.zippt.l3l4.common.enums.NotificationStatus;
import com.zippt.l3l4.common.enums.NotificationType;
import java.time.LocalDateTime;

public class Notification {
    private final String notificationId;
    private final String receiverUserId;
    private final NotificationType notificationType;
    private final String message;
    private final LocalDateTime createdAt;
    private LocalDateTime readAt;
    private NotificationStatus status;

    public Notification(String notificationId, String receiverUserId,
                        NotificationType notificationType, String message) {
        this.notificationId = notificationId;
        this.receiverUserId = receiverUserId;
        this.notificationType = notificationType;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.UNREAD;
    }

    public void markRead() {
        status = NotificationStatus.READ;
        readAt = LocalDateTime.now();
    }

    public void markFailed() {
        status = NotificationStatus.FAILED;
    }

    public String getNotificationId() { return notificationId; }
    public String getReceiverUserId() { return receiverUserId; }
    public NotificationType getNotificationType() { return notificationType; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public NotificationStatus getStatus() { return status; }
}

