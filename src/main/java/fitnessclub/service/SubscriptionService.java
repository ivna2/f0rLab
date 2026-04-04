package fitnessclub.service;

import fitnessclub.dto.SubscriptionRequest;
import fitnessclub.model.Subscription;
import fitnessclub.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository repo;
    private final MemberService memberService;

    public List<Subscription> getAll() {
        return repo.findAll();
    }

    public Subscription get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Абонемент не найден: " + id));
    }

    public Subscription getLatestForMember(Long memberId) {
        memberService.get(memberId);
        return repo.findTopByMemberIdOrderByEndDateDesc(memberId)
                .orElse(null);
    }

    @Transactional
    public Subscription add(SubscriptionRequest request) {
        validateDates(request.startDate(), request.endDate());
        Subscription subscription = new Subscription(
                null,
                memberService.get(request.memberId()),
                request.startDate(),
                request.endDate()
        );
        return repo.save(subscription);
    }

    @Transactional
    public Subscription update(Long id, SubscriptionRequest request) {
        validateDates(request.startDate(), request.endDate());
        Subscription subscription = get(id);
        subscription.setMember(memberService.get(request.memberId()));
        subscription.setStartDate(request.startDate());
        subscription.setEndDate(request.endDate());
        return repo.save(subscription);
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        repo.deleteById(id);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Дата окончания абонемента не может быть раньше даты начала");
        }
    }
}
