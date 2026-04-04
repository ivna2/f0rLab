package fitnessclub.dto;

import jakarta.validation.constraints.NotNull;

public record RescheduleBookingRequest(
        @NotNull Long newLessonId
) {
}
