package io.signalzero.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Message wrapper for agent responses
 * Converts directly to AgentResult entities via repository pattern
 * Reference: DETAILED_DESIGN.md Section 8 - Repository Pattern
 */
public class AgentResponseMessage {
    
    @JsonProperty("analysisId")
    private String analysisId;
    
    @JsonProperty("agentType")
    private String agentType;
    
    @JsonProperty("agentVersion")
    private String agentVersion = "1.0.0";
    
    @JsonProperty("score")
    private BigDecimal score;
    
    @JsonProperty("confidence")
    private BigDecimal confidence;
    
    @JsonProperty("processingTimeMs")
    private Integer processingTimeMs;
    
    @JsonProperty("evidence")
    private Map<String, Object> evidence;
    
    @JsonProperty("dataSources")
    private String[] dataSources;
    
    @JsonProperty("rawData")
    private Map<String, Object> rawData;
    
    @JsonProperty("status")
    private String status = "COMPLETE";
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    // Default constructor for Jackson
    public AgentResponseMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Constructor for successful responses
    public AgentResponseMessage(String analysisId, String agentType, BigDecimal score, 
                               BigDecimal confidence, Map<String, Object> evidence) {
        this();
        this.analysisId = analysisId;
        this.agentType = agentType;
        this.score = score;
        this.confidence = confidence;
        this.evidence = evidence;
        this.correlationId = analysisId;
    }
    
    // Constructor for error responses
    public AgentResponseMessage(String analysisId, String agentType, String errorMessage) {
        this();
        this.analysisId = analysisId;
        this.agentType = agentType;
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.correlationId = analysisId;
    }
    
    // Getters and setters
    public String getAnalysisId() {
        return analysisId;
    }
    
    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }
    
    public String getAgentType() {
        return agentType;
    }
    
    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }
    
    public String getAgentVersion() {
        return agentVersion;
    }
    
    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public BigDecimal getConfidence() {
        return confidence;
    }
    
    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }
    
    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public Map<String, Object> getEvidence() {
        return evidence;
    }
    
    public void setEvidence(Map<String, Object> evidence) {
        this.evidence = evidence;
    }
    
    public String[] getDataSources() {
        return dataSources;
    }
    
    public void setDataSources(String[] dataSources) {
        this.dataSources = dataSources;
    }
    
    public Map<String, Object> getRawData() {
        return rawData;
    }
    
    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    // Helper methods
    public boolean isSuccessful() {
        return "COMPLETE".equals(status);
    }
    
    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("AgentResponseMessage{analysisId='%s', agentType='%s', score=%s, status='%s'}", 
                           analysisId, agentType, score, status);
    }
}
