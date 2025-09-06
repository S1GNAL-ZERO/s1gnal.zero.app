package io.signalzero.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Agent result entity matching database schema
 * Reference: DETAILED_DESIGN.md Section 6.1.3 - Agent Results Table
 */
@Entity
@Table(name = "agent_results", 
       indexes = {
           @Index(name = "idx_agent_results_analysis_id", columnList = "analysisId"),
           @Index(name = "idx_agent_results_agent_type", columnList = "agentType"),
           @Index(name = "idx_agent_results_status", columnList = "status")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_analysis_agent", columnNames = {"analysisId", "agentType"})
       })
public class AgentResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotNull
    @Column(name = "analysis_id", nullable = false)
    private UUID analysisId;
    
    @NotNull
    @Column(name = "agent_type", nullable = false, length = 50)
    private String agentType; // 'bot-detector', 'trend-analyzer', etc.
    
    @Column(name = "agent_version", length = 20)
    private String agentVersion = "1.0.0";
    
    // Results
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
    
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "confidence", precision = 5, scale = 2)
    private BigDecimal confidence;
    
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;
    
    // Evidence and details (stored as JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence", columnDefinition = "jsonb")
    private Map<String, Object> evidence;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_sources", columnDefinition = "jsonb")
    private String[] dataSources;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private Map<String, Object> rawData;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisStatus status = AnalysisStatus.PENDING;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Default constructor
    public AgentResult() {}
    
    // Constructor for new agent result
    public AgentResult(UUID analysisId, String agentType) {
        this.analysisId = analysisId;
        this.agentType = agentType;
        this.status = AnalysisStatus.PENDING;
        this.agentVersion = "1.0.0";
    }
    
    // Constructor with results
    public AgentResult(UUID analysisId, String agentType, BigDecimal score, 
                      BigDecimal confidence, Map<String, Object> evidence) {
        this(analysisId, agentType);
        this.score = score;
        this.confidence = confidence;
        this.evidence = evidence;
        this.status = AnalysisStatus.COMPLETE;
        this.completedAt = LocalDateTime.now();
    }
    
    // Business logic methods
    public boolean isCompleted() {
        return status == AnalysisStatus.COMPLETE;
    }
    
    public boolean isFailed() {
        return status.isFailed();
    }
    
    public boolean isInProgress() {
        return status.isInProgress();
    }
    
    public void completeWithResult(BigDecimal score, BigDecimal confidence, 
                                  Map<String, Object> evidence, Integer processingTimeMs) {
        this.score = score;
        this.confidence = confidence;
        this.evidence = evidence;
        this.processingTimeMs = processingTimeMs;
        this.status = AnalysisStatus.COMPLETE;
        this.completedAt = LocalDateTime.now();
    }
    
    public void failWithError(String errorMessage, Integer processingTimeMs) {
        this.errorMessage = errorMessage;
        this.processingTimeMs = processingTimeMs;
        this.status = AnalysisStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }
    
    public boolean isBotDetectorAgent() {
        return "bot-detector".equals(agentType);
    }
    
    public boolean isTrendAnalyzerAgent() {
        return "trend-analyzer".equals(agentType);
    }
    
    public boolean isReviewValidatorAgent() {
        return "review-validator".equals(agentType);
    }
    
    public boolean isPromotionDetectorAgent() {
        return "promotion-detector".equals(agentType);
    }
    
    public boolean isScoreAggregatorAgent() {
        return "score-aggregator".equals(agentType);
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAnalysisId() {
        return analysisId;
    }
    
    public void setAnalysisId(UUID analysisId) {
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
    
    public AnalysisStatus getStatus() {
        return status;
    }
    
    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    @Override
    public String toString() {
        return String.format("AgentResult{id=%s, analysisId=%s, agentType='%s', score=%s, status=%s}", 
                           id, analysisId, agentType, score, status);
    }
}
