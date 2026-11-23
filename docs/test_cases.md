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

---

## 2. Test Execution Output from `NetFlowApp`

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

---

## 3. Automated JUnit 5 Verification Suite

The project includes automated JUnit 5 test classes under `netflow.test`:
- `NetworkDeviceTest`: Validates successful initialization (T01), IPv4 regex filtering (T04), empty/whitespace strings (T05), null parameter handling, and `Monitorable` summary output.
- `TrafficFlowTest`: Validates telemetry fields (T02), port bounds checking `[0, 65535]` (T06), negative byte counts (T07), negative packet counts, and nullable port wrapper handling for ICMP protocols.
