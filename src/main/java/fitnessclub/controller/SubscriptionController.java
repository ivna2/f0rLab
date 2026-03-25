package fitnessclub.controller;

import fitnessclub.dto.SubscriptionRequest;
import fitnessclub.model.Subscription;
import fitnessclub.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<Subscription>> getAll() {
        return ResponseEntity.ok(subscriptionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> get(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.get(id));
    }

    @PostMapping
    public ResponseEntity<Subscription> add(@Valid @RequestBody SubscriptionRequest subscription) {
        return ResponseEntity.status(CREATED).body(subscriptionService.add(subscription));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> update(@PathVariable Long id,
                                               @Valid @RequestBody SubscriptionRequest subscription) {
        return ResponseEntity.ok(subscriptionService.update(id, subscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
