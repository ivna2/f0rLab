package fitnessclub.repository;

import fitnessclub.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLoginIgnoreCase(String login);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByMemberId(Long memberId);

    long countByRolesContaining(fitnessclub.model.Role role);

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByEmailIgnoreCase(String email);

    java.util.List<AppUser> findAllByOrderByIdAsc();
}
