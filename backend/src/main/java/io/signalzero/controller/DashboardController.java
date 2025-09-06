package io.signalzero.controller;

import io.signalzero.model.Analysis;
import io.signalzero.model.AnalysisStatus;
import io.signalzero.model.User;
import io.signalzero.model.WallOfShame;
import io.signalzero.repository.AnalysisRepository;
import io.signalzero.repository.UserRepository;
import io.signalzero.repository.WallOfShameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for dashboard data and analytics
 * Reference: DETAILED_DESIGN.md Section 10
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:3000"})
public class DashboardController {
    
    private final AnalysisRepository analysisRepository;
    private final WallOfShameRepository wallOfShameRepository;
    private final UserRepository userRepository;

    /**
     * Get dashboard overview data
     * GET /api/dashboard/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getDashboardOverview() {
        try {
            User currentUser = getCurrentUser();
            
            // Get system-wide metrics
            long totalAnalyses = analysisRepository.count();
            long completedAnalyses = analysisRepository.countByStatus(AnalysisStatus.COMPLETE);
            long manipulatedProducts = analysisRepository.countByBotPercentageGreaterThan(BigDecimal.valueOf(60));
            long totalUsers = userRepository.count();
            
            // Get recent public analyses
            List<Analysis> recentAnalyses = analysisRepository
                .findTop10ByIsPublicTrueAndStatusOrderByCreatedAtDesc(AnalysisStatus.COMPLETE);
            
            // Get Wall of Shame entries
            List<WallOfShame> wallOfShame = wallOfShameRepository.findTop10ByIsActiveTrueOrderByBotPercentageDesc();
            
            // Calculate averages
            Double avgBotPercentage = calculateAverageBotPercentage();
            Double avgRealityScore = calculateAverageRealityScore();
            
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalAnalyses", totalAnalyses);
            overview.put("completedAnalyses", completedAnalyses);
            overview.put("manipulatedProducts", manipulatedProducts);
            overview.put("totalUsers", totalUsers);
            overview.put("completionRate", completedAnalyses * 100.0 / Math.max(1, totalAnalyses));
            overview.put("averageBotPercentage", avgBotPercentage != null ? avgBotPercentage : 0.0);
            overview.put("averageRealityScore", avgRealityScore != null ? avgRealityScore : 0.0);
            overview.put("recentAnalyses", recentAnalyses);
            overview.put("wallOfShame", wallOfShame);
            
            // Add user-specific data if authenticated
            if (currentUser != null) {
                overview.put("userAnalysesCount", analysisRepository.countByUserId(currentUser.getId()));
                overview.put("userAnalysesUsed", currentUser.getAnalysesUsedThisMonth());
                overview.put("userAnalysesRemaining", currentUser.getRemainingAnalyses());
            }

            return ResponseEntity.ok(overview);

        } catch (Exception e) {
            log.error("Failed to get dashboard overview", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve dashboard data"));
        }
    }

    /**
     * Get Wall of Shame data
     * GET /api/dashboard/wall-of-shame
     */
    @GetMapping("/wall-of-shame")
    public ResponseEntity<?> getWallOfShame(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "botPercentage") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WallOfShame> wallOfShame = wallOfShameRepository
                .findByIsActiveTrueOrderByBotPercentageDesc(pageable);

            return ResponseEntity.ok(Map.of(
                "content", wallOfShame.getContent(),
                "totalElements", wallOfShame.getTotalElements(),
                "totalPages", wallOfShame.getTotalPages(),
                "currentPage", page
            ));

        } catch (Exception e) {
            log.error("Failed to get Wall of Shame data", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve Wall of Shame"));
        }
    }

    /**
     * Get recent public analyses
     * GET /api/dashboard/recent-analyses
     */
    @GetMapping("/recent-analyses")
    public ResponseEntity<?> getRecentAnalyses(
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            Pageable pageable = PageRequest.of(0, limit, 
                Sort.by("createdAt").descending());
            
            Page<Analysis> recentAnalyses = analysisRepository
                .findByIsPublicTrueAndStatus(AnalysisStatus.COMPLETE, pageable);

            return ResponseEntity.ok(Map.of(
                "analyses", recentAnalyses.getContent(),
                "totalCount", recentAnalyses.getTotalElements()
            ));

        } catch (Exception e) {
            log.error("Failed to get recent analyses", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve recent analyses"));
        }
    }

    /**
     * Get trending manipulation patterns
     * GET /api/dashboard/trends
     */
    @GetMapping("/trends")
    public ResponseEntity<?> getTrends() {
        try {
            // Get manipulation distribution
            long highManipulation = analysisRepository
                .countByRealityScoreLessThanEqual(BigDecimal.valueOf(33));
            long mediumManipulation = analysisRepository
                .countByRealityScoreBetween(BigDecimal.valueOf(34), BigDecimal.valueOf(66));
            long lowManipulation = analysisRepository
                .countByRealityScoreGreaterThanEqual(BigDecimal.valueOf(67));

            // Get most manipulated categories (mock data for now)
            List<Map<String, Object>> categories = List.of(
                Map.of("category", "Consumer Electronics", "avgBotPercentage", 68.5, "count", 45),
                Map.of("category", "Fashion & Beauty", "avgBotPercentage", 62.3, "count", 38),
                Map.of("category", "Health & Wellness", "avgBotPercentage", 71.2, "count", 29),
                Map.of("category", "Gaming", "avgBotPercentage", 58.9, "count", 52),
                Map.of("category", "Food & Beverage", "avgBotPercentage", 64.1, "count", 33)
            );

            // Get platform breakdown (mock data for now)
            List<Map<String, Object>> platforms = List.of(
                Map.of("platform", "Twitter", "avgBotPercentage", 65.4, "count", 156),
                Map.of("platform", "Instagram", "avgBotPercentage", 71.2, "count", 124),
                Map.of("platform", "TikTok", "avgBotPercentage", 68.8, "count", 89),
                Map.of("platform", "Reddit", "avgBotPercentage", 52.1, "count", 67),
                Map.of("platform", "YouTube", "avgBotPercentage", 59.3, "count", 78)
            );

            return ResponseEntity.ok(Map.of(
                "manipulationDistribution", Map.of(
                    "high", highManipulation,
                    "medium", mediumManipulation,
                    "low", lowManipulation
                ),
                "topCategories", categories,
                "platformBreakdown", platforms,
                "lastUpdated", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("Failed to get trends", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve trends"));
        }
    }

    /**
     * Get system health metrics
     * GET /api/dashboard/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> getSystemHealth() {
        try {
            // System processing metrics
            long processingAnalyses = analysisRepository.countByStatus(AnalysisStatus.PROCESSING);
            long pendingAnalyses = analysisRepository.countByStatus(AnalysisStatus.PENDING);
            long failedAnalyses = analysisRepository.countByStatus(AnalysisStatus.FAILED);
            
            // Calculate success rate
            long totalCompleted = analysisRepository.countByStatus(AnalysisStatus.COMPLETE);
            long totalProcessed = totalCompleted + failedAnalyses;
            double successRate = totalProcessed > 0 ? (totalCompleted * 100.0) / totalProcessed : 100.0;

            // Mock performance metrics (in production, these would come from monitoring)
            Map<String, Object> performance = Map.of(
                "avgProcessingTime", "2.8s",
                "agentResponseTime", "1.2s",
                "systemLoad", 0.65,
                "memoryUsage", 0.72,
                "activeConnections", 45
            );

            return ResponseEntity.ok(Map.of(
                "processing", processingAnalyses,
                "pending", pendingAnalyses,
                "failed", failedAnalyses,
                "successRate", Math.round(successRate * 100.0) / 100.0,
                "performance", performance,
                "status", processingAnalyses < 10 ? "healthy" : "busy",
                "lastCheck", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("Failed to get system health", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve system health"));
        }
    }

    /**
     * Search analyses by query
     * GET /api/dashboard/search
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAnalyses(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            if (q == null || q.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Search query is required"));
            }

            Pageable pageable = PageRequest.of(page, size, 
                Sort.by("createdAt").descending());
            
            Page<Analysis> searchResults = analysisRepository
                .findByQueryContainingIgnoreCaseAndIsPublicTrueAndStatus(
                    q.trim(), AnalysisStatus.COMPLETE, pageable);

            return ResponseEntity.ok(Map.of(
                "query", q,
                "results", searchResults.getContent(),
                "totalElements", searchResults.getTotalElements(),
                "totalPages", searchResults.getTotalPages(),
                "currentPage", page
            ));

        } catch (Exception e) {
            log.error("Failed to search analyses", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Search failed"));
        }
    }

    // Helper methods

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return null;
            }

            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            return null;
        }
    }

    private Double calculateAverageBotPercentage() {
        try {
            List<Analysis> completedAnalyses = analysisRepository
                .findByStatusAndBotPercentageIsNotNull(AnalysisStatus.COMPLETE);
            
            if (completedAnalyses.isEmpty()) {
                return 0.0;
            }

            double sum = completedAnalyses.stream()
                .mapToDouble(analysis -> analysis.getBotPercentage().doubleValue())
                .sum();
                
            return sum / completedAnalyses.size();
        } catch (Exception e) {
            log.error("Failed to calculate average bot percentage", e);
            return 0.0;
        }
    }

    private Double calculateAverageRealityScore() {
        try {
            List<Analysis> completedAnalyses = analysisRepository
                .findByStatusAndRealityScoreIsNotNull(AnalysisStatus.COMPLETE);
            
            if (completedAnalyses.isEmpty()) {
                return 0.0;
            }

            double sum = completedAnalyses.stream()
                .mapToDouble(analysis -> analysis.getRealityScore().doubleValue())
                .sum();
                
            return sum / completedAnalyses.size();
        } catch (Exception e) {
            log.error("Failed to calculate average reality score", e);
            return 0.0;
        }
    }
}
