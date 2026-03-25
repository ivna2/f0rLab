package fitnessclub.service;

import fitnessclub.dto.TrainerRequest;
import fitnessclub.model.Trainer;
import fitnessclub.repository.LessonRepository;
import fitnessclub.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TrainerService {
    private final TrainerRepository repo;
    private final LessonRepository lessonRepository;

    public List<Trainer> getAll() {
        return repo.findAll();
    }

    public Trainer get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Тренер не найден: " + id));
    }

    @Transactional
    public Trainer add(TrainerRequest request) {
        return repo.save(new Trainer(null, request.name(), request.specialization()));
    }

    @Transactional
    public Trainer update(Long id, TrainerRequest request) {
        Trainer trainer = get(id);
        trainer.setName(request.name());
        trainer.setSpecialization(request.specialization());
        return repo.save(trainer);
    }

    @Transactional
    public void delete(Long id) {
        Trainer trainer = get(id);
        long lessonsCount = lessonRepository.countByTrainerId(id);
        if (lessonsCount > 0) {
            throw new ResponseStatusException(CONFLICT,
                    "\u041d\u0435\u043b\u044c\u0437\u044f \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0442\u0440\u0435\u043d\u0435\u0440\u0430: \u0441 \u043d\u0438\u043c \u0441\u0432\u044f\u0437\u0430\u043d\u043e " + lessonsCount + " \u0437\u0430\u043d\u044f\u0442\u0438\u0435(\u0439)");
        }
        repo.delete(trainer);
    }
}
