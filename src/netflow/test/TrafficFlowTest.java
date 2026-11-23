package netflow.test;

import netflow.exception.InvalidFlowException;
import netflow.model.NetworkProtocol;
import netflow.model.TrafficFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying {@link TrafficFlow} creation, port validation,
 * non-negative count constraints, and exception handling.
 */
class TrafficFlowTest {

    @Test
    @DisplayName("T02: Successful traffic flow creation with valid parameters")
    void testValidTrafficFlowCreation() throws InvalidFlowException {
        TrafficFlow flow = new TrafficFlow(
                "FLW-9001",
                "192.168.1.50",
                "10.0.0.1",
                NetworkProtocol.TCP,
                52134,
                443,
                15420L,
                18456000L,
                12500L,
                System.currentTimeMillis()
        );

        assertNotNull(flow);
        assertEquals("FLW-9001", flow.getFlowId());
        assertEquals("192.168.1.50", flow.getSourceIp());
        assertEquals("10.0.0.1", flow.getDestIp());
        assertEquals(NetworkProtocol.TCP, flow.getProtocol());
        assertEquals(Integer.valueOf(52134), flow.getSourcePort());
        assertEquals(Integer.valueOf(443), flow.getDestPort());
        assertEquals(Long.valueOf(15420L), flow.getPacketCount());
        assertEquals(Long.valueOf(18456000L), flow.getByteCount());
        assertEquals(Long.valueOf(12500L), flow.getDurationMs());

        String record = flow.toString();
        assertNotNull(record);
        assertTrue(record.contains("FLW-9001"));
        assertTrue(record.contains("192.168.1.50:52134"));
        assertTrue(record.contains("10.0.0.1:443"));
    }

    @Test
    @DisplayName("T06: Creation with out-of-range port throws InvalidFlowException")
    void testOutOfRangePortThrowsException() {
        // Port > 65535 on source port
        InvalidFlowException ex1 = assertThrows(InvalidFlowException.class, () ->
                new TrafficFlow("FLW-01", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, 70000, 80)
        );
        assertTrue(ex1.getMessage().contains("Source port must be between 0 and 65535"));

        // Negative source port < 0
        InvalidFlowException ex2 = assertThrows(InvalidFlowException.class, () ->
                new TrafficFlow("FLW-02", "10.0.0.1", "10.0.0.2", NetworkProtocol.TCP, -1, 80)
        );
        assertTrue(ex2.getMessage().contains("Source port must be between 0 and 65535"));

        // Port > 65535 on destination port
        InvalidFlowException ex3 = assertThrows(InvalidFlowException.class, () ->
                new TrafficFlow("FLW-03", "10.0.0.1", "10.0.0.2", NetworkProtocol.UDP, 53, 99999)
        );
        assertTrue(ex3.getMessage().contains("Destination port must be between 0 and 65535"));
    }

    @Test
    @DisplayName("T07: Creation with negative byte count throws InvalidFlowException")
    void testNegativeByteCountThrowsException() {
        InvalidFlowException ex = assertThrows(InvalidFlowException.class, () ->
                new TrafficFlow(
                        "FLW-04",
                        "192.168.1.1",
                        "10.0.0.1",
                        NetworkProtocol.TCP,
                        5000,
                        80,
                        100L,
                        -500L, // Negative byte count
                        1000L,
                        System.currentTimeMillis()
                )
        );
        assertTrue(ex.getMessage().contains("Byte count must be non-null and >= 0"));
    }

    @Test
    @DisplayName("Creation with negative packet count throws InvalidFlowException")
    void testNegativePacketCountThrowsException() {
        InvalidFlowException ex = assertThrows(InvalidFlowException.class, () ->
                new TrafficFlow(
                        "FLW-05",
                        "192.168.1.1",
                        "10.0.0.1",
                        NetworkProtocol.TCP,
                        5000,
                        80,
                        -10L, // Negative packet count
                        5000L,
                        1000L,
                        System.currentTimeMillis()
                )
        );
        assertTrue(ex.getMessage().contains("Packet count must be non-null and >= 0"));
    }

    @Test
    @DisplayName("Creation with null ports is valid for ICMP protocol")
    void testIcmpWithNullPortsSucceeds() throws InvalidFlowException {
        TrafficFlow icmpFlow = new TrafficFlow(
                "FLW-ICMP",
                "192.168.1.1",
                "8.8.8.8",
                NetworkProtocol.ICMP,
                null,
                null
        );

        assertNotNull(icmpFlow);
        assertNull(icmpFlow.getSourcePort());
        assertNull(icmpFlow.getDestPort());
        assertEquals(NetworkProtocol.ICMP, icmpFlow.getProtocol());
    }
}
