package netflow.util;

import java.util.regex.Pattern;

/**
 * Common string, IP, MAC, and port validation helper routines.
 */
public final class ValidationUtils {

    // Regex for standard IPv4 octet boundaries (0-255)
    private static final String IPV4_SEGMENT = "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^" + IPV4_SEGMENT + "(\\." + IPV4_SEGMENT + "){3}$"
    );

    // Regex for 48-bit MAC addresses (colon or hyphen separated)
    private static final Pattern MAC_PATTERN = Pattern.compile(
            "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    );

    private ValidationUtils() {
        // Utility class: prevent instantiation
    }

    public static boolean isValidIpv4(String ip) {
        if (ip == null) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip.trim()).matches();
    }

    public static boolean isValidMacAddress(String mac) {
        if (mac == null) {
            return false;
        }
        return MAC_PATTERN.matcher(mac.trim()).matches();
    }

    public static String normalizeMacAddress(String mac) {
        if (!isValidMacAddress(mac)) {
            throw new IllegalArgumentException("Invalid MAC address: " + mac);
        }
        return mac.trim().replace("-", ":").toUpperCase();
    }

    public static boolean isValidPort(Integer port) {
        // Port is valid if null (for protocols without ports) or within 0..65535
        return port == null || (port >= 0 && port <= 65535);
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
