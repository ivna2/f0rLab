package fitnessclub.dto;

import jakarta.validation.constraints.NotNull;

public record BookLessonOperationRequest(
        @NotNull Long memberId,
        @NotNull Long lessonId
) {
}
