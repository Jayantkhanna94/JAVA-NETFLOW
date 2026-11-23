package netflow.service;

import netflow.exception.DuplicateRecordException;
import netflow.exception.RecordNotFoundException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing {@link TrafficFlow} records in memory using Java Collections.
 * Employs an {@link ArrayList} as the underlying repository and provides
 * search, protocol filtering, and volume-based sorting operations.
 */
public class TrafficFlowService {

    // Repository list holding flow records
    private final List<TrafficFlow> flowList;

    public TrafficFlowService() {
        this.flowList = new ArrayList<>();
    }

    /**
     * Appends a new flow record into memory.
     *
     * @param flow The traffic flow record to add.
     * @throws DuplicateRecordException if a flow with the same flowId already exists.
     * @throws IllegalArgumentException if the flow is null.
     */
    public void addFlow(TrafficFlow flow) {
        if (flow == null) {
            throw new IllegalArgumentException("Traffic flow cannot be null");
        }
        if (flow.getFlowId() == null || flow.getFlowId().trim().isEmpty()) {
            throw new IllegalArgumentException("Flow ID cannot be null or empty");
        }

        String targetId = flow.getFlowId().trim();
        for (TrafficFlow existing : flowList) {
            if (existing.getFlowId().equalsIgnoreCase(targetId)) {
                throw new DuplicateRecordException("Traffic flow with ID '" + targetId + "' already exists.");
            }
        }
        flowList.add(flow);
    }

    /**
     * Retrieves a flow record by its unique flowId.
     *
     * @param flowId The unique identifier of the flow.
     * @return The matched {@link TrafficFlow}.
     * @throws RecordNotFoundException if no flow with the given ID exists.
     */
    public TrafficFlow getFlow(String flowId) {
        if (flowId != null) {
            String targetId = flowId.trim();
            for (TrafficFlow flow : flowList) {
                if (flow.getFlowId().equalsIgnoreCase(targetId)) {
                    return flow;
                }
            }
        }
        throw new RecordNotFoundException("Traffic flow with ID '" + flowId + "' not found.");
    }

    /**
     * Returns an unmodifiable view of all flow records.
     *
     * @return List containing all flows.
     */
    public List<TrafficFlow> getAllFlows() {
        return Collections.unmodifiableList(new ArrayList<>(flowList));
    }

    /**
     * Filters and returns all flow records matching the specified network protocol.
     *
     * @param protocol The {@link NetworkProtocol} to filter by.
     * @return List of flows matching the protocol.
     */
    public List<TrafficFlow> getFlowsByProtocol(NetworkProtocol protocol) {
        if (protocol == null) {
            return Collections.emptyList();
        }
        return flowList.stream()
                .filter(flow -> flow.getProtocol() == protocol)
                .collect(Collectors.toList());
    }

    /**
     * Returns the top high-volume flows sorted in descending order by {@code byteCount}.
     * Sorts a copy of the {@link ArrayList} using a {@link Comparator}.
     *
     * @param limit The maximum number of top flow records to return.
     * @return List of the top N flows with the highest byte count.
     */
    public List<TrafficFlow> getTopHighVolumeFlows(int limit) {
        if (limit <= 0 || flowList.isEmpty()) {
            return Collections.emptyList();
        }

        // Create an ArrayList copy to preserve original insertion order in flowList
        List<TrafficFlow> sortedCopy = new ArrayList<>(flowList);

        // Sort descending by byteCount using a Comparator
        sortedCopy.sort((f1, f2) -> {
            Long bytes1 = (f1.getByteCount() != null) ? f1.getByteCount() : 0L;
            Long bytes2 = (f2.getByteCount() != null) ? f2.getByteCount() : 0L;
            return Long.compare(bytes2, bytes1); // Descending order
        });

        int end = Math.min(limit, sortedCopy.size());
        return Collections.unmodifiableList(sortedCopy.subList(0, end));
    }

    /**
     * Returns the total count of recorded flows.
     */
    public int getFlowCount() {
        return flowList.size();
    }
}
