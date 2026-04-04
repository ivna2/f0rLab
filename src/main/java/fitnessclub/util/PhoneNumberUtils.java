package fitnessclub.util;

public final class PhoneNumberUtils {

    private PhoneNumberUtils() {
    }

    public static String normalizeRussianPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return null;
        }

        String digits = rawPhone.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }

        if (digits.length() == 11 && digits.startsWith("8")) {
            digits = "7" + digits.substring(1);
        } else if (digits.length() == 10) {
            digits = "7" + digits;
        }

        if (digits.length() != 11 || !digits.startsWith("7")) {
            return rawPhone.trim();
        }

        return String.format(
                "+7 (%s) %s-%s-%s",
                digits.substring(1, 4),
                digits.substring(4, 7),
                digits.substring(7, 9),
                digits.substring(9, 11)
        );
    }
}
