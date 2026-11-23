package netflow.model;

import netflow.contract.Monitorable;
import netflow.exception.InvalidDeviceException;
import netflow.util.ValidationUtils;

import java.util.Objects;

/**
 * Represents a monitored network device (e.g., Router, Switch, Server).
 * Strictly encapsulates device fields and validates IP address and string inputs.
 * Implements {@link Monitorable}.
 */
public class NetworkDevice implements Monitorable {

    private String id;
    private String name;
    private DeviceType deviceType;
    private String managementIp;
    private String location;
    private DeviceStatus status;

    /**
     * Minimal constructor defaulting location to "Unassigned" and status to ONLINE.
     */
    public NetworkDevice(String id, String name, DeviceType deviceType, String managementIp)
            throws InvalidDeviceException {
        this(id, name, deviceType, managementIp, "Unassigned", DeviceStatus.ONLINE);
    }

    /**
     * Complete constructor validating all required device parameters.
     */
    public NetworkDevice(String id, String name, DeviceType deviceType,
                         String managementIp, String location, DeviceStatus status)
            throws InvalidDeviceException {
        setId(id);
        setName(name);
        setDeviceType(deviceType);
        setManagementIp(managementIp);
        setLocation(location);
        setStatus(status);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) throws InvalidDeviceException {
        if (ValidationUtils.isBlank(id)) {
            throw new InvalidDeviceException("Device ID cannot be null or empty");
        }
        this.id = id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidDeviceException {
        if (ValidationUtils.isBlank(name)) {
            throw new InvalidDeviceException("Device name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) throws InvalidDeviceException {
        if (deviceType == null) {
            throw new InvalidDeviceException("Device type cannot be null");
        }
        this.deviceType = deviceType;
    }

    public String getManagementIp() {
        return managementIp;
    }

    public void setManagementIp(String managementIp) throws InvalidDeviceException {
        if (ValidationUtils.isBlank(managementIp)) {
            throw new InvalidDeviceException("Management IP cannot be null or empty");
        }
        if (!ValidationUtils.isValidIpv4(managementIp)) {
            throw new InvalidDeviceException("Invalid IPv4 address format for management IP: " + managementIp);
        }
        this.managementIp = managementIp.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = (location != null && !location.trim().isEmpty()) ? location.trim() : "Unassigned";
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) throws InvalidDeviceException {
        if (status == null) {
            throw new InvalidDeviceException("Device status cannot be null");
        }
        this.status = status;
    }

    /**
     * Implementation of {@link Monitorable}.
     * Constructs a diagnostic summary string using {@link StringBuilder}.
     */
    @Override
    public String getMonitoringSummary() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("[").append(status).append("] ")
          .append("Device ").append(id)
          .append(" ('").append(name).append("', ")
          .append(deviceType).append(")")
          .append(" - IP: ").append(managementIp)
          .append(" | Location: ").append(location);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkDevice that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getMonitoringSummary();
    }
}
