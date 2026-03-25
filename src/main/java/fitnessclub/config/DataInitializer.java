package fitnessclub.config;

import fitnessclub.model.AppUser;
import fitnessclub.model.Role;
import fitnessclub.repository.AdminNotificationRepository;
import fitnessclub.repository.AppUserRepository;
import fitnessclub.repository.BookingRepository;
import fitnessclub.repository.LessonRepository;
import fitnessclub.repository.MemberRepository;
import fitnessclub.repository.SubscriptionRepository;
import fitnessclub.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final Path CLEANUP_MARKER = Path.of("data", "postgres-cleanup.done");

    private final MemberRepository memberRepository;
    private final AppUserRepository appUserRepository;
    private final TrainerRepository trainerRepository;
    private final LessonRepository lessonRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BookingRepository bookingRepository;
    private final AdminNotificationRepository adminNotificationRepository;

    @Bean
    CommandLineRunner seedData() {
        return args -> cleanupLegacyDemoDataOnce();
    }

    private void cleanupLegacyDemoDataOnce() throws IOException {
        if (Files.exists(CLEANUP_MARKER)) {
            return;
        }

        bookingRepository.deleteAllInBatch();
        subscriptionRepository.deleteAllInBatch();
        lessonRepository.deleteAllInBatch();
        trainerRepository.deleteAllInBatch();
        adminNotificationRepository.deleteAllInBatch();

        List<AppUser> users = appUserRepository.findAll();
        Set<Long> adminMemberIds = users.stream()
                .filter(user -> user.getRoles().contains(Role.ADMIN))
                .map(AppUser::getMember)
                .filter(member -> member != null)
                .map(member -> member.getId())
                .collect(java.util.stream.Collectors.toSet());

        List<Long> nonAdminUserIds = users.stream()
                .filter(user -> !user.getRoles().contains(Role.ADMIN))
                .map(AppUser::getId)
                .toList();

        if (!nonAdminUserIds.isEmpty()) {
            appUserRepository.deleteAllByIdInBatch(nonAdminUserIds);
        }

        List<Long> removableMemberIds = memberRepository.findAll().stream()
                .map(member -> member.getId())
                .filter(memberId -> !adminMemberIds.contains(memberId))
                .toList();

        if (!removableMemberIds.isEmpty()) {
            memberRepository.deleteAllByIdInBatch(removableMemberIds);
        }

        Files.createDirectories(CLEANUP_MARKER.getParent());
        Files.writeString(CLEANUP_MARKER, "cleanup-done");
    }
}
