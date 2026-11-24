package netflow.dao;

import netflow.database.DBConnection;
import netflow.exception.InvalidFlowException;
import netflow.exception.RecordNotFoundException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing {@link TrafficFlow} records in MySQL.
 * Accurately maps nullable Integer port wrapper types using JDBC {@code setNull()}
 * and {@link ResultSet#wasNull()}.
 */
public class TrafficFlowDAO {

    /**
     * Persists a traffic flow record into MySQL.
     * Properly maps nullable port numbers to SQL NULL for protocols like ICMP.
     *
     * @param flow The traffic flow record to insert.
     * @throws SQLException if a database access error occurs.
     */
    public void addFlow(TrafficFlow flow) throws SQLException {
        String sql = "INSERT INTO traffic_flows (flow_id, source_ip, dest_ip, protocol, source_port, dest_port, " +
                     "packet_count, byte_count, duration_ms, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flow.getFlowId());
            ps.setString(2, flow.getSourceIp());
            ps.setString(3, flow.getDestIp());
            ps.setString(4, flow.getProtocol().name());

            // Handle optional sourcePort wrapper type
            if (flow.getSourcePort() != null) {
                ps.setInt(5, flow.getSourcePort());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            // Handle optional destPort wrapper type
            if (flow.getDestPort() != null) {
                ps.setInt(6, flow.getDestPort());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setLong(7, flow.getPacketCount() != null ? flow.getPacketCount() : 0L);
            ps.setLong(8, flow.getByteCount() != null ? flow.getByteCount() : 0L);
            ps.setLong(9, flow.getDurationMs() != null ? flow.getDurationMs() : 0L);
            ps.setLong(10, flow.getTimestamp() != null ? flow.getTimestamp() : System.currentTimeMillis());

            ps.executeUpdate();
        }
    }

    /**
     * Retrieves all persisted traffic flow records ordered by timestamp descending.
     *
     * @return List of all stored {@link TrafficFlow} records.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidFlowException if stored data fails domain model validation.
     */
    public List<TrafficFlow> getAllFlows() throws SQLException, InvalidFlowException {
        String sql = "SELECT flow_id, source_ip, dest_ip, protocol, source_port, dest_port, " +
                     "packet_count, byte_count, duration_ms, timestamp " +
                     "FROM traffic_flows ORDER BY timestamp DESC";

        List<TrafficFlow> flows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                flows.add(mapRowToFlow(rs));
            }
        }
        return flows;
    }

    /**
     * Retrieves an individual flow record by its flow ID.
     *
     * @param flowId Unique flow identifier.
     * @return The matched {@link TrafficFlow}, or null if not found.
     * @throws SQLException if a database error occurs.
     * @throws InvalidFlowException if domain validation fails.
     */
    public TrafficFlow getFlow(String flowId) throws SQLException, InvalidFlowException {
        String sql = "SELECT flow_id, source_ip, dest_ip, protocol, source_port, dest_port, " +
                     "packet_count, byte_count, duration_ms, timestamp " +
                     "FROM traffic_flows WHERE flow_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flowId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFlow(rs);
                }
                throw new RecordNotFoundException("Traffic flow with ID '" + flowId + "' not found in database.");
            }
        }
    }

    private TrafficFlow mapRowToFlow(ResultSet rs) throws SQLException, InvalidFlowException {
        String flowId = rs.getString("flow_id");
        String srcIp = rs.getString("source_ip");
        String dstIp = rs.getString("dest_ip");
        NetworkProtocol protocol = NetworkProtocol.valueOf(rs.getString("protocol"));

        // Read nullable source port
        int srcPortVal = rs.getInt("source_port");
        Integer srcPort = rs.wasNull() ? null : srcPortVal;

        // Read nullable destination port
        int dstPortVal = rs.getInt("dest_port");
        Integer dstPort = rs.wasNull() ? null : dstPortVal;

        long packetCount = rs.getLong("packet_count");
        long byteCount = rs.getLong("byte_count");
        long durationMs = rs.getLong("duration_ms");
        long timestamp = rs.getLong("timestamp");

        return new TrafficFlow(flowId, srcIp, dstIp, protocol, srcPort, dstPort, packetCount, byteCount, durationMs, timestamp);
    }
}
