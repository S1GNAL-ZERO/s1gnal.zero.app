package io.signalzero.controller;

import io.signalzero.model.Analysis;
import io.signalzero.model.AnalysisStatus;
import io.signalzero.model.User;
import io.signalzero.repository.AnalysisRepository;
import io.signalzero.repository.UserRepository;
import io.signalzero.service.AnalysisService;
import io.signalzero.service.UsageTrackingService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for analysis operations
 * Reference: DETAILED_DESIGN.md Section 10
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:3000"})
public class AnalysisController {
    
    private final AnalysisService analysisService;
    private final UsageTrackingService usageTrackingService;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;

    /**
     * Submit new analysis request
     * POST /api/analysis
     */
    @PostMapping
    public ResponseEntity<?> createAnalysis(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            String platform = request.get("platform");
            String queryType = request.get("queryType");

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Query is required"));
            }

            // Get current user
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
            }

            // Check usage limits
            if (!usageTrackingService.canUserAnalyze(user)) {
                Map<String, Object> limitResponse = Map.of(
                    "error", "Usage limit exceeded",
                    "message", "You have reached your monthly analysis limit",
                    "currentUsage", user.getAnalysesUsedThisMonth(),
                    "limit", usageTrackingService.getMonthlyLimit(user.getSubscriptionTier()),
                    "subscriptionTier", user.getSubscriptionTier().name(),
                    "upgradeRequired", true
                );
                return ResponseEntity.status(429).body(limitResponse);
            }

            // Create and submit analysis
            Analysis analysis = analysisService.createAnalysis(user, query, platform, queryType);
            
            // Submit to agents for processing
            analysisService.submitForProcessing(analysis);

            log.info("Analysis created and submitted: {} for user: {}", 
                analysis.getId(), user.getEmail());

            return ResponseEntity.ok(Map.of(
                "analysisId", analysis.getId(),
                "status", analysis.getStatus().name(),
                "query", analysis.getQuery(),
                "message", "Analysis submitted successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to create analysis", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to create analysis: " + e.getMessage()));
        }
    }

    /**
     * Get analysis by ID
     * GET /api/analysis/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAnalysis(@PathVariable UUID id) {
        try {
            Analysis analysis = analysisRepository.findById(id).orElse(null);
            if (analysis == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user can access this analysis
            User currentUser = getCurrentUser();
            if (!canAccessAnalysis(analysis, currentUser)) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied"));
            }

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            log.error("Failed to get analysis: {}", id, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve analysis"));
        }
    }

    /**
     * Get user's analyses
     * GET /api/analysis/my
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Analysis> analyses = analysisRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), pageable);

            return ResponseEntity.ok(Map.of(
                "content", analyses.getContent(),
                "totalElements", analyses.getTotalElements(),
                "totalPages", analyses.getTotalPages(),
                "currentPage", page
            ));

        } catch (Exception e) {
            log.error("Failed to get user analyses", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve analyses"));
        }
    }

    /**
     * Get public analyses (Wall of Shame)
     * GET /api/analysis/public
     */
    @GetMapping("/public")
    public ResponseEntity<?> getPublicAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by("botPercentage").descending());
            
            Page<Analysis> analyses = analysisRepository
                .findByIsPublicTrueAndBotPercentageGreaterThanOrderByBotPercentageDesc(
                    true, java.math.BigDecimal.valueOf(60), pageable);

            return ResponseEntity.ok(Map.of(
                "content", analyses.getContent(),
                "totalElements", analyses.getTotalElements(),
                "totalPages", analyses.getTotalPages(),
                "currentPage", page
            ));

        } catch (Exception e) {
            log.error("Failed to get public analyses", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve public analyses"));
        }
    }

    /**
     * Get analysis status
     * GET /api/analysis/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getAnalysisStatus(@PathVariable UUID id) {
        try {
            Analysis analysis = analysisRepository.findById(id).orElse(null);
            if (analysis == null) {
                return ResponseEntity.notFound().build();
            }

            User currentUser = getCurrentUser();
            if (!canAccessAnalysis(analysis, currentUser)) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied"));
            }

            return ResponseEntity.ok(Map.of(
                "id", analysis.getId(),
                "status", analysis.getStatus().name(),
                "realityScore", analysis.getRealityScore(),
                "botPercentage", analysis.getBotPercentage(),
                "manipulationLevel", analysis.getManipulationLevel(),
                "processingTimeMs", analysis.getProcessingTimeMs(),
                "completedAt", analysis.getCompletedAt(),
                "errorMessage", analysis.getErrorMessage()
            ));

        } catch (Exception e) {
            log.error("Failed to get analysis status: {}", id, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve status"));
        }
    }

    /**
     * Get system analytics
     * GET /api/analysis/analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        try {
            long totalAnalyses = analysisRepository.count();
            long completedAnalyses = analysisRepository.countByStatus(AnalysisStatus.COMPLETE);
            long manipulatedProducts = analysisRepository.countByBotPercentageGreaterThan(
                java.math.BigDecimal.valueOf(60));
            
            List<Analysis> recentAnalyses = analysisRepository
                .findTop10ByStatusOrderByCreatedAtDesc(AnalysisStatus.COMPLETE);

            return ResponseEntity.ok(Map.of(
                "totalAnalyses", totalAnalyses,
                "completedAnalyses", completedAnalyses,
                "manipulatedProducts", manipulatedProducts,
                "completionRate", completedAnalyses * 100.0 / Math.max(1, totalAnalyses),
                "recentAnalyses", recentAnalyses.size(),
                "averageBotPercentage", calculateAverageBotPercentage(),
                "averageRealityScore", calculateAverageRealityScore()
            ));

        } catch (Exception e) {
            log.error("Failed to get analytics", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve analytics"));
        }
    }

    /**
     * Cancel analysis
     * DELETE /api/analysis/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAnalysis(@PathVariable UUID id) {
        try {
            Analysis analysis = analysisRepository.findById(id).orElse(null);
            if (analysis == null) {
                return ResponseEntity.notFound().build();
            }

            User currentUser = getCurrentUser();
            if (!analysis.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied"));
            }

            if (analysis.getStatus() == AnalysisStatus.COMPLETE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot cancel completed analysis"));
            }

            // Update status to cancelled
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setErrorMessage("Cancelled by user");
            analysisRepository.save(analysis);

            log.info("Analysis cancelled: {} by user: {}", id, currentUser.getEmail());

            return ResponseEntity.ok(Map.of(
                "message", "Analysis cancelled successfully",
                "analysisId", id
            ));

        } catch (Exception e) {
            log.error("Failed to cancel analysis: {}", id, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to cancel analysis"));
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

    private boolean canAccessAnalysis(Analysis analysis, User user) {
        if (analysis.getIsPublic()) {
            return true;
        }
        
        if (user == null) {
            return false;
        }

        return analysis.getUserId().equals(user.getId());
    }

    private Double calculateAverageBotPercentage() {
        try {
            return analysisRepository.findAverageBotPercentage();
        } catch (Exception e) {
            log.error("Failed to calculate average bot percentage", e);
            return 0.0;
        }
    }

    private Double calculateAverageRealityScore() {
        try {
            return analysisRepository.findAverageRealityScore();
        } catch (Exception e) {
            log.error("Failed to calculate average reality score", e);
            return 0.0;
        }
    }
}
