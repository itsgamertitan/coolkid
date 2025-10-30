/**
 * Simple test runner for IdUtils. No external test framework required.
 * Run with: java -cp bin IdUtilsTest
 */
public class IdUtilsTest {
    public static void main(String[] args) {
        int failures = 0;
        failures += check("123456", 6, "TP123456");
        failures += check("TP123456", 6, "TP123456");
        failures += check("tp123456", 6, "TP123456");
        failures += check(" 123-456 ", 6, "TP123456");
        failures += check("12345", 6, null); // too short
        failures += check("abc", 6, null); // no digits

        if (failures > 0) {
            System.err.println("IdUtilsTest: FAILURES=" + failures);
            System.exit(2);
        } else {
            System.out.println("IdUtilsTest: OK");
        }
    }

    private static int check(String input, int digits, String expected) {
        String out = IdUtils.normalizeTpId(input, digits);
        boolean ok = (out == null ? expected == null : out.equals(expected));
        if (!ok) {
            System.err.println("FAIL: input='" + input + "' -> '" + out + "', expected='" + expected + "'");
            return 1;
        }
        return 0;
    }
}
