package fitnessclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SubscriptionRequest(
        @NotNull Long memberId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
