package fitnessclub.service;

import fitnessclub.dto.LessonRequest;
import fitnessclub.model.Lesson;
import fitnessclub.model.Trainer;
import fitnessclub.repository.BookingRepository;
import fitnessclub.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository repo;
    private final TrainerService trainerService;
    private final BookingRepository bookingRepository;

    public List<Lesson> getAll() {
        return repo.findAll();
    }

    public Lesson get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Занятие не найдено: " + id));
    }

    @Transactional
    public Lesson add(LessonRequest request) {
        validateTrainerAvailability(request.trainerId(), request.dateTime(), null);
        Trainer trainer = trainerService.get(request.trainerId());
        Lesson lesson = new Lesson(null, request.name(), trainer, request.dateTime(), request.capacity());
        return repo.save(lesson);
    }

    @Transactional
    public Lesson update(Long id, LessonRequest request) {
        Lesson lesson = get(id);
        validateTrainerAvailability(request.trainerId(), request.dateTime(), id);
        lesson.setName(request.name());
        lesson.setTrainer(trainerService.get(request.trainerId()));
        lesson.setDateTime(request.dateTime());
        lesson.setCapacity(request.capacity());
        return repo.save(lesson);
    }

    @Transactional
    public void delete(Long id) {
        Lesson lesson = get(id);
        long bookingsCount = bookingRepository.countByLessonId(id);
        if (bookingsCount > 0) {
            throw new ResponseStatusException(CONFLICT,
                    "\u041d\u0435\u043b\u044c\u0437\u044f \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0437\u0430\u043d\u044f\u0442\u0438\u0435: \u043d\u0430 \u043d\u0435\u0433\u043e \u0435\u0441\u0442\u044c " + bookingsCount + " \u0431\u0440\u043e\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435(\u0439)");
        }
        repo.delete(lesson);
    }

    public List<Lesson> getFutureLessonsForTrainer(Long trainerId, LocalDateTime from) {
        trainerService.get(trainerId);
        return repo.findByTrainerIdAndDateTimeAfterOrderByDateTimeAsc(trainerId, from);
    }

    private void validateTrainerAvailability(Long trainerId, LocalDateTime dateTime, Long currentLessonId) {
        boolean occupied = currentLessonId == null
                ? repo.existsByTrainerIdAndDateTime(trainerId, dateTime)
                : repo.existsByTrainerIdAndDateTimeAndIdNot(trainerId, dateTime, currentLessonId);
        if (occupied) {
            throw new ResponseStatusException(CONFLICT, "У тренера уже есть занятие на это время");
        }
    }
}
