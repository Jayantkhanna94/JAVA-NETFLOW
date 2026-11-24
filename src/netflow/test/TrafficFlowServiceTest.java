package netflow.test;

import netflow.exception.DuplicateRecordException;
import netflow.exception.InvalidFlowException;
import netflow.exception.RecordNotFoundException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;
import netflow.service.TrafficFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link TrafficFlowService} operations (ArrayList storage,
 * duplicate prevention, Stream filtering, and Comparator sorting).
 */
class TrafficFlowServiceTest {

    private TrafficFlowService flowService;

    @BeforeEach
    void setUp() {
        flowService = new TrafficFlowService();
    }

    @Test
    @DisplayName("Adding and retrieving flow records")
    void testAddAndGetFlow() throws InvalidFlowException {
        TrafficFlow flow = new TrafficFlow(
                "FLW-001",
                "192.168.1.10",
                "10.0.0.1",
                NetworkProtocol.TCP,
                5000,
                80
        );

        flowService.addFlow(flow);
        assertEquals(1, flowService.getFlowCount());

        TrafficFlow retrieved = flowService.getFlow("FLW-001");
        assertNotNull(retrieved);
        assertEquals("FLW-001", retrieved.getFlowId());
        assertEquals("192.168.1.10", retrieved.getSourceIp());
    }

    @Test
    @DisplayName("Adding flow with duplicate flow ID throws DuplicateRecordException")
    void testDuplicateFlowIdThrowsException() throws InvalidFlowException {
        TrafficFlow flow1 = new TrafficFlow("FLW-001", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 5000, 80);
        TrafficFlow duplicate = new TrafficFlow("FLW-001", "10.0.0.3", "10.0.0.4", NetworkProtocol.UDP, 5300, 53);

        flowService.addFlow(flow1);

        DuplicateRecordException ex = assertThrows(DuplicateRecordException.class, () ->
                flowService.addFlow(duplicate)
        );
        assertTrue(ex.getMessage().contains("Traffic flow with ID 'FLW-001' already exists"));
    }

    @Test
    @DisplayName("Querying missing flow ID throws RecordNotFoundException")
    void testMissingFlowThrowsException() {
        assertThrows(RecordNotFoundException.class, () -> flowService.getFlow("FLW-NONEXISTENT"));
    }

    @Test
    @DisplayName("T10.1: Filtering flows by protocol using Stream API")
    void testGetFlowsByProtocol() throws InvalidFlowException {
        TrafficFlow tcp1 = new TrafficFlow("F-TCP-1", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 1000, 80);
        TrafficFlow tcp2 = new TrafficFlow("F-TCP-2", "10.0.0.1", "10.0.0.3", NetworkProtocol.TCP, 1001, 443);
        TrafficFlow udp1 = new TrafficFlow("F-UDP-1", "10.0.0.1", "10.0.0.4", NetworkProtocol.UDP, 1002, 53);
        TrafficFlow icmp1 = new TrafficFlow("F-ICMP-1", "10.0.0.1", "10.0.0.5", NetworkProtocol.ICMP, null, null);

        flowService.addFlow(tcp1);
        flowService.addFlow(tcp2);
        flowService.addFlow(udp1);
        flowService.addFlow(icmp1);

        List<TrafficFlow> tcpFlows = flowService.getFlowsByProtocol(NetworkProtocol.TCP);
        assertEquals(2, tcpFlows.size());
        assertTrue(tcpFlows.contains(tcp1));
        assertTrue(tcpFlows.contains(tcp2));

        List<TrafficFlow> udpFlows = flowService.getFlowsByProtocol(NetworkProtocol.UDP);
        assertEquals(1, udpFlows.size());
        assertTrue(udpFlows.contains(udp1));

        List<TrafficFlow> icmpFlows = flowService.getFlowsByProtocol(NetworkProtocol.ICMP);
        assertEquals(1, icmpFlows.size());
        assertTrue(icmpFlows.contains(icmp1));

        List<TrafficFlow> otherFlows = flowService.getFlowsByProtocol(NetworkProtocol.OTHER);
        assertTrue(otherFlows.isEmpty());
    }

    @Test
    @DisplayName("T10.2: Top high-volume flows sorted in descending order by byteCount")
    void testGetTopHighVolumeFlowsDescendingOrder() throws InvalidFlowException {
        TrafficFlow flowLow = new TrafficFlow(
                "FLW-LOW", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 1000, 80,
                100L, 50000L, 1000L, System.currentTimeMillis()
        );
        TrafficFlow flowMid = new TrafficFlow(
                "FLW-MID", "10.0.0.1", "10.0.0.3", NetworkProtocol.TCP, 1001, 443,
                500L, 2000000L, 2000L, System.currentTimeMillis()
        );
        TrafficFlow flowHigh = new TrafficFlow(
                "FLW-HIGH", "10.0.0.1", "10.0.0.4", NetworkProtocol.UDP, 1002, 5004,
                25000L, 35000000L, 15000L, System.currentTimeMillis()
        );

        // Add in mixed order
        flowService.addFlow(flowMid);
        flowService.addFlow(flowLow);
        flowService.addFlow(flowHigh);

        List<TrafficFlow> top2 = flowService.getTopHighVolumeFlows(2);
        assertEquals(2, top2.size());
        assertEquals("FLW-HIGH", top2.get(0).getFlowId()); // 35,000,000 bytes
        assertEquals("FLW-MID", top2.get(1).getFlowId());  //  2,000,000 bytes

        List<TrafficFlow> topAll = flowService.getTopHighVolumeFlows(5);
        assertEquals(3, topAll.size());
        assertEquals("FLW-HIGH", topAll.get(0).getFlowId());
        assertEquals("FLW-MID", topAll.get(1).getFlowId());
        assertEquals("FLW-LOW", topAll.get(2).getFlowId());

        // Edge case: limit <= 0
        assertTrue(flowService.getTopHighVolumeFlows(0).isEmpty());
        assertTrue(flowService.getTopHighVolumeFlows(-1).isEmpty());
    }
}
