package netflow.dao;

import netflow.database.DBConnection;
import netflow.exception.InvalidDeviceException;
import netflow.exception.RecordNotFoundException;
import netflow.model.DeviceStatus;
import netflow.model.DeviceType;
import netflow.model.NetworkDevice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing {@link NetworkDevice} persistence in MySQL.
 * Employs parameterized {@link PreparedStatement} queries, try-with-resources,
 * and explicit JDBC transaction management (commit/rollback).
 */
public class DeviceDAO {

    /**
     * Inserts a new network device record into the database.
     *
     * @param device The network device to persist.
     * @throws SQLException if a database error occurs (e.g. unique constraint violation).
     */
    public void addDevice(NetworkDevice device) throws SQLException {
        String sql = "INSERT INTO network_devices (device_id, device_name, device_type, management_ip, location, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setDevicePreparedStatement(ps, device);
            ps.executeUpdate();
        }
    }

    /**
     * Retrieves a network device by its unique ID.
     *
     * @param id The device identifier.
     * @return The matched {@link NetworkDevice}, or null if not found.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidDeviceException if database data fails domain validation.
     */
    public NetworkDevice getDevice(String id) throws SQLException, InvalidDeviceException {
        String sql = "SELECT device_id, device_name, device_type, management_ip, location, status " +
                     "FROM network_devices WHERE device_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDevice(rs);
                }
                throw new RecordNotFoundException("Device with ID '" + id + "' not found in database.");
            }
        }
    }

    /**
     * Updates the operational status of an existing device.
     *
     * @param id The device identifier.
     * @param status The new {@link DeviceStatus}.
     * @return true if a row was updated; false if device ID was not found.
     * @throws SQLException if a database access error occurs.
     */
    public boolean updateDeviceStatus(String id, DeviceStatus status) throws SQLException {
        String sql = "UPDATE network_devices SET status = ? WHERE device_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status != null ? status.name() : DeviceStatus.OFFLINE.name());
            ps.setString(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a device record by its unique ID.
     *
     * @param id The identifier of the device to delete.
     * @return true if a row was deleted; false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean deleteDevice(String id) throws SQLException {
        String sql = "DELETE FROM network_devices WHERE device_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all network devices registered in the database.
     *
     * @return List of all persisted {@link NetworkDevice} objects.
     * @throws SQLException if a database access error occurs.
     * @throws InvalidDeviceException if any row fails domain model validation.
     */
    public List<NetworkDevice> getAllDevices() throws SQLException, InvalidDeviceException {
        String sql = "SELECT device_id, device_name, device_type, management_ip, location, status FROM network_devices";
        List<NetworkDevice> devices = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                devices.add(mapRowToDevice(rs));
            }
        }
        return devices;
    }

    /**
     * Demonstrates ACID transaction control (commit and rollback).
     * Disables auto-commit, inserts device1, conditionally triggers an exception,
     * inserts device2, and commits or rolls back accordingly.
     *
     * @param device1 The first device to insert.
     * @param device2 The second device to insert.
     * @param forceFail If true, triggers a simulated failure to demonstrate rollback.
     * @throws SQLException on database error or simulated failure.
     */
    public void addDeviceWithTransactionDemo(NetworkDevice device1, NetworkDevice device2, boolean forceFail)
            throws SQLException {

        String sql = "INSERT INTO network_devices (device_id, device_name, device_type, management_ip, location, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();

            // Begin transaction: disable auto-commit
            conn.setAutoCommit(false);

            // Step 1: Insert first device
            try (PreparedStatement ps1 = conn.prepareStatement(sql)) {
                setDevicePreparedStatement(ps1, device1);
                ps1.executeUpdate();
            }

            // Step 2: Simulate failure condition if requested
            if (forceFail) {
                throw new SQLException("Simulated transaction failure triggered (forceFail=true). Rolling back!");
            }

            // Step 3: Insert second device
            try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                setDevicePreparedStatement(ps2, device2);
                ps2.executeUpdate();
            }

            // Commit transaction if all operations succeed
            conn.commit();

        } catch (SQLException | RuntimeException e) {
            // Roll back all changes if any error occurs
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;

        } finally {
            // Restore auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                }
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private void setDevicePreparedStatement(PreparedStatement ps, NetworkDevice device) throws SQLException {
        ps.setString(1, device.getId());
        ps.setString(2, device.getName());
        ps.setString(3, device.getDeviceType().name());
        ps.setString(4, device.getManagementIp());
        ps.setString(5, device.getLocation());
        ps.setString(6, device.getStatus().name());
    }

    private NetworkDevice mapRowToDevice(ResultSet rs) throws SQLException, InvalidDeviceException {
        String id = rs.getString("device_id");
        String name = rs.getString("device_name");
        String typeStr = rs.getString("device_type");
        String ip = rs.getString("management_ip");
        String location = rs.getString("location");
        String statusStr = rs.getString("status");

        DeviceType type = DeviceType.valueOf(typeStr);
        DeviceStatus status = DeviceStatus.valueOf(statusStr);

        return new NetworkDevice(id, name, type, ip, location, status);
    }
}
