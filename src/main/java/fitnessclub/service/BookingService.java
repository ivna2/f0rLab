package fitnessclub.service;

import fitnessclub.dto.BookingRequest;
import fitnessclub.model.Booking;
import fitnessclub.model.Lesson;
import fitnessclub.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository repo;
    private final MemberService memberService;
    private final LessonService lessonService;

    public List<Booking> getAll() {
        return repo.findAllByOrderByDateAsc();
    }

    public List<Booking> getAllForMember(Long memberId) {
        return repo.findByMemberIdOrderByDateAsc(memberId);
    }

    public List<Booking> getAllForMemberEmail(String email) {
        return repo.findByMemberEmailIgnoreCaseOrderByDateAsc(email);
    }

    public Booking get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Бронирование не найдено: " + id));
    }

    @Transactional
    public Booking add(BookingRequest request) {
        Lesson lesson = lessonService.get(request.lessonId());
        Booking booking = new Booking(
                null,
                memberService.get(request.memberId()),
                lesson,
                request.date() != null ? request.date() : lesson.getDateTime().toLocalDate()
        );
        return repo.save(booking);
    }

    @Transactional
    public Booking update(Long id, BookingRequest request) {
        Booking booking = get(id);
        Lesson lesson = lessonService.get(request.lessonId());
        booking.setMember(memberService.get(request.memberId()));
        booking.setLesson(lesson);
        booking.setDate(request.date() != null ? request.date() : lesson.getDateTime().toLocalDate());
        return repo.save(booking);
    }

    @Transactional
    public void cancel(Long id) {
        get(id);
        repo.deleteById(id);
    }
}
