package fitnessclub.dto;

import java.time.LocalDateTime;

public record MemberBookingOptionResponse(
        Long lessonId,
        String lessonName,
        String trainerName,
        LocalDateTime dateTime,
        long freeSeats
) {
}
