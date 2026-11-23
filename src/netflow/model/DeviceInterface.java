package netflow.model;

import netflow.exception.InvalidDeviceException;
import netflow.util.ValidationUtils;

import java.util.Objects;

/**
 * Represents a physical or virtual network interface card (NIC/Port) associated with a device.
 */
public class DeviceInterface {

    private String id;
    private String deviceId;
    private String interfaceName;
    private String ipAddress;
    private String macAddress;
    private DeviceStatus status;

    /**
     * Minimal constructor for interface onboarding.
     */
    public DeviceInterface(String id, String deviceId, String interfaceName, String macAddress)
            throws InvalidDeviceException {
        this(id, deviceId, interfaceName, null, macAddress, DeviceStatus.ONLINE);
    }

    /**
     * Complete constructor validating interface configuration.
     */
    public DeviceInterface(String id, String deviceId, String interfaceName,
                           String ipAddress, String macAddress, DeviceStatus status)
            throws InvalidDeviceException {
        setId(id);
        setDeviceId(deviceId);
        setInterfaceName(interfaceName);
        setIpAddress(ipAddress);
        setMacAddress(macAddress);
        setStatus(status);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) throws InvalidDeviceException {
        if (ValidationUtils.isBlank(id)) {
            throw new InvalidDeviceException("Interface ID cannot be null or empty");
        }
        this.id = id.trim();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) throws InvalidDeviceException {
        if (ValidationUtils.isBlank(deviceId)) {
            throw new InvalidDeviceException("Device ID cannot be null or empty");
        }
        this.deviceId = deviceId.trim();
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) throws InvalidDeviceException {
        if (ValidationUtils.isBlank(interfaceName)) {
            throw new InvalidDeviceException("Interface name cannot be null or empty");
        }
        this.interfaceName = interfaceName.trim();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) throws InvalidDeviceException {
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            if (!ValidationUtils.isValidIpv4(ipAddress)) {
                throw new InvalidDeviceException("Invalid IPv4 address on interface " + id + ": " + ipAddress);
            }
            this.ipAddress = ipAddress.trim();
        } else {
            this.ipAddress = null;
        }
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) throws InvalidDeviceException {
        if (ValidationUtils.isBlank(macAddress)) {
            throw new InvalidDeviceException("MAC address cannot be null or empty");
        }
        if (!ValidationUtils.isValidMacAddress(macAddress)) {
            throw new InvalidDeviceException("Invalid MAC address format: " + macAddress);
        }
        this.macAddress = ValidationUtils.normalizeMacAddress(macAddress);
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) throws InvalidDeviceException {
        if (status == null) {
            throw new InvalidDeviceException("Interface status cannot be null");
        }
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceInterface that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(96);
        sb.append(interfaceName).append(" (ID: ").append(id).append(")")
          .append(" - MAC: ").append(macAddress)
          .append(" | IP: ").append(ipAddress != null ? ipAddress : "None")
          .append(" [").append(status).append("]");
        return sb.toString();
    }
}
