package netflow.contract;

/**
 * Interface representing any network entity capable of providing
 * real-time operational monitoring diagnostics and summaries.
 */
public interface Monitorable {

    /**
     * Generates a formatted summary of the entity's current monitoring status.
     *
     * @return Formatted monitoring summary string.
     */
    String getMonitoringSummary();
}
