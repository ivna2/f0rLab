package fitnessclub.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MemberBookingCardResponse(
        Long bookingId,
        Long lessonId,
        String lessonName,
        String trainerName,
        LocalDate date,
        LocalDateTime dateTime,
        List<MemberBookingOptionResponse> rescheduleOptions
) {
}
