package fitnessclub.repository;

import fitnessclub.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLoginIgnoreCase(String login);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByMemberId(Long memberId);

<<<<<<< HEAD
    long countByRolesContaining(fitnessclub.model.Role role);

=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
    boolean existsByLoginIgnoreCase(String login);

    boolean existsByEmailIgnoreCase(String email);

    java.util.List<AppUser> findAllByOrderByIdAsc();
}
