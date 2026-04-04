package fitnessclub.dto;

import fitnessclub.model.Trainer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LessonFull {
    private Long id;
    private String name;
    private Trainer trainer;      // шаблоны ожидают l.trainer.name
    private LocalDateTime dateTime;
    private int capacity;
}
