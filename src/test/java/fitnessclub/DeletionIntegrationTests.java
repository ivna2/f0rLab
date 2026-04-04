package fitnessclub;

import fitnessclub.model.AppUser;
import fitnessclub.model.Booking;
import fitnessclub.model.Lesson;
import fitnessclub.model.Member;
import fitnessclub.model.Role;
import fitnessclub.model.Subscription;
import fitnessclub.model.Trainer;
import fitnessclub.repository.AppUserRepository;
import fitnessclub.repository.BookingRepository;
import fitnessclub.repository.LessonRepository;
import fitnessclub.repository.MemberRepository;
import fitnessclub.repository.SubscriptionRepository;
import fitnessclub.repository.TrainerRepository;
import fitnessclub.service.LessonService;
import fitnessclub.service.MemberService;
import fitnessclub.service.TrainerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "DB_URL=jdbc:h2:mem:testdb-deletion;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "DB_DRIVER=org.h2.Driver",
        "DB_USERNAME=sa",
        "DB_PASSWORD=",
        "DDL_AUTO=create-drop"
})
@Transactional
class DeletionIntegrationTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void shouldDeleteMemberTogetherWithLinkedUserWhenNoDependentDataExists() {
        Member member = memberRepository.save(new Member(null, "Member Delete", "member.delete@example.com", null, null));
        AppUser user = new AppUser();
        user.setLogin("member-delete");
        user.setEmail("member.delete@example.com");
        user.setPasswordHash("hash");
        user.setEnabled(true);
        user.setRoles(Set.of(Role.MEMBER));
        user.setMember(member);
        user.setCreatedAt(LocalDateTime.now());
        user = appUserRepository.save(user);

        memberService.delete(member.getId());

        assertFalse(memberRepository.existsById(member.getId()));
        assertFalse(appUserRepository.existsById(user.getId()));
    }

    @Test
    void shouldRejectMemberDeletionWhenBookingsExist() {
        Member member = memberRepository.save(new Member(null, "Member Booking", "member.booking@example.com", null, null));
        Trainer trainer = trainerRepository.save(new Trainer(null, "Trainer", "Yoga"));
        Lesson lesson = lessonRepository.save(new Lesson(null, "Morning", trainer, LocalDateTime.now().plusDays(1), 10));
        bookingRepository.save(new Booking(null, member, lesson, lesson.getDateTime().toLocalDate()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> memberService.delete(member.getId()));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("\u0437\u0430\u043f\u0438\u0441"));
    }

    @Test
    void shouldRejectTrainerDeletionWhenLessonsExist() {
        Trainer trainer = trainerRepository.save(new Trainer(null, "Busy Trainer", "Pilates"));
        lessonRepository.save(new Lesson(null, "Lesson", trainer, LocalDateTime.now().plusDays(2), 12));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> trainerService.delete(trainer.getId()));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("\u0437\u0430\u043d\u044f\u0442"));
    }

    @Test
    void shouldRejectLessonDeletionWhenBookingsExist() {
        Member member = memberRepository.save(new Member(null, "Member Lesson", "member.lesson@example.com", null, null));
        Trainer trainer = trainerRepository.save(new Trainer(null, "Trainer Two", "Boxing"));
        Lesson lesson = lessonRepository.save(new Lesson(null, "Booked", trainer, LocalDateTime.now().plusDays(3), 8));
        bookingRepository.save(new Booking(null, member, lesson, LocalDate.now().plusDays(3)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> lessonService.delete(lesson.getId()));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("\u0431\u0440\u043e\u043d"));
    }

    @Test
    void shouldRejectMemberDeletionWhenSubscriptionsExist() {
        Member member = memberRepository.save(new Member(null, "Member Subscription", "member.subscription@example.com", null, null));
        subscriptionRepository.save(new Subscription(null, member, LocalDate.now(), LocalDate.now().plusMonths(1)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> memberService.delete(member.getId()));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("\u0430\u0431\u043e\u043d\u0435\u043c\u0435\u043d\u0442"));
    }
}
