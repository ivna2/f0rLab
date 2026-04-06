package fitnessclub.dto;

import java.time.LocalDateTime;

public record MemberBookableLessonResponse(
        Long lessonId,
        String lessonName,
        Long trainerId,
        String trainerName,
        String trainerSpecialization,
        LocalDateTime dateTime,
        long freeSeats
) {
}
