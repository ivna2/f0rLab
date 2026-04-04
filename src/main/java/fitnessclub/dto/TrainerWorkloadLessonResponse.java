package fitnessclub.dto;

import java.time.LocalDateTime;

public record TrainerWorkloadLessonResponse(
        Long lessonId,
        String lessonName,
        LocalDateTime dateTime,
        int capacity,
        long bookedSeats,
        long availableSeats
) {
}
