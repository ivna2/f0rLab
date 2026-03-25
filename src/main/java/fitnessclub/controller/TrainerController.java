package fitnessclub.controller;

import fitnessclub.dto.TrainerRequest;
import fitnessclub.model.Trainer;
import fitnessclub.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @GetMapping
    public ResponseEntity<List<Trainer>> getAll() {
        return ResponseEntity.ok(trainerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trainer> get(@PathVariable Long id) {
        return ResponseEntity.ok(trainerService.get(id));
    }

    @PostMapping
    public ResponseEntity<Trainer> add(@Valid @RequestBody TrainerRequest trainer) {
        return ResponseEntity.status(CREATED).body(trainerService.add(trainer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trainer> update(@PathVariable Long id,
                                          @Valid @RequestBody TrainerRequest trainer) {
        return ResponseEntity.ok(trainerService.update(id, trainer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
