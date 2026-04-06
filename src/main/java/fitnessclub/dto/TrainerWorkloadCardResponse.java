package fitnessclub.dto;

import java.util.List;

public record TrainerWorkloadCardResponse(
        Long trainerId,
        String trainerName,
        String specialization,
        List<TrainerWorkloadLessonResponse> lessons
) {
}
