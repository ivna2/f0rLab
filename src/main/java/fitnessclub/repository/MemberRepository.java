package fitnessclub.repository;

import fitnessclub.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    java.util.Optional<Member> findByEmailIgnoreCase(String email);
}
