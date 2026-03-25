package fitnessclub.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberScheduleItemResponse(
        Long bookingId,
        Long lessonId,
        String lessonName,
        Long trainerId,
        String trainerName,
        LocalDate date,
        LocalDateTime dateTime
) {
}
