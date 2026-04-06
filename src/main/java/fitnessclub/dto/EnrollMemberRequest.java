package fitnessclub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record EnrollMemberRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String phone,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate
) {
}
