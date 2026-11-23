
# NetFlow System - Test Evidence & Traceability Matrix

## 1. Test Cases Table (Milestones 1 & 2)
=======
# NetFlow System - Milestone 1 Test Evidence & Traceability Matrix

## 1. Milestone 1 Test Cases Table


| Test ID | Feature Tested | Input Data | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **T01** | Valid Device Creation & `Monitorable` Summary | `id="DEV-RTR-01"`, `name="Core-Gateway-East"`, `deviceType=ROUTER`, `managementIp="192.168.1.1"`, `location="Building-A Rack-04"`, `status=ONLINE` | Object instantiated with attributes matching input; `getMonitoringSummary()` returns formatted string containing status, ID, name, type, and IP. | `NetworkDevice` instantiated successfully. `getMonitoringSummary()` outputs: `[ONLINE] Device DEV-RTR-01 ('Core-Gateway-East', ROUTER) - IP: 192.168.1.1 \| Location: Building-A Rack-04` | **PASS** |
| **T02** | Valid Traffic Flow Creation & Port Range | `flowId="FLW-9001"`, `sourceIp="192.168.1.50"`, `destIp="10.0.0.1"`, `protocol=TCP`, `sourcePort=52134`, `destPort=443`, `packetCount=15420`, `byteCount=18456000`, `durationMs=12500` | Flow record successfully created; all telemetry fields and wrappers correctly populated; `toString()` returns formatted telemetry string. | `TrafficFlow` instantiated successfully. Output: `[FLW-9001] 192.168.1.50:52134 -> 10.0.0.1:443 [TCP] \| Packets: 15420 \| Bytes: 18456000 \| Duration: 12500ms` | **PASS** |
| **T04** | Invalid Device: Malformed Management IP Address | `id="DEV-BAD-01"`, `name="Edge-Switch"`, `deviceType=SWITCH`, `managementIp="999.300.1.1"` | Regex validation fails; constructor aborts instantiation and throws `InvalidDeviceException`. | Caught `InvalidDeviceException`: `"Invalid IPv4 address format for management IP: 999.300.1.1"` | **PASS** |
| **T05** | Invalid Device: Blank / Empty Device Name | `id="DEV-BAD-02"`, `name="   "`, `deviceType=SERVER`, `managementIp="10.0.0.5"` | Blank string check fails; constructor aborts instantiation and throws `InvalidDeviceException`. | Caught `InvalidDeviceException`: `"Device name cannot be null or empty"` | **PASS** |
| **T06** | Invalid Flow: Port Out of Valid Range | `flowId="FLW-BAD-01"`, `sourceIp="192.168.1.1"`, `destIp="10.0.0.1"`, `protocol=TCP`, `sourcePort=70000`, `destPort=80` | Port validation check (`0 <= port <= 65535`) fails; constructor throws `InvalidFlowException`. | Caught `InvalidFlowException`: `"Source port must be between 0 and 65535 (got: 70000)"` | **PASS** |
| **T07** | Invalid Flow: Negative Byte Count | `flowId="FLW-BAD-02"`, `sourceIp="192.168.1.1"`, `destIp="10.0.0.1"`, `protocol=UDP`, `sourcePort=5300`, `destPort=53`, `packetCount=100`, `byteCount=-500` | Non-negative telemetry validation fails; constructor throws `InvalidFlowException`. | Caught `InvalidFlowException`: `"Byte count must be non-null and >= 0 (got: -500)"` | **PASS** |

| **T08** | Device CRUD Operations (Add, Retrieve, Remove from Map/Set) | Register 3 valid devices (`DEV-RTR-01`, `DEV-SW-01`, `DEV-SRV-01`); query `getDevice("DEV-SW-01")`; remove `DEV-SRV-01` | Devices added to `HashMap`/`HashSet`; `getDevice()` returns device by ID; `removeDevice()` removes device from both structures and decrements count to 2. | 3 devices successfully registered; `getDevice("DEV-SW-01")` returned Access-Switch-Floor1; `removeDevice("DEV-SRV-01")` decremented count to 2 and released IP `192.168.1.10`. | **PASS** |
| **T09** | Duplicate Prevention (Attempting to add a device with an existing ID/IP) | Attempt to add device `DEV-FW-01` with existing management IP `192.168.1.1` (already used by `DEV-RTR-01`) or existing ID `DEV-RTR-01` | Service checks `HashMap` keys and `HashSet` IPs; aborts insertion and throws `DuplicateRecordException`. | Insertion aborted; caught `DuplicateRecordException`: `"Device with Management IP '192.168.1.1' already exists."` | **PASS** |
| **T10** | Flow Sorting and Filtering (`getFlowsByProtocol` and `getTopHighVolumeFlows`) | 5 flows registered (2 TCP, 2 UDP, 1 ICMP); filter by `NetworkProtocol.TCP`; sort top 3 by volume with `getTopHighVolumeFlows(3)` | `getFlowsByProtocol()` returns 2 TCP flows; `getTopHighVolumeFlows(3)` returns 3 flows strictly ordered descending by `byteCount`. | Returned exactly 2 TCP flows (`FLW-TCP-01`, `FLW-TCP-02`); Top 3 ordered descending: #1 FLW-UDP-02 (35,000,000 B), #2 FLW-TCP-02 (12,000,000 B), #3 FLW-TCP-01 (1,500,000 B). | **PASS** |

---

## 2. Console Test Harness Output

### Milestone 1 Execution (`NetFlowApp`)
=======

---

## 2. Test Execution Output from `NetFlowApp`

>>>>>>> f9de4266f843a7ba065c7f526b0b0c110aaa021a
```text
===============================================================================
       NETFLOW: NETWORK TRAFFIC MONITORING & MANAGEMENT SYSTEM
                  Milestone 1 Test Harness & Evidence
===============================================================================

--- TEST 1: Valid Device Creation (Test T01) ---
[SUCCESS] NetworkDevice instantiated successfully!
Device ID       : DEV-RTR-01
Device Name     : Core-Gateway-East
Device Type     : ROUTER
Management IP   : 192.168.1.1
Location        : Building-A Rack-04
Status          : ONLINE

[Monitoring Summary via Monitorable Interface]:
[ONLINE] Device DEV-RTR-01 ('Core-Gateway-East', ROUTER) - IP: 192.168.1.1 | Location: Building-A Rack-04

--- TEST 2: Valid Flow Creation ---
[SUCCESS] TrafficFlow instantiated successfully!
Flow ID         : FLW-9001
Source Endpoint : 192.168.1.50:52134
Dest Endpoint   : 10.0.0.1:443
Protocol        : TCP
Packet Count    : 15,420
Byte Count      : 18,456,000 bytes
Duration        : 12500 ms

[Formatted Flow Record]:
[FLW-9001] 192.168.1.50:52134 -> 10.0.0.1:443 [TCP] | Packets: 15420 | Bytes: 18456000 | Duration: 12500ms

--- TEST 3: Invalid Device Validation (Tests T04 & T05) ---
[Subtest 3a: Attempting to create NetworkDevice with malformed IP (999.300.1.1)...]
  [PASS] Caught expected InvalidDeviceException:
         --> Invalid IPv4 address format for management IP: 999.300.1.1

[Subtest 3b: Attempting to create NetworkDevice with blank name ('   ')...]
  [PASS] Caught expected InvalidDeviceException:
         --> Device name cannot be null or empty

--- TEST 4: Invalid Flow Validation (Tests T06 & T07) ---
[Subtest 4a: Attempting to create TrafficFlow with out-of-range port (70000)...]
  [PASS] Caught expected InvalidFlowException:
         --> Source port must be between 0 and 65535 (got: 70000)

[Subtest 4b: Attempting to create TrafficFlow with negative byte count (-500)...]
  [PASS] Caught expected InvalidFlowException:
         --> Byte count must be non-null and >= 0 (got: -500)

--- TEST 5: Alert Model & Protocol Port Wrapper (ICMP null port) ---
[SUCCESS] ICMP TrafficFlow with null ports instantiated:
  [FLW-ICMP-01] 192.168.1.100 -> 8.8.8.8 [ICMP] | Packets: 0 | Bytes: 0 | Duration: 0ms

[SUCCESS] NetworkAlert created:
  [CRITICAL] Alert ALT-001: High ICMP Echo volume detected (possible ping flood) | Flow: FLW-ICMP-01 | Ack: false
After acknowledgment:
  [CRITICAL] Alert ALT-001: High ICMP Echo volume detected (possible ping flood) | Flow: FLW-ICMP-01 | Ack: true

===============================================================================
            ALL MILESTONE 1 VERIFICATION TESTS PASSED SUCCESSFULLY
===============================================================================
```


### Milestone 2 Execution (`Milestone2App`)
```text
===============================================================================
  NETFLOW: MILESTONE 2 - COLLECTIONS & RECORD MANAGEMENT TEST HARNESS
===============================================================================
--- MILESTONE 2: DEVICE SERVICE TEST (T08 & T09) ---
-------------------------------------------------------------------------------
[Step 1.1] Registering 3 initial valid network devices...
  [SUCCESS] 3 devices added. Current Device Count: 3
    * [ONLINE] Device DEV-SRV-01 ('Auth-LDAP-Server', SERVER) - IP: 192.168.1.10 | Location: Datacenter Row-03
    * [ONLINE] Device DEV-RTR-01 ('Core-Router-East', ROUTER) - IP: 192.168.1.1 | Location: Building-A Rack-01
    * [ONLINE] Device DEV-SW-01 ('Access-Switch-Floor1', SWITCH) - IP: 192.168.1.2 | Location: Building-A Rack-02

[Step 1.2] Attempting to add 4th device with DUPLICATE management IP (192.168.1.1)...
  [PASS] Successfully caught DuplicateRecordException:
         --> Device with Management IP '192.168.1.1' already exists.

[Step 1.3] Retrieving device by ID 'DEV-SW-01' via HashMap...
  [SUCCESS] Found device: Access-Switch-Floor1
  Summary: [ONLINE] Device DEV-SW-01 ('Access-Switch-Floor1', SWITCH) - IP: 192.168.1.2 | Location: Building-A Rack-02

[Step 1.4] Removing device 'DEV-SRV-01'...
  [SUCCESS] Removed device: Auth-LDAP-Server (IP: 192.168.1.10)
  Remaining Device Count: 2
  Verifying IP '192.168.1.10' is released: YES (Released)

--- MILESTONE 2: TRAFFIC FLOW SERVICE TEST (T10) ---
-------------------------------------------------------------------------------
[Step 2.1] Registering 5 telemetry flow records (2 TCP, 2 UDP, 1 ICMP)...
  [SUCCESS] 5 flows registered. Total Flow Count: 5

[Step 2.2] Filtering flows by protocol (TCP) using Stream API...
  Found 2 TCP flow(s):
    - [FLW-TCP-01] 192.168.1.50:52134 -> 10.0.0.1:80 | TCP | 1,500,000 bytes
    - [FLW-TCP-02] 192.168.1.55:54321 -> 10.0.0.2:443 | TCP | 12,000,000 bytes

[Step 2.3] Slicing TOP 3 highest-volume flows via Comparator (descending byte count)...
    #1 [FLW-UDP-02] UDP   |   35,000,000 bytes ( 33.38 MB) | 192.168.1.70 -> 10.0.0.5
    #2 [FLW-TCP-02] TCP   |   12,000,000 bytes ( 11.44 MB) | 192.168.1.55 -> 10.0.0.2
    #3 [FLW-TCP-01] TCP   |    1,500,000 bytes (  1.43 MB) | 192.168.1.50 -> 10.0.0.1
===============================================================================
  MILESTONE 2 VERIFICATION TESTS COMPLETED SUCCESSFULLY
===============================================================================
```

---

## 3. Automated Test Suite (JUnit 5)

| Test Suite Class | Tests Run | Passed | Failed | Target Features |
| :--- | :--- | :--- | :--- | :--- |
| `NetworkDeviceTest` | 5 | 5 | 0 | Device creation (T01), IP format (T04), blank name (T05), ID checks |
| `TrafficFlowTest` | 5 | 5 | 0 | Flow creation (T02), port bounds (T06), negative bytes (T07), null ports |
| `DeviceServiceTest` | 7 | 7 | 0 | Device CRUD (T08), Duplicate IP & ID prevention (T09), IP release |
| `TrafficFlowServiceTest` | 5 | 5 | 0 | Flow storage, duplicate IDs, protocol filtering (T10), Comparator sorting (T10) |
| **Total** | **22** | **22** | **0** | **100% Pass Rate** |
=======
---

## 3. Automated JUnit 5 Verification Suite

The project includes automated JUnit 5 test classes under `netflow.test`:
- `NetworkDeviceTest`: Validates successful initialization (T01), IPv4 regex filtering (T04), empty/whitespace strings (T05), null parameter handling, and `Monitorable` summary output.
- `TrafficFlowTest`: Validates telemetry fields (T02), port bounds checking `[0, 65535]` (T06), negative byte counts (T07), negative packet counts, and nullable port wrapper handling for ICMP protocols.
>>>>>>> f9de4266f843a7ba065c7f526b0b0c110aaa021a
