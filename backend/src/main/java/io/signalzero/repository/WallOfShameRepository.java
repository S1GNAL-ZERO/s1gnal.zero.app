package io.signalzero.repository;

import io.signalzero.model.ManipulationLevel;
import io.signalzero.model.WallOfShame;
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
import java.util.UUID;

/**
 * Repository for WallOfShame entity - Direct JPA operations
 * Reference: DETAILED_DESIGN.md Section 8 - Repository Pattern
 * No DTOs - work directly with WallOfShame entities
 */
@Repository
public interface WallOfShameRepository extends JpaRepository<WallOfShame, UUID> {
    
    // Active/Featured queries
    List<WallOfShame> findByIsActiveTrueOrderByDisplayOrderDescCreatedAtDesc();
    
    Page<WallOfShame> findByIsActiveTrueOrderByDisplayOrderDescCreatedAtDesc(Pageable pageable);
    
    List<WallOfShame> findByIsActiveTrueOrderByBotPercentageDesc();
    
    @Query("SELECT w FROM WallOfShame w WHERE w.isActive = true AND (w.featuredUntil IS NULL OR w.featuredUntil > :now) ORDER BY w.displayOrder DESC, w.createdAt DESC")
    List<WallOfShame> findActiveAndFeatured(@Param("now") LocalDateTime now);
    
    @Query("SELECT w FROM WallOfShame w WHERE w.isActive = true AND (w.featuredUntil IS NULL OR w.featuredUntil > :now) ORDER BY w.displayOrder DESC, w.createdAt DESC")
    Page<WallOfShame> findActiveAndFeatured(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Bot percentage queries - for Wall of Shame criteria
    List<WallOfShame> findByBotPercentageGreaterThanAndIsActiveTrueOrderByBotPercentageDesc(BigDecimal threshold);
    
    List<WallOfShame> findByBotPercentageBetweenAndIsActiveTrueOrderByBotPercentageDesc(BigDecimal min, BigDecimal max);
    
    @Query("SELECT w FROM WallOfShame w WHERE w.botPercentage > :threshold AND w.isActive = true ORDER BY w.botPercentage DESC LIMIT :limit")
    List<WallOfShame> findTopHighBotProducts(@Param("threshold") BigDecimal threshold, @Param("limit") int limit);
    
    // Reality Score queries
    List<WallOfShame> findByRealityScoreLessThanAndIsActiveTrueOrderByRealityScoreAsc(BigDecimal threshold);
    
    @Query("SELECT w FROM WallOfShame w WHERE w.realityScore < :threshold AND w.isActive = true ORDER BY w.realityScore ASC LIMIT :limit")
    List<WallOfShame> findTopManipulatedProducts(@Param("threshold") BigDecimal threshold, @Param("limit") int limit);
    
    // Manipulation level queries
    List<WallOfShame> findByManipulationLevelAndIsActiveTrueOrderByBotPercentageDesc(ManipulationLevel level);
    
    List<WallOfShame> findByManipulationLevelOrderByBotPercentageDesc(ManipulationLevel level);
    
    long countByManipulationLevel(ManipulationLevel level);
    
    // Category queries
    List<WallOfShame> findByCategoryAndIsActiveTrueOrderByBotPercentageDesc(String category);
    
    @Query("SELECT w.category, COUNT(w) FROM WallOfShame w WHERE w.isActive = true GROUP BY w.category ORDER BY COUNT(w) DESC")
    List<Object[]> getCategoryCounts();
    
    // Company queries
    List<WallOfShame> findByCompanyAndIsActiveTrueOrderByBotPercentageDesc(String company);
    
    @Query("SELECT w.company, COUNT(w) FROM WallOfShame w WHERE w.isActive = true AND w.company IS NOT NULL GROUP BY w.company ORDER BY COUNT(w) DESC")
    List<Object[]> getCompanyCounts();
    
    // Search queries
    List<WallOfShame> findByProductNameContainingIgnoreCaseAndIsActiveTrueOrderByBotPercentageDesc(String productName);
    
    @Query("SELECT w FROM WallOfShame w WHERE w.isActive = true AND (LOWER(w.productName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.company) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY w.botPercentage DESC")
    List<WallOfShame> searchByNameOrCompany(@Param("search") String search);
    
    // Analysis relationship queries
    List<WallOfShame> findByAnalysisIdOrderByCreatedAtDesc(UUID analysisId);
    
    boolean existsByAnalysisId(UUID analysisId);
    
    // Engagement queries
    List<WallOfShame> findByIsActiveTrueOrderByViewsDesc();
    
    List<WallOfShame> findByIsActiveTrueOrderBySharesDesc();
    
    @Query("SELECT w FROM WallOfShame w WHERE w.isActive = true AND w.views > :minViews ORDER BY w.views DESC")
    List<WallOfShame> findPopularEntries(@Param("minViews") int minViews);
    
    // Moderation queries
    List<WallOfShame> findByReportsGreaterThanAndIsActiveTrueOrderByReportsDesc(int minReports);
    
    @Query("SELECT w FROM WallOfShame w WHERE w.reports >= :threshold ORDER BY w.reports DESC")
    List<WallOfShame> findHighlyReported(@Param("threshold") int threshold);
    
    // Analytics queries
    @Query("SELECT COUNT(w) FROM WallOfShame w WHERE w.createdAt >= :date")
    long countEntriesAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(w) FROM WallOfShame w WHERE w.isActive = true")
    long countActiveEntries();
    
    @Query("SELECT DATE(w.createdAt), COUNT(w) FROM WallOfShame w WHERE w.createdAt >= :startDate GROUP BY DATE(w.createdAt) ORDER BY DATE(w.createdAt)")
    List<Object[]> getEntriesByDay(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT AVG(w.botPercentage) FROM WallOfShame w WHERE w.isActive = true")
    Double getAverageBotPercentage();
    
    @Query("SELECT AVG(w.realityScore) FROM WallOfShame w WHERE w.isActive = true")
    Double getAverageRealityScore();
    
    // Top entries for dashboard
    @Query("SELECT w FROM WallOfShame w WHERE w.isActive = true ORDER BY w.botPercentage DESC LIMIT :limit")
    List<WallOfShame> findTopEntriesByBotPercentage(@Param("limit") int limit);
    
    @Query("SELECT w FROM WallOfShame w WHERE w.isActive = true ORDER BY w.views DESC LIMIT :limit")
    List<WallOfShame> findTopEntriesByViews(@Param("limit") int limit);
    
    @Query("SELECT w FROM WallOfShame w WHERE w.isActive = true AND w.createdAt >= :since ORDER BY w.createdAt DESC LIMIT :limit")
    List<WallOfShame> findRecentEntries(@Param("since") LocalDateTime since, @Param("limit") int limit);
    
    // Update operations
    @Modifying
    @Query("UPDATE WallOfShame w SET w.views = w.views + 1 WHERE w.id = :id")
    int incrementViews(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE WallOfShame w SET w.shares = w.shares + 1 WHERE w.id = :id")
    int incrementShares(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE WallOfShame w SET w.reports = w.reports + 1 WHERE w.id = :id")
    int incrementReports(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE WallOfShame w SET w.isActive = false WHERE w.id = :id")
    int deactivateEntry(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE WallOfShame w SET w.featuredUntil = :until WHERE w.id = :id")
    int updateFeaturedUntil(@Param("id") UUID id, @Param("until") LocalDateTime until);
    
    @Modifying
    @Query("UPDATE WallOfShame w SET w.displayOrder = :order WHERE w.id = :id")
    int updateDisplayOrder(@Param("id") UUID id, @Param("order") Integer order);
    
    // Cleanup operations
    @Query("DELETE FROM WallOfShame w WHERE w.isActive = false AND w.createdAt < :cutoffDate")
    void deleteOldInactiveEntries(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("DELETE FROM WallOfShame w WHERE w.reports >= :threshold")
    void deleteHighlyReportedEntries(@Param("threshold") int threshold);
    
    // Demo data helpers for specific products
    @Query("SELECT w FROM WallOfShame w WHERE LOWER(w.productName) LIKE LOWER(CONCAT('%', :product, '%')) AND w.isActive = true ORDER BY w.createdAt DESC")
    List<WallOfShame> findByProductNameLike(@Param("product") String product);
    
    // Featured management
    @Query("SELECT w FROM WallOfShame w WHERE w.featuredUntil IS NOT NULL AND w.featuredUntil <= :now")
    List<WallOfShame> findExpiredFeatured(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE WallOfShame w SET w.featuredUntil = NULL WHERE w.featuredUntil <= :now")
    int clearExpiredFeatured(@Param("now") LocalDateTime now);
    
    // Additional methods for dashboard controller
    List<WallOfShame> findTop10ByIsActiveTrueOrderByBotPercentageDesc();
    
    Page<WallOfShame> findByIsActiveTrueOrderByBotPercentageDesc(Pageable pageable);
    
    // Count methods for statistics
    Long countByIsActive(Boolean isActive);
}
