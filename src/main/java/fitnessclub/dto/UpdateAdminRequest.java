package fitnessclub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateAdminRequest(
        @NotBlank String login,
        @NotBlank String name,
        @Email @NotBlank String email,
        String phone,
        String password
) {
}
