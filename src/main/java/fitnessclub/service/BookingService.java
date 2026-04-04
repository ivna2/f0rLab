package fitnessclub.service;

import fitnessclub.dto.BookingRequest;
import fitnessclub.model.Booking;
import fitnessclub.model.Lesson;
import fitnessclub.repository.BookingRepository;
<<<<<<< HEAD
import fitnessclub.repository.SubscriptionRepository;
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

<<<<<<< HEAD
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
=======
import java.util.List;

>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository repo;
    private final MemberService memberService;
    private final LessonService lessonService;
<<<<<<< HEAD
    private final SubscriptionRepository subscriptionRepository;
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d

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
<<<<<<< HEAD
        LocalDate bookingDate = request.date() != null ? request.date() : lesson.getDateTime().toLocalDate();
        ensureMemberHasActiveSubscription(request.memberId(), bookingDate);
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
        Booking booking = new Booking(
                null,
                memberService.get(request.memberId()),
                lesson,
<<<<<<< HEAD
                bookingDate
=======
                request.date() != null ? request.date() : lesson.getDateTime().toLocalDate()
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
        );
        return repo.save(booking);
    }

    @Transactional
    public Booking update(Long id, BookingRequest request) {
        Booking booking = get(id);
        Lesson lesson = lessonService.get(request.lessonId());
<<<<<<< HEAD
        LocalDate bookingDate = request.date() != null ? request.date() : lesson.getDateTime().toLocalDate();
        ensureMemberHasActiveSubscription(request.memberId(), bookingDate);
        booking.setMember(memberService.get(request.memberId()));
        booking.setLesson(lesson);
        booking.setDate(bookingDate);
=======
        booking.setMember(memberService.get(request.memberId()));
        booking.setLesson(lesson);
        booking.setDate(request.date() != null ? request.date() : lesson.getDateTime().toLocalDate());
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
        return repo.save(booking);
    }

    @Transactional
    public void cancel(Long id) {
        get(id);
        repo.deleteById(id);
    }
<<<<<<< HEAD

    private void ensureMemberHasActiveSubscription(Long memberId, LocalDate lessonDate) {
        boolean active = subscriptionRepository.existsByMemberIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                memberId,
                lessonDate,
                lessonDate
        );
        if (!active) {
            throw new ResponseStatusException(CONFLICT, "На дату занятия у участника нет действующего абонемента");
        }
    }
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
}
