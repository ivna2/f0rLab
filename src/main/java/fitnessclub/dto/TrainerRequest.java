package fitnessclub.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerRequest(
        @NotBlank String name,
        @NotBlank String specialization
) {
}
