package fitnessclub.repository;

import fitnessclub.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsByMemberIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long memberId, LocalDate startDate, LocalDate endDate);

    long countByMemberId(Long memberId);

    Optional<Subscription> findTopByMemberIdOrderByEndDateDesc(Long memberId);

    Optional<Subscription> findTopByMemberIdAndEndDateIsNotNullOrderByEndDateDesc(Long memberId);

    List<Subscription> findByMemberIdOrderByEndDateDesc(Long memberId);
}
