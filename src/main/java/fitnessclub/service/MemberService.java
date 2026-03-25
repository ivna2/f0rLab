package fitnessclub.service;

import fitnessclub.dto.MemberRequest;
import fitnessclub.dto.UpdateMemberProfileRequest;
import fitnessclub.model.AppUser;
import fitnessclub.model.Member;
import fitnessclub.model.Role;
import fitnessclub.repository.AppUserRepository;
import fitnessclub.repository.BookingRepository;
import fitnessclub.repository.MemberRepository;
import fitnessclub.repository.SubscriptionRepository;
import fitnessclub.util.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repo;
    private final AppUserRepository appUserRepository;
    private final BookingRepository bookingRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<Member> getAllMembers() {
        return repo.findAll();
    }

    public List<Member> getVisibleMembers() {
        java.util.Set<Long> adminMemberIds = appUserRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(Role.ADMIN))
                .map(user -> user.getMember() != null ? user.getMember().getId() : null)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        return repo.findAll().stream()
                .filter(member -> !adminMemberIds.contains(member.getId()))
                .toList();
    }

    public Member get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Участник не найден: " + id));
    }

    public java.util.Optional<Member> findByEmail(String email) {
        return repo.findByEmailIgnoreCase(email);
    }

    @Transactional
    public Member add(MemberRequest request) {
        ensureEmailIsUnique(request.email(), null);
        return repo.save(new Member(
                null,
                request.name().trim(),
                request.email().trim().toLowerCase(),
                PhoneNumberUtils.normalizeRussianPhone(request.phone()),
                null
        ));
    }

    @Transactional
    public Member update(Long id, MemberRequest request) {
        Member member = get(id);
        ensureEmailIsUnique(request.email(), id);
        member.setName(request.name().trim());
        member.setEmail(request.email().trim().toLowerCase());
        member.setPhone(PhoneNumberUtils.normalizeRussianPhone(request.phone()));
        return repo.save(member);
    }

    @Transactional
    public Member updateProfile(Long id, UpdateMemberProfileRequest request) {
        Member member = get(id);
        ensureEmailIsUnique(request.email(), id);
        member.setName(request.name().trim());
        member.setEmail(request.email().trim().toLowerCase());
        member.setPhone(PhoneNumberUtils.normalizeRussianPhone(request.phone()));
        return repo.save(member);
    }

    @Transactional
    public Member updatePhotoPath(Long id, String photoPath) {
        Member member = get(id);
        member.setPhotoPath(photoPath);
        return repo.save(member);
    }

    @Transactional
    public void delete(Long id) {
        Member member = get(id);

        long subscriptionsCount = subscriptionRepository.countByMemberId(id);
        if (subscriptionsCount > 0) {
            throw new ResponseStatusException(CONFLICT,
                    "\u041d\u0435\u043b\u044c\u0437\u044f \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u0430: \u0441 \u043d\u0438\u043c \u0441\u0432\u044f\u0437\u0430\u043d\u043e " + subscriptionsCount + " \u0430\u0431\u043e\u043d\u0435\u043c\u0435\u043d\u0442(\u043e\u0432)");
        }

        long bookingsCount = bookingRepository.countByMemberId(id);
        if (bookingsCount > 0) {
            throw new ResponseStatusException(CONFLICT,
                    "\u041d\u0435\u043b\u044c\u0437\u044f \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u0430: \u0441 \u043d\u0438\u043c \u0441\u0432\u044f\u0437\u0430\u043d\u043e " + bookingsCount + " \u0437\u0430\u043f\u0438\u0441\u044c(\u0435\u0439) \u043d\u0430 \u0437\u0430\u043d\u044f\u0442\u0438\u044f");
        }

        java.util.Optional<AppUser> linkedUser = appUserRepository.findByMemberId(id);
        if (linkedUser.isPresent()) {
            AppUser appUser = linkedUser.get();
            if (appUser.getRoles().contains(Role.ADMIN)) {
                throw new ResponseStatusException(CONFLICT,
                        "\u041d\u0435\u043b\u044c\u0437\u044f \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u0430, \u043f\u043e\u043a\u0430 \u043e\u043d \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d \u043a \u0443\u0447\u0435\u0442\u043d\u043e\u0439 \u0437\u0430\u043f\u0438\u0441\u0438 \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0430");
            }
            appUserRepository.delete(appUser);
        }

        repo.delete(member);
    }

    private void ensureEmailIsUnique(String email, Long currentMemberId) {
        boolean exists = currentMemberId == null
                ? repo.existsByEmailIgnoreCase(email)
                : repo.existsByEmailIgnoreCaseAndIdNot(email, currentMemberId);
        if (exists) {
            throw new ResponseStatusException(CONFLICT, "Участник с таким email уже существует: " + email);
        }
    }
}
