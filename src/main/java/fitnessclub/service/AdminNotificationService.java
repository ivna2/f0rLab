package fitnessclub.service;

import fitnessclub.dto.AdminNotificationResponse;
import fitnessclub.model.AdminNotification;
import fitnessclub.model.NotificationType;
import fitnessclub.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminNotificationRepository repository;

    @Transactional
    public void notify(NotificationType type, String title, String message, String actorLogin) {
        repository.save(new AdminNotification(null, type, title, message, actorLogin, LocalDateTime.now()));
    }

    public List<AdminNotificationResponse> getLatest() {
        return repository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(notification -> new AdminNotificationResponse(
                        notification.getId(),
                        notification.getType(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getActorLogin(),
                        notification.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void clearAll() {
        repository.deleteAllInBatch();
    }
}
