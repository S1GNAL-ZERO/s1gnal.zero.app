package io.signalzero.service;

import io.signalzero.model.SubscriptionTier;
import io.signalzero.model.User;
import io.signalzero.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Usage Tracking Service - Repository Pattern
 * Reference: DETAILED_DESIGN.md Section 9.3 - Usage Tracking
 * No DTOs - work directly with User entities
 */
@Service
@Transactional
public class UsageTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(UsageTrackingService.class);
    
    // Tier limits from DETAILED_DESIGN.md Section 9
    private static final Map<SubscriptionTier, Integer> TIER_LIMITS = new HashMap<>();
    
    static {
        TIER_LIMITS.put(SubscriptionTier.FREE, 3);
        TIER_LIMITS.put(SubscriptionTier.PRO, 100);
        TIER_LIMITS.put(SubscriptionTier.BUSINESS, 1000);
        TIER_LIMITS.put(SubscriptionTier.ENTERPRISE, Integer.MAX_VALUE); // Unlimited
    }
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Check if user can perform analysis based on their tier and current usage
     */
    public boolean canUserAnalyze(User user) {
        if (user == null || !user.getIsActive()) {
            return false;
        }
        
        // Check if usage needs to be reset (new month)
        resetUsageIfNeeded(user);
        
        // Get current tier limit
        int limit = getTierLimit(user.getSubscriptionTier());
        
        // Check current usage
        boolean canAnalyze = user.getAnalysesUsedThisMonth() < limit;
        
        logger.debug("User {} can analyze: {} (used: {}/{}, tier: {})", 
                    user.getId(), canAnalyze, user.getAnalysesUsedThisMonth(), 
                    limit, user.getSubscriptionTier());
        
        return canAnalyze;
    }
    
    /**
     * Increment user's usage count
     */
    @Transactional
    public void incrementUserUsage(User user) {
        if (user == null) {
            logger.warn("Cannot increment usage for null user");
            return;
        }
        
        // Reset usage if needed
        resetUsageIfNeeded(user);
        
        // Use repository method to increment atomically
        int updatedRows = userRepository.incrementUserUsage(user.getId());
        
        if (updatedRows > 0) {
            // Refresh entity state
            user.setAnalysesUsedThisMonth(user.getAnalysesUsedThisMonth() + 1);
            user.setAnalysesUsedTotal(user.getAnalysesUsedTotal() + 1);
            
            logger.info("Incremented usage for user {} to {}/{} (total: {})", 
                       user.getId(), user.getAnalysesUsedThisMonth(), 
                       getTierLimit(user.getSubscriptionTier()), user.getAnalysesUsedTotal());
        } else {
            logger.warn("Failed to increment usage for user {}", user.getId());
        }
    }
    
    /**
     * Get usage statistics for user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserUsageStats(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new HashMap<>();
        }
        
        int limit = getTierLimit(user.getSubscriptionTier());
        int remaining = Math.max(0, limit - user.getAnalysesUsedThisMonth());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("tier", user.getSubscriptionTier().name());
        stats.put("usedThisMonth", user.getAnalysesUsedThisMonth());
        stats.put("limit", limit);
        stats.put("remaining", remaining);
        stats.put("totalUsed", user.getAnalysesUsedTotal());
        stats.put("percentUsed", limit > 0 ? (user.getAnalysesUsedThisMonth() * 100.0) / limit : 0);
        stats.put("canAnalyze", canUserAnalyze(user));
        stats.put("resetDate", user.getLastUsageReset());
        
        return stats;
    }
    
    /**
     * Check if user needs usage reset (new month)
     */
    private void resetUsageIfNeeded(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = user.getLastUsageReset();
        
        // Reset if:
        // 1. Never reset before (new user)
        // 2. Last reset was previous month
        boolean shouldReset = lastReset == null || 
                             lastReset.getMonth() != now.getMonth() ||
                             lastReset.getYear() != now.getYear();
        
        if (shouldReset) {
            user.setAnalysesUsedThisMonth(0);
            user.setLastUsageReset(now);
            userRepository.save(user);
            
            logger.info("Reset monthly usage for user {} (tier: {})", 
                       user.getId(), user.getSubscriptionTier());
        }
    }
    
    /**
     * Get tier limit for subscription tier
     */
    private int getTierLimit(SubscriptionTier tier) {
        return TIER_LIMITS.getOrDefault(tier, TIER_LIMITS.get(SubscriptionTier.FREE));
    }
    
    /**
     * Check if user is at or near their limit
     */
    public boolean isUserNearLimit(User user, double threshold) {
        if (user == null) return false;
        
        int limit = getTierLimit(user.getSubscriptionTier());
        if (limit == Integer.MAX_VALUE) return false; // Unlimited
        
        double usage = (double) user.getAnalysesUsedThisMonth() / limit;
        return usage >= threshold;
    }
    
    /**
     * Get users who are at their limits
     */
    @Transactional(readOnly = true)
    public long countUsersAtLimit() {
        long count = 0;
        for (Map.Entry<SubscriptionTier, Integer> entry : TIER_LIMITS.entrySet()) {
            if (entry.getValue() < Integer.MAX_VALUE) {
                count += userRepository.findUsersAtLimit(entry.getValue()).size();
            }
        }
        return count;
    }
    
    /**
     * Upgrade user subscription tier
     */
    @Transactional
    public boolean upgradeUserTier(UUID userId, SubscriptionTier newTier) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("Cannot upgrade tier for non-existent user: {}", userId);
            return false;
        }
        
        SubscriptionTier currentTier = user.getSubscriptionTier();
        if (isUpgrade(currentTier, newTier)) {
            user.setSubscriptionTier(newTier);
            user.setSubscriptionStartDate(LocalDateTime.now());
            
            // Set end date based on tier (simplified for hackathon)
            if (newTier != SubscriptionTier.FREE) {
                user.setSubscriptionEndDate(LocalDateTime.now().plusDays(30)); // 30-day trial
            }
            
            userRepository.save(user);
            
            logger.info("Upgraded user {} from {} to {}", userId, currentTier, newTier);
            return true;
        } else {
            logger.warn("Attempted downgrade or invalid upgrade for user {}: {} to {}", 
                       userId, currentTier, newTier);
            return false;
        }
    }
    
    /**
     * Check if new tier is an upgrade
     */
    private boolean isUpgrade(SubscriptionTier current, SubscriptionTier proposed) {
        int currentLimit = getTierLimit(current);
        int proposedLimit = getTierLimit(proposed);
        return proposedLimit > currentLimit;
    }
    
    /**
     * Get monthly analysis limit for a subscription tier
     */
    public int getMonthlyLimit(SubscriptionTier tier) {
        if (tier == null) {
            return 3; // Default to FREE
        }
        return switch (tier) {
            case PUBLIC -> 0; // View only
            case FREE -> 3;
            case PRO -> 100;
            case BUSINESS -> 1000;
            case ENTERPRISE -> -1; // Unlimited
        };
    }

    /**
     * Upgrade user to new subscription tier
     */
    @Transactional
    public void upgradeUser(UUID userId, SubscriptionTier newTier) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setSubscriptionTier(newTier);
        user.setAnalysesUsedThisMonth(0); // Reset usage on upgrade
        userRepository.save(user);
        
        logger.info("Upgraded user {} to tier {}", userId, newTier);
    }
}
