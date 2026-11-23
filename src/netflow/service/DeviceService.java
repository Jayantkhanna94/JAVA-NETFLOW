package netflow.service;

import netflow.exception.DuplicateRecordException;
import netflow.exception.RecordNotFoundException;
import netflow.model.NetworkDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service class for managing {@link NetworkDevice} domain entities in memory.
 * Employs a {@link HashMap} for O(1) lookups by ID and a {@link HashSet}
 * to strictly enforce uniqueness of management IP addresses across all devices.
 */
public class DeviceService {

    // Map for fast O(1) lookups by device ID
    private final Map<String, NetworkDevice> deviceMap;

    // Set for enforcing unique management IP addresses across all devices
    private final Set<String> ipSet;

    public DeviceService() {
        this.deviceMap = new HashMap<>();
        this.ipSet = new HashSet<>();
    }

    /**
     * Registers a new device into memory.
     *
     * @param device The network device to add.
     * @throws DuplicateRecordException if a device with the same ID or management IP already exists.
     * @throws IllegalArgumentException if the device or its ID/IP is null or empty.
     */
    public void addDevice(NetworkDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }
        if (device.getId() == null || device.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID cannot be null or empty");
        }
        if (device.getManagementIp() == null || device.getManagementIp().trim().isEmpty()) {
            throw new IllegalArgumentException("Management IP cannot be null or empty");
        }

        String id = device.getId().trim();
        String ip = device.getManagementIp().trim();

        if (deviceMap.containsKey(id)) {
            throw new DuplicateRecordException("Device with ID '" + id + "' already exists.");
        }
        if (ipSet.contains(ip)) {
            throw new DuplicateRecordException("Device with Management IP '" + ip + "' already exists.");
        }

        deviceMap.put(id, device);
        ipSet.add(ip);
    }

    /**
     * Retrieves a device by its unique ID.
     *
     * @param id The device identifier.
     * @return The matched {@link NetworkDevice}.
     * @throws RecordNotFoundException if no device with the given ID exists.
     */
    public NetworkDevice getDevice(String id) {
        if (id == null || !deviceMap.containsKey(id.trim())) {
            throw new RecordNotFoundException("Device with ID '" + id + "' not found.");
        }
        return deviceMap.get(id.trim());
    }

    /**
     * Removes a device from memory by its ID, releasing its management IP.
     *
     * @param id The identifier of the device to remove.
     * @return The removed {@link NetworkDevice}.
     * @throws RecordNotFoundException if no device with the given ID exists.
     */
    public NetworkDevice removeDevice(String id) {
        if (id == null || !deviceMap.containsKey(id.trim())) {
            throw new RecordNotFoundException("Device with ID '" + id + "' not found for removal.");
        }
        NetworkDevice removed = deviceMap.remove(id.trim());
        if (removed != null && removed.getManagementIp() != null) {
            ipSet.remove(removed.getManagementIp().trim());
        }
        return removed;
    }

    /**
     * Returns an unmodifiable list of all currently registered devices.
     *
     * @return List of all {@link NetworkDevice} objects.
     */
    public List<NetworkDevice> getAllDevices() {
        return Collections.unmodifiableList(new ArrayList<>(deviceMap.values()));
    }

    /**
     * Returns the total count of registered devices.
     */
    public int getDeviceCount() {
        return deviceMap.size();
    }

    /**
     * Checks if a device exists by its ID.
     */
    public boolean containsDevice(String id) {
        return id != null && deviceMap.containsKey(id.trim());
    }

    /**
     * Checks if a management IP is already assigned to an existing device.
     */
    public boolean containsIp(String ip) {
        return ip != null && ipSet.contains(ip.trim());
    }
}
