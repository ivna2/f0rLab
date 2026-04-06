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
        return repo.findTopByMemberIdAndEndDateIsNotNullOrderByEndDateDesc(memberId)
                .orElse(null);
    }

    public List<Subscription> getAllForMember(Long memberId) {
        memberService.get(memberId);
        return repo.findByMemberIdOrderByEndDateDesc(memberId);
    }

    public boolean hasActiveSubscriptionOn(Long memberId, LocalDate date) {
        memberService.get(memberId);
        return repo.existsByMemberIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(memberId, date, date);
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
        if (startDate == null && endDate == null) {
            return;
        }
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Для абонемента нужно заполнить обе даты или оставить обе пустыми");
        }
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Дата окончания абонемента не может быть раньше даты начала");
        }
    }
}
