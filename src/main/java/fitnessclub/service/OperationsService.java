package fitnessclub.service;

import fitnessclub.dto.BookLessonOperationRequest;
import fitnessclub.dto.BookingRequest;
import fitnessclub.dto.EnrollMemberRequest;
import fitnessclub.dto.EnrollmentResponse;
import fitnessclub.dto.MemberRequest;
import fitnessclub.dto.MemberScheduleItemResponse;
import fitnessclub.dto.RenewSubscriptionRequest;
import fitnessclub.dto.RescheduleBookingRequest;
import fitnessclub.dto.SubscriptionRequest;
import fitnessclub.dto.TrainerWorkloadLessonResponse;
import fitnessclub.model.Booking;
import fitnessclub.model.Lesson;
import fitnessclub.model.Member;
import fitnessclub.model.NotificationType;
import fitnessclub.model.Subscription;
import fitnessclub.repository.BookingRepository;
import fitnessclub.repository.LessonRepository;
import fitnessclub.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class OperationsService {

    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final TrainerService trainerService;
    private final LessonService lessonService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final LessonRepository lessonRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SecurityAccessService securityAccessService;
    private final AdminNotificationService adminNotificationService;

    @Transactional
    public EnrollmentResponse enrollMember(EnrollMemberRequest request) {
        Member member = memberService.add(new MemberRequest(request.name(), request.email(), null));
        Subscription subscription = subscriptionService.add(new SubscriptionRequest(
                member.getId(),
                request.subscriptionStartDate(),
                request.subscriptionEndDate()
        ));
        return new EnrollmentResponse(member, subscription);
    }

    @Transactional
    public Booking bookLesson(BookLessonOperationRequest request) {
        securityAccessService.ensureCurrentMemberAccess(request.memberId());
        Lesson lesson = lessonService.get(request.lessonId());
        ensureMemberHasActiveSubscription(request.memberId(), lesson.getDateTime().toLocalDate());
        ensureBookingCapacity(lesson);
        if (bookingRepository.existsByMemberIdAndLessonId(request.memberId(), request.lessonId())) {
            throw new ResponseStatusException(CONFLICT, "\u0423\u0447\u0430\u0441\u0442\u043d\u0438\u043a \u0443\u0436\u0435 \u0437\u0430\u043f\u0438\u0441\u0430\u043d \u043d\u0430 \u044d\u0442\u043e \u0437\u0430\u043d\u044f\u0442\u0438\u0435");
        }
        return bookingService.add(new BookingRequest(request.memberId(), request.lessonId(), lesson.getDateTime().toLocalDate()));
    }

    @Transactional
    public Booking rescheduleBooking(Long bookingId, RescheduleBookingRequest request) {
        Booking existingBooking = bookingService.get(bookingId);
        String actorLogin = securityAccessService.getCurrentUser().getLogin();
        securityAccessService.ensureCurrentMemberOwnsBooking(existingBooking);

        Lesson newLesson = lessonService.get(request.newLessonId());
        ensureMemberHasActiveSubscription(existingBooking.getMember().getId(), newLesson.getDateTime().toLocalDate());
        ensureBookingCapacity(newLesson);
        if (bookingRepository.existsByMemberIdAndLessonId(existingBooking.getMember().getId(), newLesson.getId())) {
            throw new ResponseStatusException(CONFLICT, "\u0423\u0447\u0430\u0441\u0442\u043d\u0438\u043a \u0443\u0436\u0435 \u0437\u0430\u043f\u0438\u0441\u0430\u043d \u043d\u0430 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0435 \u0437\u0430\u043d\u044f\u0442\u0438\u0435");
        }

        Booking updated = bookingService.update(
                bookingId,
                new BookingRequest(existingBooking.getMember().getId(), newLesson.getId(), newLesson.getDateTime().toLocalDate())
        );

        adminNotificationService.notify(
                NotificationType.BOOKING_RESCHEDULED,
                "\u041f\u0435\u0440\u0435\u043d\u043e\u0441 \u0437\u0430\u043f\u0438\u0441\u0438",
                "\u041f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c " + actorLogin + " \u043f\u0435\u0440\u0435\u043d\u0435\u0441 \u0437\u0430\u043d\u044f\u0442\u0438\u0435 " + existingBooking.getLesson().getName()
                        + " \u043d\u0430 " + newLesson.getName() + " (" + newLesson.getDateTime() + ")",
                actorLogin
        );
        return updated;
    }

    @Transactional
    public Subscription renewSubscription(RenewSubscriptionRequest request) {
        securityAccessService.ensureCurrentMemberAccess(request.memberId());
        Member member = memberService.get(request.memberId());
        Subscription lastSubscription = subscriptionRepository.findTopByMemberIdOrderByEndDateDesc(member.getId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "\u0423 \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u0430 \u043d\u0435\u0442 \u0430\u0431\u043e\u043d\u0435\u043c\u0435\u043d\u0442\u0430 \u0434\u043b\u044f \u043f\u0440\u043e\u0434\u043b\u0435\u043d\u0438\u044f"));

        LocalDate newStartDate = lastSubscription.getEndDate().plusDays(1);
        if (!request.newEndDate().isAfter(lastSubscription.getEndDate())) {
            throw new ResponseStatusException(BAD_REQUEST, "\u041d\u043e\u0432\u0430\u044f \u0434\u0430\u0442\u0430 \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u043f\u043e\u0437\u0436\u0435 \u0442\u0435\u043a\u0443\u0449\u0435\u0439");
        }

        return subscriptionService.add(new SubscriptionRequest(member.getId(), newStartDate, request.newEndDate()));
    }

    @Transactional(readOnly = true)
    public List<MemberScheduleItemResponse> getMemberSchedule(Long memberId, LocalDate from, LocalDate to) {
        securityAccessService.ensureCurrentMemberAccess(memberId);
        memberService.get(memberId);
        return bookingRepository.findByMemberIdAndDateBetweenOrderByDateAsc(memberId, from, to).stream()
                .map(booking -> new MemberScheduleItemResponse(
                        booking.getId(),
                        booking.getLesson().getId(),
                        booking.getLesson().getName(),
                        booking.getLesson().getTrainer().getId(),
                        booking.getLesson().getTrainer().getName(),
                        booking.getDate(),
                        booking.getLesson().getDateTime()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerWorkloadLessonResponse> getTrainerWorkload(Long trainerId, LocalDate from, LocalDate to) {
        trainerService.get(trainerId);
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusSeconds(1);
        return lessonRepository.findByTrainerIdAndDateTimeBetweenOrderByDateTimeAsc(trainerId, fromDateTime, toDateTime).stream()
                .map(lesson -> {
                    long bookedSeats = bookingRepository.countByLessonId(lesson.getId());
                    return new TrainerWorkloadLessonResponse(
                            lesson.getId(),
                            lesson.getName(),
                            lesson.getDateTime(),
                            lesson.getCapacity(),
                            bookedSeats,
                            lesson.getCapacity() - bookedSeats
                    );
                })
                .toList();
    }

    private void ensureMemberHasActiveSubscription(Long memberId, LocalDate lessonDate) {
        boolean active = subscriptionRepository.existsByMemberIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                memberId,
                lessonDate,
                lessonDate
        );
        if (!active) {
            throw new ResponseStatusException(CONFLICT, "\u041d\u0430 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u0443\u044e \u0434\u0430\u0442\u0443 \u0443 \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u0430 \u043d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0433\u043e \u0430\u0431\u043e\u043d\u0435\u043c\u0435\u043d\u0442\u0430");
        }
    }

    private void ensureBookingCapacity(Lesson lesson) {
        long bookedSeats = bookingRepository.countByLessonId(lesson.getId());
        if (bookedSeats >= lesson.getCapacity()) {
            throw new ResponseStatusException(CONFLICT, "\u041d\u0430 \u044d\u0442\u043e \u0437\u0430\u043d\u044f\u0442\u0438\u0435 \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435\u0442 \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u044b\u0445 \u043c\u0435\u0441\u0442");
        }
    }

    @Transactional
    public void cancelBookingForCurrentMember(Booking booking) {
        String actorLogin = securityAccessService.getCurrentUser().getLogin();
        securityAccessService.ensureCurrentMemberOwnsBooking(booking);
        bookingService.cancel(booking.getId());
        adminNotificationService.notify(
                NotificationType.BOOKING_CANCELLED,
                "\u041e\u0442\u043c\u0435\u043d\u0430 \u0437\u0430\u043f\u0438\u0441\u0438",
                "\u041f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044c " + actorLogin + " \u043e\u0442\u043c\u0435\u043d\u0438\u043b \u0437\u0430\u043d\u044f\u0442\u0438\u0435 " + booking.getLesson().getName()
                        + " (" + booking.getLesson().getDateTime() + ")",
                actorLogin
        );
    }

    public long getAvailableSeats(Lesson lesson) {
        return lesson.getCapacity() - bookingRepository.countByLessonId(lesson.getId());
    }
}
