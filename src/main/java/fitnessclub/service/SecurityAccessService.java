package fitnessclub.service;

import fitnessclub.model.AppUser;
import fitnessclub.model.Booking;
import fitnessclub.model.Role;
import fitnessclub.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class SecurityAccessService {

    private final AppUserRepository appUserRepository;

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(UNAUTHORIZED, "Требуется авторизация");
        }

        return appUserRepository.findByLoginIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Текущий пользователь не найден"));
    }

    public void ensureCurrentMemberAccess(Long memberId) {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRoles().contains(Role.ADMIN)) {
            return;
        }
        if (currentUser.getMember() == null || !currentUser.getRoles().contains(Role.MEMBER)) {
            throw new ResponseStatusException(FORBIDDEN, "Операция недоступна для текущего пользователя");
        }
        if (!currentUser.getMember().getId().equals(memberId)) {
            throw new ResponseStatusException(FORBIDDEN, "Участник может работать только со своими данными");
        }
    }

    public void ensureCurrentMemberOwnsBooking(Booking booking) {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRoles().contains(Role.ADMIN)) {
            return;
        }
        if (currentUser.getMember() == null || !currentUser.getMember().getId().equals(booking.getMember().getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Участник может управлять только своими записями");
        }
    }
}
