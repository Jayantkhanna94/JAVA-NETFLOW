package netflow.model;

import netflow.exception.InvalidFlowException;
import netflow.util.ValidationUtils;

import java.util.Objects;

/**
 * Represents a network traffic flow telemetry record.
 * Uses wrapper types (e.g., {@link Integer} for optional ports) to handle protocols
 * where ports are absent (such as ICMP).
 */
public class TrafficFlow {

    private String flowId;
    private String sourceIp;
    private String destIp;
    private NetworkProtocol protocol;
    private Integer sourcePort;
    private Integer destPort;
    private Long packetCount;
    private Long byteCount;
    private Long durationMs;
    private Long timestamp;

    /**
     * Minimal constructor with zero counts and current timestamp.
     */
    public TrafficFlow(String flowId, String sourceIp, String destIp,
                       NetworkProtocol protocol, Integer sourcePort, Integer destPort)
            throws InvalidFlowException {
        this(flowId, sourceIp, destIp, protocol, sourcePort, destPort, 0L, 0L, 0L, System.currentTimeMillis());
    }

    /**
     * Complete constructor validating telemetry fields, port bounds, and non-negative counts.
     */
    public TrafficFlow(String flowId, String sourceIp, String destIp,
                       NetworkProtocol protocol, Integer sourcePort, Integer destPort,
                       Long packetCount, Long byteCount, Long durationMs, Long timestamp)
            throws InvalidFlowException {
        setFlowId(flowId);
        setSourceIp(sourceIp);
        setDestIp(destIp);
        setProtocol(protocol);
        setSourcePort(sourcePort);
        setDestPort(destPort);
        setPacketCount(packetCount);
        setByteCount(byteCount);
        setDurationMs(durationMs);
        setTimestamp(timestamp);
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) throws InvalidFlowException {
        if (ValidationUtils.isBlank(flowId)) {
            throw new InvalidFlowException("Flow ID cannot be null or empty");
        }
        this.flowId = flowId.trim();
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) throws InvalidFlowException {
        if (ValidationUtils.isBlank(sourceIp)) {
            throw new InvalidFlowException("Source IP cannot be null or empty");
        }
        if (!ValidationUtils.isValidIpv4(sourceIp)) {
            throw new InvalidFlowException("Invalid IPv4 address format for source IP: " + sourceIp);
        }
        this.sourceIp = sourceIp.trim();
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) throws InvalidFlowException {
        if (ValidationUtils.isBlank(destIp)) {
            throw new InvalidFlowException("Destination IP cannot be null or empty");
        }
        if (!ValidationUtils.isValidIpv4(destIp)) {
            throw new InvalidFlowException("Invalid IPv4 address format for destination IP: " + destIp);
        }
        this.destIp = destIp.trim();
    }

    public NetworkProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(NetworkProtocol protocol) throws InvalidFlowException {
        if (protocol == null) {
            throw new InvalidFlowException("Network protocol cannot be null");
        }
        this.protocol = protocol;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) throws InvalidFlowException {
        if (sourcePort != null && (sourcePort < 0 || sourcePort > 65535)) {
            throw new InvalidFlowException("Source port must be between 0 and 65535 (got: " + sourcePort + ")");
        }
        this.sourcePort = sourcePort;
    }

    public Integer getDestPort() {
        return destPort;
    }

    public void setDestPort(Integer destPort) throws InvalidFlowException {
        if (destPort != null && (destPort < 0 || destPort > 65535)) {
            throw new InvalidFlowException("Destination port must be between 0 and 65535 (got: " + destPort + ")");
        }
        this.destPort = destPort;
    }

    public Long getPacketCount() {
        return packetCount;
    }

    public void setPacketCount(Long packetCount) throws InvalidFlowException {
        if (packetCount == null || packetCount < 0) {
            throw new InvalidFlowException("Packet count must be non-null and >= 0 (got: " + packetCount + ")");
        }
        this.packetCount = packetCount;
    }

    public Long getByteCount() {
        return byteCount;
    }

    public void setByteCount(Long byteCount) throws InvalidFlowException {
        if (byteCount == null || byteCount < 0) {
            throw new InvalidFlowException("Byte count must be non-null and >= 0 (got: " + byteCount + ")");
        }
        this.byteCount = byteCount;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) throws InvalidFlowException {
        if (durationMs == null || durationMs < 0) {
            throw new InvalidFlowException("Duration (ms) must be non-null and >= 0 (got: " + durationMs + ")");
        }
        this.durationMs = durationMs;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) throws InvalidFlowException {
        if (timestamp == null || timestamp < 0) {
            throw new InvalidFlowException("Timestamp must be non-null and >= 0 (got: " + timestamp + ")");
        }
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrafficFlow that)) return false;
        return Objects.equals(flowId, that.flowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(140);
        sb.append("[").append(flowId).append("] ")
          .append(sourceIp).append(sourcePort != null ? ":" + sourcePort : "")
          .append(" -> ")
          .append(destIp).append(destPort != null ? ":" + destPort : "")
          .append(" [").append(protocol).append("]")
          .append(" | Packets: ").append(packetCount)
          .append(" | Bytes: ").append(byteCount)
          .append(" | Duration: ").append(durationMs).append("ms");
        return sb.toString();
    }
}
