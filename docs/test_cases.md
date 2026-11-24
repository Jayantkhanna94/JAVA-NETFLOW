# NetFlow System - Test Evidence & Traceability Matrix

## 1. Test Cases Table (Milestones 1, 2, 3, 4 & 5)

| Test ID | Milestone | Feature Tested | Input Data | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **T01** | M1 | Valid Device Creation & `Monitorable` Summary | `id="DEV-RTR-01"`, `name="Core-Gateway-East"`, `deviceType=ROUTER`, `managementIp="192.168.1.1"`, `location="Building-A Rack-04"`, `status=ONLINE` | Object instantiated with attributes matching input; `getMonitoringSummary()` returns formatted string containing status, ID, name, type, and IP. | `NetworkDevice` instantiated successfully. `getMonitoringSummary()` outputs: `[ONLINE] Device DEV-RTR-01 ('Core-Gateway-East', ROUTER) - IP: 192.168.1.1 \| Location: Building-A Rack-04` | **PASS** |
| **T02** | M1 | Valid Traffic Flow Creation & Port Range | `flowId="FLW-9001"`, `sourceIp="192.168.1.50"`, `destIp="10.0.0.1"`, `protocol=TCP`, `sourcePort=52134`, `destPort=443`, `packetCount=15420`, `byteCount=18456000`, `durationMs=12500` | Flow record successfully created; all telemetry fields and wrappers correctly populated; `toString()` returns formatted telemetry string. | `TrafficFlow` instantiated successfully. Output: `[FLW-9001] 192.168.1.50:52134 -> 10.0.0.1:443 [TCP] \| Packets: 15420 \| Bytes: 18456000 \| Duration: 12500ms` | **PASS** |
| **T04** | M1 | Invalid Device: Malformed Management IP Address | `id="DEV-BAD-01"`, `name="Edge-Switch"`, `deviceType=SWITCH`, `managementIp="999.300.1.1"` | Regex validation fails; constructor aborts instantiation and throws `InvalidDeviceException`. | Caught `InvalidDeviceException`: `"Invalid IPv4 address format for management IP: 999.300.1.1"` | **PASS** |
| **T05** | M1 | Invalid Device: Blank / Empty Device Name | `id="DEV-BAD-02"`, `name="   "`, `deviceType=SERVER`, `managementIp="10.0.0.5"` | Blank string check fails; constructor aborts instantiation and throws `InvalidDeviceException`. | Caught `InvalidDeviceException`: `"Device name cannot be null or empty"` | **PASS** |
| **T06** | M1 | Invalid Flow: Port Out of Valid Range | `flowId="FLW-BAD-01"`, `sourceIp="192.168.1.1"`, `destIp="10.0.0.1"`, `protocol=TCP`, `sourcePort=70000`, `destPort=80` | Port validation check (`0 <= port <= 65535`) fails; constructor throws `InvalidFlowException`. | Caught `InvalidFlowException`: `"Source port must be between 0 and 65535 (got: 70000)"` | **PASS** |
| **T07** | M1 | Invalid Flow: Negative Byte Count | `flowId="FLW-BAD-02"`, `sourceIp="192.168.1.1"`, `destIp="10.0.0.1"`, `protocol=UDP`, `sourcePort=5300`, `destPort=53`, `packetCount=100`, `byteCount=-500` | Non-negative telemetry validation fails; constructor throws `InvalidFlowException`. | Caught `InvalidFlowException`: `"Byte count must be non-null and >= 0 (got: -500)"` | **PASS** |
| **T08** | M2 | Device CRUD Operations (Add, Retrieve, Remove from Map/Set) | Register 3 valid devices (`DEV-RTR-01`, `DEV-SW-01`, `DEV-SRV-01`); query `getDevice("DEV-SW-01")`; remove `DEV-SRV-01` | Devices added to `HashMap`/`HashSet`; `getDevice()` returns device by ID; `removeDevice()` removes device from both structures and decrements count to 2. | 3 devices successfully registered; `getDevice("DEV-SW-01")` returned Access-Switch-Floor1; `removeDevice("DEV-SRV-01")` decremented count to 2 and released IP `192.168.1.10`. | **PASS** |
| **T09** | M2 | Duplicate Prevention (Attempting to add a device with an existing ID/IP) | Attempt to add device `DEV-FW-01` with existing management IP `192.168.1.1` (already used by `DEV-RTR-01`) or existing ID `DEV-RTR-01` | Service checks `HashMap` keys and `HashSet` IPs; aborts insertion and throws `DuplicateRecordException`. | Insertion aborted; caught `DuplicateRecordException`: `"Device with Management IP '192.168.1.1' already exists."` | **PASS** |
| **T10** | M2 | Flow Sorting and Filtering (`getFlowsByProtocol` and `getTopHighVolumeFlows`) | 5 flows registered (2 TCP, 2 UDP, 1 ICMP); filter by `NetworkProtocol.TCP`; sort top 3 by volume with `getTopHighVolumeFlows(3)` | `getFlowsByProtocol()` returns 2 TCP flows; `getTopHighVolumeFlows(3)` returns 3 flows strictly ordered descending by `byteCount`. | Returned exactly 2 TCP flows (`FLW-TCP-01`, `FLW-TCP-02`); Top 3 ordered descending: #1 FLW-UDP-02 (35,000,000 B), #2 FLW-TCP-02 (12,000,000 B), #3 FLW-TCP-01 (1,500,000 B). | **PASS** |
| **T11** | M3 | Concurrent Threads & Synchronization | Launch multiple threads (10-30 threads) simultaneously updating shared totals via `addFlowStats()` | Final totals match exact processed records without lost updates or race conditions. | 10 worker threads completed; `sharedStats` totals: 10 flows, 10,000 bytes (100% match with expected mathematical values, 0 lost updates). | **PASS** |
| **T12** | M3 | Thread Interruption | Interrupt a processing thread (`FlowWorker-Interruptible`) via `interrupt()` during execution | Interruption is caught and handled safely; thread terminates cleanly without crashing or corrupting shared statistics. | Interrupted thread logged warning, cleanly exited without throwing uncaught exceptions, and left shared statistics uncontaminated. | **PASS** |
| **T13** | M4 | Produce and consume queued flow tasks | `FlowQueue(capacity=3)` with `FlowProducer` and `FlowConsumer` running on concurrent threads transferring 6 flows | Accepted tasks are processed without dropping. Queue never exceeds capacity; consumer waits when empty, producer waits when full; final queue size: 0. | All 6 flow tasks successfully produced and consumed without dropping or race conditions. Final queue size: 0. | **PASS** |
| **T14** | M4 | Execute a Callable task and retrieve its Future | `Executors.newFixedThreadPool(2)`, `CallableFlowTask` with 15MB flow, `future.get()`, `shutdown()`, and `awaitTermination()` | Result matches processed data. Callable computes flow bytes in worker thread; `future.get()` blocks and returns exact processed byte count. | `Future.get()` returned 15,728,640 bytes (exact match with submitted flow); executor shut down cleanly. | **PASS** |
| **T15** | M5 | Database Persistence (CRUD) & SQL Parameterization | Insert `DEV-DB-001` via `DeviceDAO.addDevice()`, query via `getDevice()`, update status to `DEGRADED`, query all | `PreparedStatement` correctly executes queries; ResultSet mapped to `NetworkDevice`; update reflects in database state. | Record inserted, retrieved (`ONLINE`), status updated to `DEGRADED`, and verified via re-fetch from MySQL. | **PASS** |
| **T17** | M5 | Telemetry Flow Persistence & Nullable Ports | Insert `TrafficFlow` with nullable ports into `traffic_flows` table via `TrafficFlowDAO`, query all flows | PreparedStatement sets port values or `Types.INTEGER` null; `getAllFlows()` maps rows using `rs.wasNull()`. | Flow record persisted and retrieved from MySQL with full telemetry accuracy. | **PASS** |
| **T18** | M5 | Transaction Management & Atomic Rollback | Call `addDeviceWithTransactionDemo(device2, device3, forceFail=true)` with disabled auto-commit | Simulated failure causes `rollback()`; neither device2 nor device3 is committed; `getDevice("DEV-TX-02")` throws `RecordNotFoundException`. | Caught simulated failure; rollback confirmed; `DEV-TX-02` lookup threw `RecordNotFoundException` (0 partial records saved). | **PASS** |

---

## 2. Console Test Harness Output

### Milestone 5 Execution (`Milestone5App`)
```text
===============================================================================
  NETFLOW: MILESTONE 5 - JDBC & RELATIONAL PERSISTENCE TEST HARNESS
===============================================================================
--- MILESTONE 5: DATABASE CRUD & PERSISTENCE TEST (T15 & T17) ---
-------------------------------------------------------------------------------
[Step 1.1] Creating and inserting NetworkDevice into MySQL...
  [SUCCESS] Device inserted into 'network_devices' table: DEV-DB-001

[Step 1.2] Querying device 'DEV-DB-001' from database via getDevice()...
  [SUCCESS] Record fetched successfully from MySQL:
  [ONLINE] Device DEV-DB-001 ('Core-Edge-Router', ROUTER) - IP: 192.168.10.1 | Location: Datacenter-Rack-E1

[Step 1.3] Updating device status to DEGRADED via updateDeviceStatus()...
  Update execution status: SUCCESS
  Re-fetched device state from MySQL:
  [DEGRADED] Device DEV-DB-001 ('Core-Edge-Router', ROUTER) - IP: 192.168.10.1 | Location: Datacenter-Rack-E1

[Step 1.4] Inserting TrafficFlow telemetry record into 'traffic_flows' table...
  [SUCCESS] Flow inserted: FLW-DB-91518 (18456000 bytes)

[Step 1.5] Calling getAllFlows() to query persisted flows...
  [SUCCESS] Total flows retrieved from MySQL: 1 flow(s)
  Latest Flow in DB: [FLW-DB-91518] 192.168.10.100:52134 -> 10.0.0.1:443 [TCP] | Packets: 15420 | Bytes: 18456000 | Duration: 12500ms

--- MILESTONE 5: TRANSACTION ROLLBACK TEST (Test T18) ---
-------------------------------------------------------------------------------
[Step 2.1] Preparing 2 new devices for transactional insertion:
  Device 2: DEV-TX-02 (172.16.1.1)
  Device 3: DEV-TX-03 (172.16.1.2)

[Step 2.2] Invoking addDeviceWithTransactionDemo(device2, device3, forceFail=true)...
  Expected behavior: device2 is staged, simulated failure fires, whole transaction rolls back.
  [PASS] Caught simulated transaction failure:
         --> Simulated transaction failure triggered (forceFail=true). Rolling back!
  [PASS] Connection rollback() was executed in catch block.

[Step 2.3] Verifying rollback by querying device2 ('DEV-TX-02') from database...
  [PASS] Caught expected RecordNotFoundException:
         --> Device with ID 'DEV-TX-02' not found in database.
  [VERIFIED] Neither device2 nor device3 were saved to MySQL.
  Atomic consistency guaranteed: 0 partial records committed.
===============================================================================
  MILESTONE 5 DATABASE & TRANSACTION TESTS COMPLETED SUCCESSFULLY
===============================================================================
```
