package netflow.exception;

/**
 * Checked exception thrown when network traffic flow records contain
 * invalid port numbers, malformed IP addresses, or negative counters.
 */
public class InvalidFlowException extends Exception {

    public InvalidFlowException(String message) {
        super(message);
    }

    public InvalidFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
