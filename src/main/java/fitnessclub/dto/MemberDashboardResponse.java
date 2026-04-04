package fitnessclub.dto;

import java.time.LocalDate;
import java.util.List;

public record MemberDashboardResponse(
        String login,
        String name,
        String email,
        String phone,
        String photoPath,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        List<MemberBookingCardResponse> upcomingBookings
) {
}
