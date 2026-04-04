package fitnessclub.dto;

import fitnessclub.model.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BookingFull {
    private Long id;
    private Member member;        // шаблоны ожидают b.member.name
    private LessonFull lesson;    // вложенный LessonFull — шаблоны смогут обращаться к b.lesson.trainer.name
    private LocalDate date;
}

