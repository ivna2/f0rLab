package fitnessclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SubscriptionRequest(
        @NotNull Long memberId,
        LocalDate startDate,
        LocalDate endDate
) {
}
