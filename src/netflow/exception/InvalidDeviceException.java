package netflow.exception;

/**
 * Checked exception thrown when device initialization, IP validation,
 * or device parameters fail validation checks.
 */
public class InvalidDeviceException extends Exception {

    public InvalidDeviceException(String message) {
        super(message);
    }

    public InvalidDeviceException(String message, Throwable cause) {
        super(message, cause);
    }
}
