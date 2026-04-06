package fitnessclub.dto;

import java.util.List;

public record AdminDashboardResponse(
        String login,
        List<AdminNotificationResponse> notifications,
        List<TrainerWorkloadCardResponse> trainerWorkloads
) {
}
