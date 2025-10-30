/**
 * Small ID utility helpers used by the GUI to normalize and validate TP-style IDs.
 * Kept minimal and dependency-free so it compiles with the project's default setup.
 */
public class IdUtils {
    /**
     * Normalize a raw ID input into the form TP<digits> when the digit count matches requiredDigits.
     * Returns the normalized ID (e.g. TP123456) or null if input is invalid.
     */
    public static String normalizeTpId(String raw, int requiredDigits) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() != requiredDigits) return null;
        return "TP" + digits;
    }

    /**
     * Extract digits from input. Returns empty string if none.
     */
    public static String extractDigits(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("\\D", "");
    }
}
