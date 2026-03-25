package fitnessclub.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RenewSubscriptionRequest(
        @NotNull Long memberId,
        @NotNull LocalDate newEndDate
) {
}
