package fitnessclub;

import fitnessclub.model.AdminNotification;
<<<<<<< HEAD
import fitnessclub.model.AppUser;
import fitnessclub.model.Member;
import fitnessclub.model.NotificationType;
import fitnessclub.model.Role;
import fitnessclub.repository.AdminNotificationRepository;
import fitnessclub.repository.AppUserRepository;
import fitnessclub.repository.MemberRepository;
=======
import fitnessclub.model.NotificationType;
import fitnessclub.repository.AdminNotificationRepository;
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.Set;
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "DB_URL=jdbc:h2:mem:testdb-security;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "DB_DRIVER=org.h2.Driver",
        "DB_USERNAME=sa",
        "DB_PASSWORD=",
        "DDL_AUTO=create-drop"
})
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminNotificationRepository adminNotificationRepository;

<<<<<<< HEAD
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MemberRepository memberRepository;

=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
    @Test
    void shouldDenyProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectWeakPasswordDuringRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "login": "weak-user",
                                  "name": "Weak User",
                                  "email": "weak.user@example.com",
                                  "password": "weak"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "\u041f\u0430\u0440\u043e\u043b\u044c \u0434\u043e\u043b\u0436\u0435\u043d \u0441\u043e\u0434\u0435\u0440\u0436\u0430\u0442\u044c \u043e\u0442 12 \u0434\u043e 128 \u0441\u0438\u043c\u0432\u043e\u043b\u043e\u0432, \u0431\u0443\u043a\u0432\u044b \u0432 \u0440\u0430\u0437\u043d\u044b\u0445 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0445, \u0446\u0438\u0444\u0440\u0443 \u0438 \u0441\u043f\u0435\u0446\u0441\u0438\u043c\u0432\u043e\u043b, \u043d\u0435 \u0441\u043e\u0434\u0435\u0440\u0436\u0430\u0442\u044c \u043f\u0440\u043e\u0431\u0435\u043b\u044b, \u043b\u0438\u0447\u043d\u044b\u0435 \u0434\u0430\u043d\u043d\u044b\u0435 \u0438 \u043e\u0447\u0435\u0432\u0438\u0434\u043d\u044b\u0435 \u043f\u043e\u0441\u043b\u0435\u0434\u043e\u0432\u0430\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u0438"));
    }

    @Test
    @WithMockUser(username = "member-view", roles = {"MEMBER"})
    void shouldAllowMemberToReadLessons() throws Exception {
        mockMvc.perform(get("/lessons"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void shouldAllowAdminToClearNotifications() throws Exception {
        adminNotificationRepository.save(new AdminNotification(
                null,
                NotificationType.BOOKING_CANCELLED,
                "Test title",
                "Test message",
                "admin-user",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/admin/notifications/clear").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        Assertions.assertEquals(0, adminNotificationRepository.count());
    }
<<<<<<< HEAD

    @Test
    void shouldPromoteFirstRegisteredUserToAdminWhenDatabaseHasNoAdmins() throws Exception {
        Member existingMember = memberRepository.save(new Member(
                null,
                "Existing Member",
                "existing.member@example.com",
                "+79990000000",
                null
        ));

        appUserRepository.save(new AppUser(
                null,
                "member-only",
                "member.only@example.com",
                "$2a$10$DowJonesIndex1234567890123456789012345678901234567",
                true,
                Set.of(Role.MEMBER),
                existingMember,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "login": "sql-admin",
                                  "name": "SQL Admin",
                                  "email": "sql.admin@example.com",
                                  "phone": "+79991112233",
                                  "password": "StrongAdmin!2026"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    void shouldRejectRegistrationWhenMemberWasNotCreatedByAdmin() throws Exception {
        Member adminMember = memberRepository.save(new Member(
                null,
                "Bootstrap Admin",
                "bootstrap.admin@example.com",
                "+79990000001",
                null
        ));

        appUserRepository.save(new AppUser(
                null,
                "bootstrap-admin",
                "bootstrap.admin@example.com",
                "$2a$10$DowJonesIndex1234567890123456789012345678901234567",
                true,
                Set.of(Role.ADMIN),
                adminMember,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "login": "plain-member",
                                  "name": "Plain Member",
                                  "email": "plain.member@example.com",
                                  "phone": "+79992223344",
                                  "password": "StrongMember!2026"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Регистрация доступна только для участников, которых предварительно добавил администратор"));
    }

    @Test
    void shouldRegisterMemberAccountWhenAdminCreatedMemberInAdvance() throws Exception {
        Member adminMember = memberRepository.save(new Member(
                null,
                "Bootstrap Admin",
                "bootstrap.admin@example.com",
                "+79990000001",
                null
        ));

        appUserRepository.save(new AppUser(
                null,
                "bootstrap-admin",
                "bootstrap.admin@example.com",
                "$2a$10$DowJonesIndex1234567890123456789012345678901234567",
                true,
                Set.of(Role.ADMIN),
                adminMember,
                LocalDateTime.now()
        ));

        memberRepository.save(new Member(
                null,
                "Plain Member",
                "plain.member@example.com",
                "+7 (999) 222-33-44",
                null
        ));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "login": "plain-member",
                                  "name": "Plain Member",
                                  "email": "plain.member@example.com",
                                  "phone": "+79992223344",
                                  "password": "StrongMember!2026"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("MEMBER"));
    }
=======
>>>>>>> 524a0e1364287037ac59b4a573e1ba2a6b60e60d
}
