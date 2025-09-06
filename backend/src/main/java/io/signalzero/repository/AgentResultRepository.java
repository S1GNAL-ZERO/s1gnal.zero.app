package io.signalzero.repository;

import io.signalzero.model.AgentResult;
import io.signalzero.model.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AgentResult entity - Direct JPA operations
 * Reference: DETAILED_DESIGN.md Section 8 - Repository Pattern
 * No DTOs - work directly with AgentResult entities
 */
@Repository
public interface AgentResultRepository extends JpaRepository<AgentResult, UUID> {
    
    // Analysis-specific queries
    List<AgentResult> findByAnalysisIdOrderByCreatedAtAsc(UUID analysisId);
    
    List<AgentResult> findByAnalysisIdAndStatusOrderByCreatedAtAsc(UUID analysisId, AnalysisStatus status);
    
    long countByAnalysisId(UUID analysisId);
    
    long countByAnalysisIdAndStatus(UUID analysisId, AnalysisStatus status);
    
    // Agent type queries
    List<AgentResult> findByAgentTypeOrderByCreatedAtDesc(String agentType);
    
    Optional<AgentResult> findByAnalysisIdAndAgentType(UUID analysisId, String agentType);
    
    List<AgentResult> findByAgentTypeAndStatusOrderByCreatedAtDesc(String agentType, AnalysisStatus status);
    
    // Check if analysis has all agent results
    @Query("SELECT COUNT(DISTINCT ar.agentType) FROM AgentResult ar WHERE ar.analysisId = :analysisId AND ar.status = 'COMPLETE'")
    long countCompletedAgentsByAnalysis(@Param("analysisId") UUID analysisId);
    
    @Query("SELECT ar.agentType FROM AgentResult ar WHERE ar.analysisId = :analysisId AND ar.status = 'COMPLETE'")
    List<String> getCompletedAgentTypesByAnalysis(@Param("analysisId") UUID analysisId);
    
    @Query("SELECT ar.agentType FROM AgentResult ar WHERE ar.analysisId = :analysisId AND ar.status != 'COMPLETE'")
    List<String> getPendingAgentTypesByAnalysis(@Param("analysisId") UUID analysisId);
    
    // Status queries
    List<AgentResult> findByStatus(AnalysisStatus status);
    
    List<AgentResult> findByStatusOrderByCreatedAtAsc(AnalysisStatus status);
    
    long countByStatus(AnalysisStatus status);
    
    long countByAgentTypeAndStatus(String agentType, AnalysisStatus status);
    
    // Performance queries
    @Query("SELECT AVG(ar.processingTimeMs) FROM AgentResult ar WHERE ar.agentType = :agentType AND ar.status = 'COMPLETE' AND ar.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTimeByAgent(@Param("agentType") String agentType);
    
    @Query("SELECT ar.agentType, AVG(ar.processingTimeMs) FROM AgentResult ar WHERE ar.status = 'COMPLETE' AND ar.processingTimeMs IS NOT NULL GROUP BY ar.agentType")
    List<Object[]> getAverageProcessingTimeByAllAgents();
    
    @Query("SELECT MAX(ar.processingTimeMs) FROM AgentResult ar WHERE ar.analysisId = :analysisId AND ar.status = 'COMPLETE'")
    Integer getMaxProcessingTimeForAnalysis(@Param("analysisId") UUID analysisId);
    
    // Score queries
    @Query("SELECT AVG(ar.score) FROM AgentResult ar WHERE ar.agentType = :agentType AND ar.status = 'COMPLETE' AND ar.score IS NOT NULL")
    Double getAverageScoreByAgent(@Param("agentType") String agentType);
    
    @Query("SELECT AVG(ar.confidence) FROM AgentResult ar WHERE ar.agentType = :agentType AND ar.status = 'COMPLETE' AND ar.confidence IS NOT NULL")
    Double getAverageConfidenceByAgent(@Param("agentType") String agentType);
    
    List<AgentResult> findByScoreLessThanAndStatusOrderByScoreAsc(BigDecimal threshold, AnalysisStatus status);
    
    List<AgentResult> findByScoreGreaterThanAndStatusOrderByScoreDesc(BigDecimal threshold, AnalysisStatus status);
    
    // Timeout queries
    @Query("SELECT ar FROM AgentResult ar WHERE ar.status = 'PROCESSING' AND ar.createdAt < :timeout")
    List<AgentResult> findTimedOutAgentResults(@Param("timeout") LocalDateTime timeout);
    
    @Query("SELECT COUNT(ar) FROM AgentResult ar WHERE ar.status = 'PROCESSING' AND ar.createdAt < :timeout")
    long countTimedOutAgentResults(@Param("timeout") LocalDateTime timeout);
    
    // Analysis completion checks
    @Query("SELECT CASE WHEN COUNT(ar) >= 5 AND COUNT(CASE WHEN ar.status = 'COMPLETE' THEN 1 END) = COUNT(ar) THEN true ELSE false END FROM AgentResult ar WHERE ar.analysisId = :analysisId")
    boolean isAnalysisCompleteByAllAgents(@Param("analysisId") UUID analysisId);
    
    @Query("SELECT CASE WHEN COUNT(CASE WHEN ar.status = 'COMPLETE' THEN 1 END) >= 3 THEN true ELSE false END FROM AgentResult ar WHERE ar.analysisId = :analysisId")
    boolean hasAnalysisMinimumResults(@Param("analysisId") UUID analysisId);
    
    // Agent version tracking
    List<AgentResult> findByAgentVersionOrderByCreatedAtDesc(String agentVersion);
    
    @Query("SELECT ar.agentVersion, COUNT(ar) FROM AgentResult ar GROUP BY ar.agentVersion ORDER BY COUNT(ar) DESC")
    List<Object[]> getAgentVersionUsage();
    
    // Data source tracking - TODO: Implement with native query if needed
    // @Query("SELECT ar FROM AgentResult ar WHERE ar.dataSources @> CAST(:dataSource AS jsonb)")
    // List<AgentResult> findByDataSourceContaining(@Param("dataSource") String dataSource);
    
    // Error tracking
    List<AgentResult> findByStatusAndErrorMessageIsNotNullOrderByCreatedAtDesc(AnalysisStatus status);
    
    @Query("SELECT ar.agentType, COUNT(ar) FROM AgentResult ar WHERE ar.status = 'FAILED' GROUP BY ar.agentType ORDER BY COUNT(ar) DESC")
    List<Object[]> getFailureCountsByAgent();
    
    // Recent activity
    @Query("SELECT COUNT(ar) FROM AgentResult ar WHERE ar.createdAt >= :date")
    long countAgentResultsAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(ar) FROM AgentResult ar WHERE ar.createdAt >= :date AND ar.status = 'COMPLETE'")
    long countCompletedAgentResultsAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT ar.agentType, COUNT(ar) FROM AgentResult ar WHERE ar.createdAt >= :date GROUP BY ar.agentType ORDER BY COUNT(ar) DESC")
    List<Object[]> getAgentActivityAfter(@Param("date") LocalDateTime date);
    
    // Demo data helpers for specific agents
    @Query("SELECT ar FROM AgentResult ar WHERE ar.analysisId = :analysisId AND ar.agentType = 'bot-detector' AND ar.status = 'COMPLETE'")
    Optional<AgentResult> findBotDetectorResult(@Param("analysisId") UUID analysisId);
    
    @Query("SELECT ar FROM AgentResult ar WHERE ar.analysisId = :analysisId AND ar.agentType = 'trend-analyzer' AND ar.status = 'COMPLETE'")
    Optional<AgentResult> findTrendAnalyzerResult(@Param("analysisId") UUID analysisId);
    
    @Query("SELECT ar FROM AgentResult ar WHERE ar.analysisId = :analysisId AND ar.agentType = 'score-aggregator' AND ar.status = 'COMPLETE'")
    Optional<AgentResult> findScoreAggregatorResult(@Param("analysisId") UUID analysisId);
    
    // Cleanup operations
    @Query("DELETE FROM AgentResult ar WHERE ar.status = 'FAILED' AND ar.createdAt < :cutoffDate")
    void deleteOldFailedResults(@Param("cutoffDate") LocalDateTime cutoffDate);
}
