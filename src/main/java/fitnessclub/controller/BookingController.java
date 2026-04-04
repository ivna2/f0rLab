package fitnessclub.controller;

import fitnessclub.dto.BookingRequest;
import fitnessclub.model.Booking;
import fitnessclub.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getAll() {
        return ResponseEntity.ok(bookingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> get(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.get(id));
    }

    @PostMapping
    public ResponseEntity<Booking> add(@Valid @RequestBody BookingRequest booking) {
        return ResponseEntity.status(CREATED).body(bookingService.add(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> update(@PathVariable Long id,
                                          @Valid @RequestBody BookingRequest booking) {
        return ResponseEntity.ok(bookingService.update(id, booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
