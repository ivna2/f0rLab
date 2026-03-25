package fitnessclub.dto;

import fitnessclub.model.Role;

import java.util.Set;

public record AppUserResponse(
        Long id,
        String login,
        String email,
        Long memberId,
        Set<Role> roles
) {
}
