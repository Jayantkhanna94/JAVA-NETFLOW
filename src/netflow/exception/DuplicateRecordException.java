package netflow.exception;

/**
 * Exception thrown when attempting to add or register an entity whose
 * identifier or unique attribute already exists.
 */
public class DuplicateRecordException extends RuntimeException {

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
