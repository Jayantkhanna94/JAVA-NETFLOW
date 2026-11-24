-- =============================================================================
-- NetFlow System: Network Traffic Monitoring & Management System
-- Milestone 5: Relational Database Schema (MySQL 8.x)
-- =============================================================================

CREATE DATABASE IF NOT EXISTS netflow_db;
USE netflow_db;

-- -----------------------------------------------------------------------------
-- 1. Table: network_devices
-- Stores metadata for monitored network hardware/virtual entities.
-- Enforces uniqueness on management_ip across all devices.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS network_devices (
    device_id VARCHAR(64) NOT NULL,
    device_name VARCHAR(128) NOT NULL,
    device_type VARCHAR(32) NOT NULL,
    management_ip VARCHAR(45) NOT NULL,
    location VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_network_devices PRIMARY KEY (device_id),
    CONSTRAINT uq_device_management_ip UNIQUE (management_ip),
    INDEX idx_devices_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 2. Table: traffic_flows
-- Stores unidirectional telemetry flow records.
-- Constrains packet_count, byte_count, and duration_ms to non-negative values.
-- Allows NULL ports for protocols without transport ports (e.g., ICMP).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS traffic_flows (
    flow_id VARCHAR(64) NOT NULL,
    source_ip VARCHAR(45) NOT NULL,
    dest_ip VARCHAR(45) NOT NULL,
    protocol VARCHAR(16) NOT NULL,
    source_port INT,
    dest_port INT,
    packet_count BIGINT NOT NULL DEFAULT 0,
    byte_count BIGINT NOT NULL DEFAULT 0,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    timestamp BIGINT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_traffic_flows PRIMARY KEY (flow_id),
    CONSTRAINT chk_packet_count CHECK (packet_count >= 0),
    CONSTRAINT chk_byte_count CHECK (byte_count >= 0),
    CONSTRAINT chk_duration_ms CHECK (duration_ms >= 0),
    INDEX idx_flows_protocol (protocol),
    INDEX idx_flows_byte_count (byte_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
