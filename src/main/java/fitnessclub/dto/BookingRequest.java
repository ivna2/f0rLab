package fitnessclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull Long memberId,
        @NotNull Long lessonId,
        LocalDate date
) {
}
