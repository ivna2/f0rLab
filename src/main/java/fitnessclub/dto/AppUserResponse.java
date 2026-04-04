package fitnessclub.dto;

import fitnessclub.model.Role;

import java.util.Set;

public record AppUserResponse(
        Long id,
        String login,
        String email,
        Long memberId,
<<<<<<< HEAD
        String memberName,
        String memberPhone,
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
        Set<Role> roles
) {
}
