package netflow.exception;

/**
 * Exception thrown when a requested network record, device, or flow
 * cannot be found in the system.
 */
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
