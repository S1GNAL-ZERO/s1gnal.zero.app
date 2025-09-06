package io.signalzero.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Analysis entity matching database schema
 * Reference: DETAILED_DESIGN.md Section 6.1.2 - Analyses Table
 */
@Entity
@Table(name = "analyses", indexes = {
    @Index(name = "idx_analyses_user_id", columnList = "userId"),
    @Index(name = "idx_analyses_status", columnList = "status"),
    @Index(name = "idx_analyses_created_at", columnList = "createdAt"),
    @Index(name = "idx_analyses_reality_score", columnList = "realityScore"),
    @Index(name = "idx_analyses_bot_percentage", columnList = "botPercentage"),
    @Index(name = "idx_analyses_is_public", columnList = "isPublic"),
    @Index(name = "idx_analyses_correlation_id", columnList = "solaceCorrelationId")
})
public class Analysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    // Query details
    @NotBlank
    @Column(name = "query", nullable = false, columnDefinition = "TEXT")
    private String query;
    
    @Column(name = "query_type", length = 50)
    private String queryType; // 'product', 'influencer', 'stock', 'trend', 'event'
    
    @Column(name = "platform", length = 50)
    private String platform; // 'twitter', 'instagram', 'tiktok', 'reddit', 'amazon', 'all'
    
    // Analysis results
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "reality_score", precision = 5, scale = 2)
    private BigDecimal realityScore;
    
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "bot_percentage", precision = 5, scale = 2)
    private BigDecimal botPercentage;
    
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "trend_score", precision = 5, scale = 2)
    private BigDecimal trendScore;
    
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "review_score", precision = 5, scale = 2)
    private BigDecimal reviewScore;
    
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "promotion_score", precision = 5, scale = 2)
    private BigDecimal promotionScore;
    
    // Manipulation classification
    @Enumerated(EnumType.STRING)
    @Column(name = "manipulation_level", length = 20)
    private ManipulationLevel manipulationLevel;
    
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;
    
    // Processing details
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisStatus status = AnalysisStatus.PENDING;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;
    
    // Solace messaging
    @Column(name = "solace_correlation_id", unique = true)
    private String solaceCorrelationId;
    
    @Column(name = "solace_request_topic")
    private String solaceRequestTopic;
    
    // Visibility
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Relationships
    @OneToMany(mappedBy = "analysisId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AgentResult> agentResults = new ArrayList<>();
    
    // Default constructor
    public Analysis() {}
    
    // Constructor for new analysis
    public Analysis(UUID userId, String query, String queryType, String platform) {
        this.userId = userId;
        this.query = query;
        this.queryType = queryType;
        this.platform = platform;
        this.status = AnalysisStatus.PENDING;
        this.isPublic = true;
        this.isFeatured = false;
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
    
    public void startProcessing() {
        this.status = AnalysisStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }
    
    public void completeAnalysis(BigDecimal realityScore, BigDecimal botPercentage) {
        this.status = AnalysisStatus.COMPLETE;
        this.realityScore = realityScore;
        this.botPercentage = botPercentage;
        this.completedAt = LocalDateTime.now();
        
        // Calculate manipulation level
        this.manipulationLevel = calculateManipulationLevel(realityScore);
        
        // Calculate processing time
        if (startedAt != null) {
            this.processingTimeMs = (int) java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }
    
    public void failAnalysis(String errorMessage) {
        this.status = AnalysisStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        
        if (startedAt != null) {
            this.processingTimeMs = (int) java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }
    
    private ManipulationLevel calculateManipulationLevel(BigDecimal realityScore) {
        if (realityScore == null) return ManipulationLevel.YELLOW;
        
        if (realityScore.compareTo(BigDecimal.valueOf(67)) >= 0) {
            return ManipulationLevel.GREEN; // 67-100%: Authentic
        } else if (realityScore.compareTo(BigDecimal.valueOf(34)) >= 0) {
            return ManipulationLevel.YELLOW; // 34-66%: Mixed signals
        } else {
            return ManipulationLevel.RED; // 0-33%: Heavily manipulated
        }
    }
    
    public boolean isHighlyManipulated() {
        return manipulationLevel == ManipulationLevel.RED;
    }
    
    public boolean shouldBeOnWallOfShame() {
        return isCompleted() && botPercentage != null && 
               botPercentage.compareTo(BigDecimal.valueOf(60)) > 0;
    }
    
    // Calculate Reality Score using EXACT weights from DETAILED_DESIGN.md Section 9.2
    public void calculateAndSetRealityScore() {
        // Always calculate if we have at least bot percentage (core requirement)
        if (botPercentage != null) {
            // Bot: 40%, Trend: 30%, Review: 20%, Promotion: 10%
            BigDecimal botScore = BigDecimal.valueOf(100).subtract(botPercentage);
            
            this.realityScore = botScore.multiply(BigDecimal.valueOf(0.4))
                .add((trendScore != null ? trendScore : BigDecimal.valueOf(50)).multiply(BigDecimal.valueOf(0.3)))
                .add((reviewScore != null ? reviewScore : BigDecimal.valueOf(50)).multiply(BigDecimal.valueOf(0.2)))
                .add((promotionScore != null ? promotionScore : BigDecimal.valueOf(50)).multiply(BigDecimal.valueOf(0.1)))
                .setScale(2, RoundingMode.HALF_UP);
                
            this.manipulationLevel = calculateManipulationLevel(this.realityScore);
        }
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
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
    
    public BigDecimal getRealityScore() {
        return realityScore;
    }
    
    public void setRealityScore(BigDecimal realityScore) {
        this.realityScore = realityScore;
        this.manipulationLevel = calculateManipulationLevel(realityScore);
    }
    
    public BigDecimal getBotPercentage() {
        return botPercentage;
    }
    
    public void setBotPercentage(BigDecimal botPercentage) {
        this.botPercentage = botPercentage;
    }
    
    public BigDecimal getTrendScore() {
        return trendScore;
    }
    
    public void setTrendScore(BigDecimal trendScore) {
        this.trendScore = trendScore;
    }
    
    public BigDecimal getReviewScore() {
        return reviewScore;
    }
    
    public void setReviewScore(BigDecimal reviewScore) {
        this.reviewScore = reviewScore;
    }
    
    public BigDecimal getPromotionScore() {
        return promotionScore;
    }
    
    public void setPromotionScore(BigDecimal promotionScore) {
        this.promotionScore = promotionScore;
    }
    
    public ManipulationLevel getManipulationLevel() {
        return manipulationLevel;
    }
    
    public void setManipulationLevel(ManipulationLevel manipulationLevel) {
        this.manipulationLevel = manipulationLevel;
    }
    
    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
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
    
    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public String getSolaceCorrelationId() {
        return solaceCorrelationId;
    }
    
    public void setSolaceCorrelationId(String solaceCorrelationId) {
        this.solaceCorrelationId = solaceCorrelationId;
    }
    
    public String getSolaceRequestTopic() {
        return solaceRequestTopic;
    }
    
    public void setSolaceRequestTopic(String solaceRequestTopic) {
        this.solaceRequestTopic = solaceRequestTopic;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public List<AgentResult> getAgentResults() {
        return agentResults;
    }
    
    public void setAgentResults(List<AgentResult> agentResults) {
        this.agentResults = agentResults;
    }
    
    @Override
    public String toString() {
        return String.format("Analysis{id=%s, query='%s', status=%s, realityScore=%s}", 
                           id, query, status, realityScore);
    }
    
}
