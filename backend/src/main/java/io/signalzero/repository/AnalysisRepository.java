package io.signalzero.repository;

import io.signalzero.model.Analysis;
import io.signalzero.model.AnalysisStatus;
import io.signalzero.model.ManipulationLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Analysis entity - Direct JPA operations
 * Reference: DETAILED_DESIGN.md Section 8 - Repository Pattern
 * No DTOs - work directly with Analysis entities
 */
@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    
    // User-specific queries
    List<Analysis> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Page<Analysis> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    List<Analysis> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, AnalysisStatus status);
    
    long countByUserId(UUID userId);
    
    long countByUserIdAndStatus(UUID userId, AnalysisStatus status);
    
    // Public analyses for Wall of Shame and dashboard
    List<Analysis> findByIsPublicTrueOrderByCreatedAtDesc();
    
    Page<Analysis> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    
    List<Analysis> findByIsPublicTrueAndStatusOrderByCreatedAtDesc(AnalysisStatus status);
    
    // Wall of Shame queries - high bot percentage analyses
    @Query("SELECT a FROM Analysis a WHERE a.isPublic = true AND a.status = 'COMPLETE' AND a.botPercentage > :threshold ORDER BY a.botPercentage DESC")
    List<Analysis> findHighBotAnalyses(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT a FROM Analysis a WHERE a.isPublic = true AND a.status = 'COMPLETE' AND a.botPercentage > :threshold ORDER BY a.botPercentage DESC")
    Page<Analysis> findHighBotAnalyses(@Param("threshold") BigDecimal threshold, Pageable pageable);
    
    // Reality Score queries
    List<Analysis> findByRealityScoreLessThanAndStatusOrderByRealityScoreAsc(BigDecimal threshold, AnalysisStatus status);
    
    List<Analysis> findByRealityScoreBetweenAndStatusOrderByCreatedAtDesc(BigDecimal min, BigDecimal max, AnalysisStatus status);
    
    @Query("SELECT a FROM Analysis a WHERE a.realityScore < :score AND a.status = 'COMPLETE' AND a.isPublic = true ORDER BY a.realityScore ASC")
    List<Analysis> findManipulatedAnalyses(@Param("score") BigDecimal score);
    
    // Manipulation level queries
    List<Analysis> findByManipulationLevelAndStatusOrderByCreatedAtDesc(ManipulationLevel level, AnalysisStatus status);
    
    List<Analysis> findByManipulationLevelAndIsPublicTrueOrderByCreatedAtDesc(ManipulationLevel level);
    
    // Status queries
    List<Analysis> findByStatus(AnalysisStatus status);
    
    List<Analysis> findByStatusOrderByCreatedAtAsc(AnalysisStatus status);
    
    long countByStatus(AnalysisStatus status);
    
    // Processing time queries
    @Query("SELECT a FROM Analysis a WHERE a.status = 'PROCESSING' AND a.startedAt < :timeout")
    List<Analysis> findTimedOutAnalyses(@Param("timeout") LocalDateTime timeout);
    
    @Query("SELECT AVG(a.processingTimeMs) FROM Analysis a WHERE a.status = 'COMPLETE' AND a.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTime();
    
    // Query search
    List<Analysis> findByQueryContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(String query, AnalysisStatus status);
    
    List<Analysis> findByQueryContainingIgnoreCaseAndIsPublicTrueOrderByCreatedAtDesc(String query);
    
    boolean existsByQueryIgnoreCaseAndUserId(String query, UUID userId);
    
    Optional<Analysis> findFirstByQueryIgnoreCaseAndUserIdOrderByCreatedAtDesc(String query, UUID userId);
    
    // Platform queries
    List<Analysis> findByPlatformAndStatusOrderByCreatedAtDesc(String platform, AnalysisStatus status);
    
    // Solace correlation
    Optional<Analysis> findBySolaceCorrelationId(String correlationId);
    
    // Featured analyses
    List<Analysis> findByIsFeaturedTrueAndStatusOrderByCreatedAtDesc(AnalysisStatus status);
    
    @Modifying
    @Query("UPDATE Analysis a SET a.isFeatured = :featured WHERE a.id = :analysisId")
    int updateFeatured(@Param("analysisId") UUID analysisId, @Param("featured") boolean featured);
    
    // Analytics queries
    @Query("SELECT COUNT(a) FROM Analysis a WHERE a.createdAt >= :date")
    long countAnalysesAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(a) FROM Analysis a WHERE a.createdAt >= :date AND a.status = 'COMPLETE'")
    long countCompletedAnalysesAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM Analysis a WHERE a.createdAt >= :startDate GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
    List<Object[]> getAnalysesByDay(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a.manipulationLevel, COUNT(a) FROM Analysis a WHERE a.status = 'COMPLETE' GROUP BY a.manipulationLevel")
    List<Object[]> getAnalysesByManipulationLevel();
    
    @Query("SELECT AVG(a.botPercentage) FROM Analysis a WHERE a.status = 'COMPLETE' AND a.botPercentage IS NOT NULL")
    Double getAverageBotPercentage();
    
    @Query("SELECT AVG(a.realityScore) FROM Analysis a WHERE a.status = 'COMPLETE' AND a.realityScore IS NOT NULL")
    Double getAverageRealityScore();
    
    // Recent trending queries
    @Query("SELECT a.query, COUNT(a) as queryCount FROM Analysis a WHERE a.createdAt >= :since GROUP BY a.query ORDER BY queryCount DESC")
    List<Object[]> getTrendingQueries(@Param("since") LocalDateTime since);
    
    @Query("SELECT a.platform, COUNT(a) as platformCount FROM Analysis a WHERE a.createdAt >= :since AND a.platform IS NOT NULL GROUP BY a.platform ORDER BY platformCount DESC")
    List<Object[]> getTrendingPlatforms(@Param("since") LocalDateTime since);
    
    // Demo data helpers for hardcoded values
    @Query("SELECT a FROM Analysis a WHERE LOWER(a.query) LIKE %:query% AND a.status = 'COMPLETE' ORDER BY a.createdAt DESC")
    List<Analysis> findByQueryLikeIgnoreCase(@Param("query") String query);
    
    // Status updates
    @Modifying
    @Query("UPDATE Analysis a SET a.status = :status WHERE a.id = :analysisId")
    int updateStatus(@Param("analysisId") UUID analysisId, @Param("status") AnalysisStatus status);
    
    @Modifying
    @Query("UPDATE Analysis a SET a.status = :status, a.startedAt = :startTime WHERE a.id = :analysisId")
    int updateStatusAndStartTime(@Param("analysisId") UUID analysisId, @Param("status") AnalysisStatus status, @Param("startTime") LocalDateTime startTime);
    
    @Modifying
    @Query("UPDATE Analysis a SET a.status = :status, a.completedAt = :completedTime, a.processingTimeMs = :processingTime WHERE a.id = :analysisId")
    int updateStatusAndCompletionTime(@Param("analysisId") UUID analysisId, @Param("status") AnalysisStatus status, @Param("completedTime") LocalDateTime completedTime, @Param("processingTime") Integer processingTime);
    
    // Business logic helpers
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Analysis a WHERE a.query = :query AND a.userId = :userId AND a.createdAt > :since")
    boolean hasUserAnalyzedQueryRecently(@Param("query") String query, @Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    // Additional methods for controllers
    long countByBotPercentageGreaterThan(BigDecimal threshold);
    
    // Additional method for DashboardView that just needs AnalysisStatus
    List<Analysis> findTop10ByIsPublicTrueAndStatusOrderByCreatedAtDesc(AnalysisStatus status);
    
    Page<Analysis> findByIsPublicTrueAndStatus(AnalysisStatus status, Pageable pageable);
    
    long countByRealityScoreLessThanEqual(BigDecimal score);
    
    long countByRealityScoreBetween(BigDecimal min, BigDecimal max);
    
    long countByRealityScoreGreaterThanEqual(BigDecimal score);
    
    Page<Analysis> findByQueryContainingIgnoreCaseAndIsPublicTrueAndStatus(String query, AnalysisStatus status, Pageable pageable);
    
    List<Analysis> findByStatusAndBotPercentageIsNotNull(AnalysisStatus status);
    
    List<Analysis> findByStatusAndRealityScoreIsNotNull(AnalysisStatus status);
    
    List<Analysis> findTop10ByStatusOrderByCreatedAtDesc(AnalysisStatus status);
    
    Page<Analysis> findByIsPublicTrueAndBotPercentageGreaterThanOrderByBotPercentageDesc(BigDecimal threshold, Pageable pageable);
    
    // Missing method for DashboardView
    List<Analysis> findByStatusOrderByCreatedAtDesc(AnalysisStatus status);
}
