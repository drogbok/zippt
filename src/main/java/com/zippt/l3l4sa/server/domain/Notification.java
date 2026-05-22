package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.NotificationStatus;
import com.zippt.l3l4sa.common.enums.NotificationType;
import com.zippt.l3l4sa.common.validation.DataDictionaryValidator;
import com.zippt.l3l4sa.common.validation.DomainValidationException;
import com.zippt.l3l4sa.common.validation.ValidationErrorCode;
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
        DataDictionaryValidator.requireId(notificationId, "noti-");
        DataDictionaryValidator.requireTextLength(message, 5, 300, ValidationErrorCode.NOTIFICATION_MESSAGE_INVALID);
        this.notificationId = notificationId;
        this.receiverUserId = receiverUserId;
        this.notificationType = notificationType;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.UNREAD;
    }

    public void markRead() {
        if (status == NotificationStatus.FAILED) {
            throw new DomainValidationException(
                    ValidationErrorCode.NOTIFICATION_STATUS_INVALID,
                    "FAILED notification cannot be marked READ."
            );
        }
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


