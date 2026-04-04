package fitnessclub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EnrollMemberRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotNull LocalDate subscriptionStartDate,
        @NotNull LocalDate subscriptionEndDate
) {
}
