package netflow.model;

import netflow.util.ValidationUtils;

import java.util.Objects;

/**
 * Represents a security or network operational alert triggered within the NetFlow system.
 */
public class NetworkAlert {

    private String alertId;
    private AlertSeverity severity;
    private String message;
    private Long generatedAt;
    private String relatedFlowId;
    private Boolean acknowledged;

    /**
     * Convenience constructor defaulting generatedAt to current epoch ms and acknowledged to false.
     */
    public NetworkAlert(String alertId, AlertSeverity severity, String message, String relatedFlowId) {
        this(alertId, severity, message, System.currentTimeMillis(), relatedFlowId, Boolean.FALSE);
    }

    /**
     * Complete constructor validating alert attributes.
     */
    public NetworkAlert(String alertId, AlertSeverity severity, String message,
                        Long generatedAt, String relatedFlowId, Boolean acknowledged) {
        setAlertId(alertId);
        setSeverity(severity);
        setMessage(message);
        setGeneratedAt(generatedAt);
        setRelatedFlowId(relatedFlowId);
        setAcknowledged(acknowledged);
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        if (ValidationUtils.isBlank(alertId)) {
            throw new IllegalArgumentException("Alert ID cannot be null or empty");
        }
        this.alertId = alertId.trim();
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        if (severity == null) {
            throw new IllegalArgumentException("Alert severity cannot be null");
        }
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (ValidationUtils.isBlank(message)) {
            throw new IllegalArgumentException("Alert message cannot be null or empty");
        }
        this.message = message.trim();
    }

    public Long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Long generatedAt) {
        if (generatedAt == null || generatedAt < 0) {
            throw new IllegalArgumentException("Generated timestamp must be non-null and >= 0");
        }
        this.generatedAt = generatedAt;
    }

    public String getRelatedFlowId() {
        return relatedFlowId;
    }

    public void setRelatedFlowId(String relatedFlowId) {
        this.relatedFlowId = (relatedFlowId != null && !relatedFlowId.trim().isEmpty())
                ? relatedFlowId.trim()
                : null;
    }

    public Boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = (acknowledged != null) ? acknowledged : Boolean.FALSE;
    }

    public void acknowledge() {
        this.acknowledged = Boolean.TRUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkAlert that)) return false;
        return Objects.equals(alertId, that.alertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("[").append(severity).append("] ")
          .append("Alert ").append(alertId).append(": ")
          .append(message)
          .append(" | Flow: ").append(relatedFlowId != null ? relatedFlowId : "None")
          .append(" | Ack: ").append(acknowledged);
        return sb.toString();
    }
}
