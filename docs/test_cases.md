# NetFlow System - Test Evidence & Traceability Matrix

## 1. Test Cases Table (Milestones 1, 2, 3 & 4)

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
| **T11** | Concurrent Threads & Synchronization | Launch multiple threads (10-30 threads) simultaneously updating shared totals via `addFlowStats()` | Final totals match exact processed records without lost updates or race conditions. | 10 worker threads completed; `sharedStats` totals: 10 flows, 10,000 bytes (100% match with expected mathematical values, 0 lost updates). | **PASS** |
| **T12** | Thread Interruption | Interrupt a processing thread (`FlowWorker-Interruptible`) via `interrupt()` during execution | Interruption is caught and handled safely; thread terminates cleanly without crashing or corrupting shared statistics. | Interrupted thread logged warning, cleanly exited without throwing uncaught exceptions, and left shared statistics uncontaminated. | **PASS** |
| **T13** | Produce and consume queued flow tasks | `FlowQueue(capacity=3)` with `FlowProducer` and `FlowConsumer` running on concurrent threads transferring 6 flows | Accepted tasks are processed without dropping. Queue never exceeds capacity; consumer waits when empty, producer waits when full; final queue size: 0. | All 6 flow tasks successfully produced and consumed without dropping or race conditions. Final queue size: 0. | **PASS** |
| **T14** | Execute a Callable task and retrieve its Future | `Executors.newFixedThreadPool(2)`, `CallableFlowTask` with 15MB flow, `future.get()`, `shutdown()`, and `awaitTermination()` | Result matches processed data. Callable computes flow bytes in worker thread; `future.get()` blocks and returns exact processed byte count. | `Future.get()` returned 15,728,640 bytes (exact match with submitted flow); executor shut down cleanly. | **PASS** |

---

## 2. Console Test Harness Output

### Milestone 4 Execution (`Milestone4App`)
```text
===============================================================================
  NETFLOW: MILESTONE 4 - PRODUCER-CONSUMER & EXECUTOR FRAMEWORK HARNESS
===============================================================================
--- MILESTONE 4: PRODUCER-CONSUMER TEST (Test T13) ---
-------------------------------------------------------------------------------
[Step 1.1] Initializing FlowQueue with bounded capacity of 3...
[Step 1.2] Starting producer and consumer threads (6 flows to transfer)...

[ConsumerThread-1] Queue is EMPTY. Consumer waiting...
[ProducerThread-1] PRODUCED: FLOW-PC-001 | Queue Size: 1/3
[ConsumerThread-1] CONSUMED: FLOW-PC-001 | Queue Size: 0/3
[ProducerThread-1] PRODUCED: FLOW-PC-002 | Queue Size: 1/3
[ConsumerThread-1] Processed flow telemetry: FLOW-PC-001 [UDP 192.168.1.101:5001 -> 10.0.0.201:80]
[ConsumerThread-1] CONSUMED: FLOW-PC-002 | Queue Size: 0/3
[ProducerThread-1] PRODUCED: FLOW-PC-003 | Queue Size: 1/3
[ProducerThread-1] PRODUCED: FLOW-PC-004 | Queue Size: 2/3
[ConsumerThread-1] Processed flow telemetry: FLOW-PC-002 [TCP 192.168.1.102:5002 -> 10.0.0.202:80]
[ConsumerThread-1] CONSUMED: FLOW-PC-003 | Queue Size: 1/3
[ProducerThread-1] PRODUCED: FLOW-PC-005 | Queue Size: 2/3
[ProducerThread-1] PRODUCED: FLOW-PC-006 | Queue Size: 3/3
[ProducerThread-1] Producer finished producing 6 flows.
[ConsumerThread-1] Processed flow telemetry: FLOW-PC-003 [UDP 192.168.1.103:5003 -> 10.0.0.203:80]
[ConsumerThread-1] CONSUMED: FLOW-PC-004 | Queue Size: 2/3
[ConsumerThread-1] Processed flow telemetry: FLOW-PC-004 [TCP 192.168.1.104:5004 -> 10.0.0.204:80]
[ConsumerThread-1] CONSUMED: FLOW-PC-005 | Queue Size: 1/3
[ConsumerThread-1] Processed flow telemetry: FLOW-PC-005 [UDP 192.168.1.105:5005 -> 10.0.0.205:80]
[ConsumerThread-1] CONSUMED: FLOW-PC-006 | Queue Size: 0/3
[ConsumerThread-1] Processed flow telemetry: FLOW-PC-006 [TCP 192.168.1.106:5006 -> 10.0.0.206:80]
[ConsumerThread-1] Consumer finished consuming 6 flows.

[SUCCESS] Producer and consumer threads completed successfully.
  Remaining Queue Size: 0 (Expected: 0)

--- MILESTONE 4: EXECUTOR SERVICE TEST (Test T14) ---
-------------------------------------------------------------------------------
[Step 2.1] Creating ExecutorService via Executors.newFixedThreadPool(2)...
[Step 2.2] Instantiating valid TrafficFlow record for asynchronous processing...
  Flow Details: [FLW-EXEC-501] 192.168.1.100:54321 -> 10.0.0.50:443 [TCP] | Packets: 12500 | Bytes: 15728640 | Duration: 5000ms

[Step 2.3] Submitting CallableFlowTask to ExecutorService...
  Task submitted. Waiting on Future.get() to retrieve processed bytes...
[pool-1-thread-1] Callable processed: FLW-EXEC-501 | Bytes: 15,728,640

[SUCCESS] Future Result (Processed Bytes): 15,728,640 bytes
  Verification: MATCH (Callable calculated exact flow volume)

[Step 2.4] Initiating graceful shutdown of ExecutorService...
[SUCCESS] ExecutorService shut down cleanly. All thread resources released.
===============================================================================
  MILESTONE 4 CONCURRENCY TESTS COMPLETED SUCCESSFULLY
===============================================================================
```

---

## 3. Automated Test Suite Summary (JUnit 5)

| Test Suite Class | Target Milestone | Target Features |
| :--- | :--- | :--- |
| `NetworkDeviceTest` | M1 | Device creation (T01), IP format (T04), blank name (T05), ID checks |
| `TrafficFlowTest` | M1 | Flow creation (T02), port bounds (T06), negative bytes (T07), null ports |
| `DeviceServiceTest` | M2 | Device CRUD (T08), Duplicate IP & ID prevention (T09), IP release |
| `TrafficFlowServiceTest` | M2 | Flow storage, duplicate IDs, protocol filtering (T10), Comparator sorting (T10) |
| `MonitoringStatsTest` | M3 | Concurrent threads & synchronized critical section (T11), reset |
| `FlowProcessingTaskTest` | M3 | Thread interruption (T12), normal execution, null validations |
| `FlowQueueTest` | M4 | Bounded queue, wait/notify, producer/consumer coordination (T13) |
| `CallableFlowTaskTest` | M4 | ExecutorService task submission, Future.get() byte verification (T14) |
| **All Test Suites** | **M1–M4** | **100% Pass Rate (33/33 Tests Passed)** |
