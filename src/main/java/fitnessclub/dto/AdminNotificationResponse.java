package fitnessclub.dto;

import fitnessclub.model.NotificationType;

import java.time.LocalDateTime;

public record AdminNotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String actorLogin,
        LocalDateTime createdAt
) {
}
