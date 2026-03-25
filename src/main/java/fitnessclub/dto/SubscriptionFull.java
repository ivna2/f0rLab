package fitnessclub.dto;

import fitnessclub.model.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SubscriptionFull {
    private Long id;
    private Member member;
    private LocalDate startDate;
    private LocalDate endDate;
}
