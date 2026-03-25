package fitnessclub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record LessonRequest(
        @NotBlank String name,
        @NotNull Long trainerId,
        @NotNull LocalDateTime dateTime,
        @Positive int capacity
) {
}
