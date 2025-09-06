package io.signalzero.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message wrapper for analysis requests sent to agents
 * Works directly with Analysis entities - no DTO conversion needed
 * Reference: DETAILED_DESIGN.md Section 8 - Repository Pattern
 */
public class AnalysisRequestMessage {
    
    @JsonProperty("analysisId")
    private String analysisId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("queryType")
    private String queryType;
    
    @JsonProperty("platform")
    private String platform;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    // Default constructor for Jackson
    public AnalysisRequestMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // Constructor for creating requests
    public AnalysisRequestMessage(String analysisId, String userId, String query, 
                                 String queryType, String platform) {
        this();
        this.analysisId = analysisId;
        this.userId = userId;
        this.query = query;
        this.queryType = queryType;
        this.platform = platform;
        this.correlationId = analysisId; // Use analysisId as correlation
    }
    
    // Getters and setters
    public String getAnalysisId() {
        return analysisId;
    }
    
    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getQueryType() {
        return queryType;
    }
    
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
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
    
    @Override
    public String toString() {
        return String.format("AnalysisRequestMessage{analysisId='%s', query='%s', platform='%s'}", 
                           analysisId, query, platform);
    }
}
