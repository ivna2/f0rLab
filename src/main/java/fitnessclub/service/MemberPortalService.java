package fitnessclub.service;

import fitnessclub.dto.MemberBookingCardResponse;
import fitnessclub.dto.MemberBookingOptionResponse;
import fitnessclub.dto.MemberBookableLessonResponse;
import fitnessclub.dto.MemberDashboardResponse;
import fitnessclub.dto.RescheduleBookingRequest;
import fitnessclub.dto.TrainerWorkloadCardResponse;
import fitnessclub.dto.BookLessonOperationRequest;
import fitnessclub.dto.RenewSubscriptionRequest;
import fitnessclub.model.AppUser;
import fitnessclub.model.Booking;
import fitnessclub.model.Lesson;
import fitnessclub.model.Member;
import fitnessclub.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class MemberPortalService {

    private final SecurityAccessService securityAccessService;
    private final BookingService bookingService;
    private final SubscriptionService subscriptionService;
    private final LessonService lessonService;
    private final MemberService memberService;
    private final OperationsService operationsService;

    @Transactional(readOnly = true)
    public MemberDashboardResponse getDashboard() {
        AppUser currentUser = securityAccessService.getCurrentUser();
        Member member = requireMember(currentUser);
        Subscription subscription = subscriptionService.getLatestForMember(member.getId());
        List<Booking> sourceBookings = loadBookingsForDashboard(currentUser, member);

        List<MemberBookingCardResponse> bookings = sourceBookings.stream()
                .filter(booking -> booking.getLesson() != null)
                .sorted(Comparator.comparing(booking -> booking.getLesson().getDateTime()))
                .limit(10)
                .map(this::toBookingCard)
                .toList();
        List<MemberBookableLessonResponse> bookableLessons = buildBookableLessons(member, sourceBookings);
        List<TrainerWorkloadCardResponse> trainerWorkloads = operationsService.getTrainerWorkloadCards(LocalDate.now(), LocalDate.now().plusDays(30));

        return new MemberDashboardResponse(
                currentUser.getLogin(),
                member.getName(),
                member.getEmail(),
                sanitizePhone(member.getPhone(), currentUser),
                member.getPhotoPath(),
                subscription != null ? subscription.getStartDate() : null,
                subscription != null ? subscription.getEndDate() : null,
                bookings,
                bookableLessons,
                trainerWorkloads
        );
    }

    @Transactional
    public void updatePhoto(MultipartFile file) {
        AppUser currentUser = securityAccessService.getCurrentUser();
        Member member = requireMember(currentUser);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0444\u0430\u0439\u043b \u0444\u043e\u0442\u043e\u0433\u0440\u0430\u0444\u0438\u0438");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(BAD_REQUEST, "\u041c\u043e\u0436\u043d\u043e \u0437\u0430\u0433\u0440\u0443\u0436\u0430\u0442\u044c \u0442\u043e\u043b\u044c\u043a\u043e \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u044f");
        }

        String extension = extractExtension(file.getOriginalFilename());
        Path uploadsDir = Path.of("uploads", "member-photos");
        String filename = "member-" + member.getId() + "-" + System.currentTimeMillis() + extension;
        try {
            Files.createDirectories(uploadsDir);
            Path target = uploadsDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            memberService.updatePhotoPath(member.getId(), "/member-photos/" + filename);
        } catch (IOException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0444\u043e\u0442\u043e\u0433\u0440\u0430\u0444\u0438\u044e");
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingService.get(bookingId);
        securityAccessService.ensureCurrentMemberOwnsBooking(booking);
        operationsService.cancelBookingForCurrentMember(booking);
    }

    @Transactional
    public void rescheduleBooking(Long bookingId, Long newLessonId) {
        operationsService.rescheduleBooking(bookingId, new RescheduleBookingRequest(newLessonId));
    }

    @Transactional
    public void renewSubscription(LocalDate newEndDate) {
        AppUser currentUser = securityAccessService.getCurrentUser();
        Member member = requireMember(currentUser);
        operationsService.renewSubscription(new RenewSubscriptionRequest(member.getId(), newEndDate));
    }

    @Transactional
    public void bookLesson(Long lessonId) {
        AppUser currentUser = securityAccessService.getCurrentUser();
        Member member = requireMember(currentUser);
        operationsService.bookLesson(new BookLessonOperationRequest(member.getId(), lessonId));
    }

    @Transactional(readOnly = true)
    public List<TrainerWorkloadCardResponse> getTrainerWorkloadCards(LocalDate from, LocalDate to) {
        return operationsService.getTrainerWorkloadCards(from, to);
    }

    private Member requireMember(AppUser currentUser) {
        if (currentUser.getMember() == null) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "\u041f\u0440\u043e\u0444\u0438\u043b\u044c \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u0430 \u043d\u0435 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d \u043a \u044d\u0442\u043e\u0439 \u0443\u0447\u0435\u0442\u043d\u043e\u0439 \u0437\u0430\u043f\u0438\u0441\u0438");
        }
        return currentUser.getMember();
    }

    private MemberBookingCardResponse toBookingCard(Booking booking) {
        List<MemberBookingOptionResponse> options = lessonService.getFutureLessonsForTrainer(
                        booking.getLesson().getTrainer().getId(),
                        LocalDateTime.now())
                .stream()
                .filter(lesson -> !lesson.getId().equals(booking.getLesson().getId()))
                .map(lesson -> new MemberBookingOptionResponse(
                        lesson.getId(),
                        lesson.getName(),
                        lesson.getTrainer().getName(),
                        lesson.getDateTime(),
                        operationsService.getAvailableSeats(lesson)
                ))
                .filter(option -> option.freeSeats() > 0)
                .limit(10)
                .toList();

        return new MemberBookingCardResponse(
                booking.getId(),
                booking.getLesson().getId(),
                booking.getLesson().getName(),
                booking.getLesson().getTrainer().getName(),
                booking.getDate(),
                booking.getLesson().getDateTime(),
                options
        );
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String sanitizePhone(String phone, AppUser currentUser) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String normalized = phone.trim();
        if (normalized.equalsIgnoreCase(currentUser.getLogin()) || normalized.equalsIgnoreCase(currentUser.getEmail())) {
            return null;
        }
        return normalized;
    }

    private List<Booking> loadBookingsForDashboard(AppUser currentUser, Member member) {
        List<Booking> directBookings = bookingService.getAllForMember(member.getId());
        if (!directBookings.isEmpty()) {
            return directBookings;
        }

        if (member.getEmail() != null && !member.getEmail().isBlank()) {
            List<Booking> emailBookings = bookingService.getAllForMemberEmail(member.getEmail());
            if (!emailBookings.isEmpty()) {
                return emailBookings;
            }
        }

        if (currentUser.getEmail() != null && !currentUser.getEmail().isBlank()) {
            return bookingService.getAllForMemberEmail(currentUser.getEmail());
        }

        return java.util.List.of();
    }

    private List<MemberBookableLessonResponse> buildBookableLessons(Member member, List<Booking> existingBookings) {
        Set<Long> bookedLessonIds = new HashSet<>();
        existingBookings.stream()
                .filter(booking -> booking.getLesson() != null)
                .map(booking -> booking.getLesson().getId())
                .forEach(bookedLessonIds::add);

        return lessonService.getAll().stream()
                .filter(lesson -> lesson.getDateTime().isAfter(LocalDateTime.now()))
                .filter(lesson -> !bookedLessonIds.contains(lesson.getId()))
                .filter(lesson -> operationsService.getAvailableSeats(lesson) > 0)
                .filter(lesson -> subscriptionService.hasActiveSubscriptionOn(member.getId(), lesson.getDateTime().toLocalDate()))
                .sorted(Comparator.comparing(Lesson::getDateTime))
                .limit(12)
                .map(lesson -> new MemberBookableLessonResponse(
                        lesson.getId(),
                        lesson.getName(),
                        lesson.getTrainer().getId(),
                        lesson.getTrainer().getName(),
                        lesson.getTrainer().getSpecialization(),
                        lesson.getDateTime(),
                        operationsService.getAvailableSeats(lesson)
                ))
                .toList();
    }
}
