package fitnessclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fitnessclub.model.Trainer;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {}
