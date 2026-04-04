package fitnessclub.controller;

import fitnessclub.dto.*;
import fitnessclub.model.Booking;
import fitnessclub.model.Subscription;
import fitnessclub.service.OperationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final OperationsService operationsService;

    @PostMapping("/enroll-member")
    public ResponseEntity<EnrollmentResponse> enrollMember(@Valid @RequestBody EnrollMemberRequest request) {
        return ResponseEntity.status(CREATED).body(operationsService.enrollMember(request));
    }

    @PostMapping("/book-lesson")
    public ResponseEntity<Booking> bookLesson(@Valid @RequestBody BookLessonOperationRequest request) {
        return ResponseEntity.status(CREATED).body(operationsService.bookLesson(request));
    }

    @PostMapping("/bookings/{bookingId}/reschedule")
    public ResponseEntity<Booking> rescheduleBooking(@PathVariable Long bookingId,
                                                     @Valid @RequestBody RescheduleBookingRequest request) {
        return ResponseEntity.ok(operationsService.rescheduleBooking(bookingId, request));
    }

    @PostMapping("/renew-subscription")
    public ResponseEntity<Subscription> renewSubscription(@Valid @RequestBody RenewSubscriptionRequest request) {
        return ResponseEntity.status(CREATED).body(operationsService.renewSubscription(request));
    }

    @GetMapping("/members/{memberId}/schedule")
    public ResponseEntity<List<MemberScheduleItemResponse>> getMemberSchedule(@PathVariable Long memberId,
                                                                              @RequestParam LocalDate from,
                                                                              @RequestParam LocalDate to) {
        return ResponseEntity.ok(operationsService.getMemberSchedule(memberId, from, to));
    }

    @GetMapping("/trainers/{trainerId}/workload")
    public ResponseEntity<List<TrainerWorkloadLessonResponse>> getTrainerWorkload(@PathVariable Long trainerId,
                                                                                   @RequestParam LocalDate from,
                                                                                   @RequestParam LocalDate to) {
        return ResponseEntity.ok(operationsService.getTrainerWorkload(trainerId, from, to));
    }
}
