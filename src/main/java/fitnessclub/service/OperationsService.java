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
import fitnessclub.dto.TrainerWorkloadCardResponse;
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
        Member member = memberService.add(new MemberRequest(request.name(), request.email(), request.phone()));
        Subscription subscription = createInitialSubscription(member, request);
        return new EnrollmentResponse(member, subscription);
    }

    @Transactional
    public Booking bookLesson(BookLessonOperationRequest request) {
        securityAccessService.ensureCurrentMemberAccess(request.memberId());
        Lesson lesson = lessonService.get(request.lessonId());
        ensureMemberHasActiveSubscription(request.memberId(), lesson.getDateTime().toLocalDate());
        ensureBookingCapacity(lesson);
        if (bookingRepository.existsByMemberIdAndLessonId(request.memberId(), request.lessonId())) {
            throw new ResponseStatusException(CONFLICT, "Участник уже записан на это занятие");
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
            throw new ResponseStatusException(CONFLICT, "Участник уже записан на выбранное занятие");
        }

        Booking updated = bookingService.update(
                bookingId,
                new BookingRequest(existingBooking.getMember().getId(), newLesson.getId(), newLesson.getDateTime().toLocalDate())
        );

        adminNotificationService.notify(
                NotificationType.BOOKING_RESCHEDULED,
                "Перенос записи",
                "Пользователь " + actorLogin + " перенес занятие " + existingBooking.getLesson().getName()
                        + " на " + newLesson.getName() + " (" + newLesson.getDateTime() + ")",
                actorLogin
        );
        return updated;
    }

    @Transactional
    public Subscription renewSubscription(RenewSubscriptionRequest request) {
        securityAccessService.ensureCurrentMemberAccess(request.memberId());
        Member member = memberService.get(request.memberId());
        Subscription lastSubscription = subscriptionRepository.findTopByMemberIdAndEndDateIsNotNullOrderByEndDateDesc(member.getId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "У участника нет абонемента для продления"));

        LocalDate newStartDate = lastSubscription.getEndDate().plusDays(1);
        if (!request.newEndDate().isAfter(lastSubscription.getEndDate())) {
            throw new ResponseStatusException(BAD_REQUEST, "Новая дата окончания должна быть позже текущей");
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

    @Transactional(readOnly = true)
    public List<TrainerWorkloadCardResponse> getTrainerWorkloadCards(LocalDate from, LocalDate to) {
        return trainerService.getAll().stream()
                .map(trainer -> new TrainerWorkloadCardResponse(
                        trainer.getId(),
                        trainer.getName(),
                        trainer.getSpecialization(),
                        getTrainerWorkload(trainer.getId(), from, to)
                ))
                .toList();
    }

    @Transactional
    public void cancelBookingForCurrentMember(Booking booking) {
        String actorLogin = securityAccessService.getCurrentUser().getLogin();
        securityAccessService.ensureCurrentMemberOwnsBooking(booking);
        bookingService.cancel(booking.getId());
        adminNotificationService.notify(
                NotificationType.BOOKING_CANCELLED,
                "Отмена записи",
                "Пользователь " + actorLogin + " отменил занятие " + booking.getLesson().getName()
                        + " (" + booking.getLesson().getDateTime() + ")",
                actorLogin
        );
    }

    public long getAvailableSeats(Lesson lesson) {
        return lesson.getCapacity() - bookingRepository.countByLessonId(lesson.getId());
    }

    private Subscription createInitialSubscription(Member member, EnrollMemberRequest request) {
        if (request.subscriptionStartDate() == null && request.subscriptionEndDate() == null) {
            return subscriptionService.add(new SubscriptionRequest(member.getId(), null, null));
        }
        if (request.subscriptionStartDate() == null || request.subscriptionEndDate() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Для абонемента нужно заполнить обе даты или оставить обе пустыми");
        }
        return subscriptionService.add(new SubscriptionRequest(
                member.getId(),
                request.subscriptionStartDate(),
                request.subscriptionEndDate()
        ));
    }

    private void ensureMemberHasActiveSubscription(Long memberId, LocalDate lessonDate) {
        boolean active = subscriptionRepository.existsByMemberIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                memberId,
                lessonDate,
                lessonDate
        );
        if (!active) {
            throw new ResponseStatusException(CONFLICT, "На выбранную дату у участника нет активного абонемента");
        }
    }

    private void ensureBookingCapacity(Lesson lesson) {
        long bookedSeats = bookingRepository.countByLessonId(lesson.getId());
        if (bookedSeats >= lesson.getCapacity()) {
            throw new ResponseStatusException(CONFLICT, "На это занятие больше нет свободных мест");
        }
    }
}
