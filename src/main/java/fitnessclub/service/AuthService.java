package fitnessclub.service;

import fitnessclub.dto.AppUserResponse;
import fitnessclub.dto.ChangePasswordRequest;
import fitnessclub.dto.CreateAdminRequest;
import fitnessclub.dto.MemberRequest;
import fitnessclub.dto.RegisterRequest;
<<<<<<< HEAD
import fitnessclub.dto.UpdateAdminRequest;
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
import fitnessclub.model.AppUser;
import fitnessclub.model.Member;
import fitnessclub.model.NotificationType;
import fitnessclub.model.Role;
import fitnessclub.repository.AppUserRepository;
<<<<<<< HEAD
import fitnessclub.util.PhoneNumberUtils;
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAccessService securityAccessService;
    private final AdminNotificationService adminNotificationService;

    @Transactional
    public AppUserResponse register(RegisterRequest request) {
        validateRegistration(request.login(), request.name(), request.email(), request.password());
<<<<<<< HEAD
        boolean hasAdmins = appUserRepository.countByRolesContaining(Role.ADMIN) > 0;
        Member member = resolveMemberForRegistration(request.name(), request.email(), request.phone(), hasAdmins);
        Role initialRole = hasAdmins ? Role.MEMBER : Role.ADMIN;
=======
        Member member = resolveMemberForAccount(request.name(), request.email(), request.phone());
        Role initialRole = appUserRepository.count() == 0 ? Role.ADMIN : Role.MEMBER;
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
        return toResponse(saveUser(request.login(), request.email(), request.password(), Set.of(initialRole), member));
    }

    @Transactional
    public AppUserResponse createAdmin(CreateAdminRequest request) {
        validateRegistration(request.login(), request.name(), request.email(), request.password());
        Member member = resolveMemberForAccount(request.name(), request.email(), request.phone());
        return toResponse(saveUser(request.login(), request.email(), request.password(), Set.of(Role.ADMIN), member));
    }

    public AppUserResponse getCurrentUser() {
        return toResponse(securityAccessService.getCurrentUser());
    }

    public java.util.List<AppUserResponse> getAdmins() {
        return appUserRepository.findAllByOrderByIdAsc().stream()
                .filter(user -> user.getRoles().contains(Role.ADMIN))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void changeCurrentUserPassword(ChangePasswordRequest request) {
        AppUser currentUser = securityAccessService.getCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "Текущий пароль введен неверно");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Подтверждение нового пароля не совпадает");
        }
        if (passwordEncoder.matches(request.newPassword(), currentUser.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "Новый пароль должен отличаться от текущего");
        }

        String memberName = currentUser.getMember() != null ? currentUser.getMember().getName().trim().toLowerCase() : "";
        validatePassword(request.newPassword(), currentUser.getLogin().trim(), currentUser.getEmail().trim().toLowerCase(), memberName);
        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        appUserRepository.save(currentUser);

        adminNotificationService.notify(
                NotificationType.PASSWORD_CHANGED,
                "Смена пароля пользователя",
                "Пользователь " + currentUser.getLogin() + " изменил пароль своей учетной записи.",
                currentUser.getLogin()
        );
    }

    @Transactional
    public void deleteAdmin(Long adminId) {
        AppUser currentUser = securityAccessService.getCurrentUser();
        AppUser admin = appUserRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Администратор не найден: " + adminId));

        if (!admin.getRoles().contains(Role.ADMIN)) {
            throw new ResponseStatusException(BAD_REQUEST, "Выбранный пользователь не является администратором");
        }
        if (currentUser.getId().equals(adminId)) {
            throw new ResponseStatusException(FORBIDDEN, "Нельзя удалить текущую административную учетную запись");
        }

        long adminsCount = appUserRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(Role.ADMIN))
                .count();
        if (adminsCount <= 1) {
            throw new ResponseStatusException(BAD_REQUEST, "В системе должен остаться хотя бы один администратор");
        }

        Member linkedMember = admin.getMember();
        appUserRepository.delete(admin);
        if (linkedMember != null) {
            memberService.delete(linkedMember.getId());
        }
    }

<<<<<<< HEAD
    @Transactional
    public AppUserResponse updateAdmin(Long adminId, UpdateAdminRequest request) {
        AppUser admin = appUserRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Администратор не найден: " + adminId));

        if (!admin.getRoles().contains(Role.ADMIN)) {
            throw new ResponseStatusException(BAD_REQUEST, "Выбранный пользователь не является администратором");
        }

        String normalizedLogin = request.login().trim();
        String normalizedName = request.name().trim();
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedPhone = PhoneNumberUtils.normalizeRussianPhone(request.phone());

        appUserRepository.findByLoginIgnoreCase(normalizedLogin)
                .filter(existing -> !existing.getId().equals(adminId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Такой логин уже занят");
                });
        appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(adminId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Этот email уже используется");
                });

        if (normalizedLogin.length() < 4) {
            throw new ResponseStatusException(BAD_REQUEST, "Логин должен содержать минимум 4 символа");
        }
        if (normalizedName.length() < 2) {
            throw new ResponseStatusException(BAD_REQUEST, "Имя должно содержать минимум 2 символа");
        }

        String password = request.password() == null ? "" : request.password().trim();
        if (!password.isEmpty()) {
            validatePassword(password, normalizedLogin, normalizedEmail, normalizedName.toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(password));
        }

        Member member = admin.getMember();
        if (member != null) {
            memberService.update(member.getId(), new MemberRequest(normalizedName, normalizedEmail, normalizedPhone));
            admin.setMember(memberService.get(member.getId()));
        }

        admin.setLogin(normalizedLogin);
        admin.setEmail(normalizedEmail);
        return toResponse(appUserRepository.save(admin));
    }

=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
    private void validateRegistration(String login, String name, String email, String password) {
        String normalizedLogin = login.trim();
        String normalizedEmail = email.trim().toLowerCase();

        if (appUserRepository.existsByLoginIgnoreCase(normalizedLogin)) {
            throw new ResponseStatusException(CONFLICT, "Такой логин уже занят");
        }
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, "Этот email уже используется");
        }
        if (normalizedLogin.length() < 4) {
            throw new ResponseStatusException(BAD_REQUEST, "Логин должен содержать минимум 4 символа");
        }
        if (name == null || name.trim().length() < 2) {
            throw new ResponseStatusException(BAD_REQUEST, "Имя должно содержать минимум 2 символа");
        }
        validatePassword(password, normalizedLogin, normalizedEmail, name.trim().toLowerCase());
    }

    private Member resolveMemberForAccount(String name, String email, String phone) {
        String normalizedEmail = email.trim().toLowerCase();
        return memberService.findByEmail(normalizedEmail)
                .map(member -> memberService.update(member.getId(), new MemberRequest(name, normalizedEmail, phone)))
                .orElseGet(() -> memberService.add(new MemberRequest(name, normalizedEmail, phone)));
    }

<<<<<<< HEAD
    private Member resolveMemberForRegistration(String name, String email, String phone, boolean hasAdmins) {
        String normalizedEmail = email.trim().toLowerCase();

        if (!hasAdmins) {
            return resolveMemberForAccount(name, normalizedEmail, phone);
        }

        Member member = memberService.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        FORBIDDEN,
                        "Регистрация доступна только для участников, которых предварительно добавил администратор"
                ));

        if (appUserRepository.findByMemberId(member.getId()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Для этого участника учетная запись уже создана");
        }

        if (!member.getName().trim().equalsIgnoreCase(name.trim())) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Имя не совпадает с данными участника, созданного администратором"
            );
        }

        String normalizedRequestPhone = PhoneNumberUtils.normalizeRussianPhone(phone);
        String memberPhone = PhoneNumberUtils.normalizeRussianPhone(member.getPhone());
        if (memberPhone != null && normalizedRequestPhone != null && !memberPhone.equals(normalizedRequestPhone)) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Телефон не совпадает с данными участника, созданного администратором"
            );
        }

        return member;
    }

=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
    private void validatePassword(String password, String login, String email, String name) {
        String lowered = password.toLowerCase();
        boolean strong = password.length() >= 12
                && password.length() <= 128
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))
                && password.chars().noneMatch(Character::isWhitespace)
                && !lowered.contains(login.toLowerCase())
                && !lowered.contains(email.split("@")[0].toLowerCase())
                && !lowered.contains(name)
                && !hasLongRepeatedSequence(password)
                && !containsKeyboardSequence(lowered);
        if (!strong) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Пароль должен содержать от 12 до 128 символов, буквы в разных регистрах, цифру и спецсимвол, не содержать пробелы, личные данные и очевидные последовательности"
            );
        }
    }

    private boolean hasLongRepeatedSequence(String password) {
        int repeated = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                repeated++;
                if (repeated >= 4) {
                    return true;
                }
            } else {
                repeated = 1;
            }
        }
        return false;
    }

    private boolean containsKeyboardSequence(String password) {
        String[] weakSequences = {"qwerty", "asdf", "zxcv", "1234", "abcd", "password"};
        for (String sequence : weakSequences) {
            if (password.contains(sequence)) {
                return true;
            }
        }
        return false;
    }

    private AppUser saveUser(String login, String email, String password, Set<Role> roles, Member member) {
        AppUser appUser = new AppUser();
        appUser.setLogin(login.trim());
        appUser.setEmail(email.trim().toLowerCase());
        appUser.setPasswordHash(passwordEncoder.encode(password));
        appUser.setEnabled(true);
        appUser.setRoles(roles);
        appUser.setMember(member);
        appUser.setCreatedAt(LocalDateTime.now());
        return appUserRepository.save(appUser);
    }

    private AppUserResponse toResponse(AppUser appUser) {
        return new AppUserResponse(
                appUser.getId(),
                appUser.getLogin(),
                appUser.getEmail(),
                appUser.getMember() != null ? appUser.getMember().getId() : null,
<<<<<<< HEAD
                appUser.getMember() != null ? appUser.getMember().getName() : null,
                appUser.getMember() != null ? appUser.getMember().getPhone() : null,
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
                appUser.getRoles()
        );
    }
}
