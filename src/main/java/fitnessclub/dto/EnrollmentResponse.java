package fitnessclub.dto;

import fitnessclub.model.Member;
import fitnessclub.model.Subscription;

public record EnrollmentResponse(
        Member member,
        Subscription subscription
) {
}
