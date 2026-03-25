package fitnessclub.repository;

import fitnessclub.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByMemberIdAndLessonId(Long memberId, Long lessonId);

    long countByMemberId(Long memberId);

    long countByLessonId(Long lessonId);

    List<Booking> findAllByOrderByDateAsc();

    List<Booking> findByMemberIdOrderByDateAsc(Long memberId);

    List<Booking> findByMemberEmailIgnoreCaseOrderByDateAsc(String email);

    List<Booking> findByMemberIdAndDateBetweenOrderByDateAsc(Long memberId, LocalDate from, LocalDate to);
}
