package fitnessclub.repository;

import fitnessclub.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    boolean existsByTrainerIdAndDateTime(Long trainerId, LocalDateTime dateTime);

    boolean existsByTrainerIdAndDateTimeAndIdNot(Long trainerId, LocalDateTime dateTime, Long id);

    long countByTrainerId(Long trainerId);

    List<Lesson> findByTrainerIdAndDateTimeBetweenOrderByDateTimeAsc(Long trainerId, LocalDateTime from, LocalDateTime to);

    List<Lesson> findByTrainerIdAndDateTimeAfterOrderByDateTimeAsc(Long trainerId, LocalDateTime from);
}
