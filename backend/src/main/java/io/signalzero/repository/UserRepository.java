package io.signalzero.repository;

import io.signalzero.model.SubscriptionTier;
import io.signalzero.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity - Direct JPA operations
 * Reference: DETAILED_DESIGN.md Section 8 - Repository Pattern
 * No DTOs - work directly with User entities
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Authentication queries
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    boolean existsByEmail(String email);
    
    // Subscription management queries
    List<User> findBySubscriptionTier(SubscriptionTier tier);
    
    List<User> findBySubscriptionTierNot(SubscriptionTier tier);
    
    @Query("SELECT u FROM User u WHERE u.subscriptionEndDate < :date AND u.subscriptionTier != 'FREE'")
    List<User> findExpiredSubscriptions(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.subscriptionEndDate BETWEEN :start AND :end")
    List<User> findExpiringSubscriptions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Usage tracking queries
    @Query("SELECT u FROM User u WHERE u.analysesUsedThisMonth >= :limit")
    List<User> findUsersAtLimit(@Param("limit") int limit);
    
    @Query("SELECT u FROM User u WHERE u.lastUsageReset < :resetDate")
    List<User> findUsersNeedingUsageReset(@Param("resetDate") LocalDateTime resetDate);
    
    @Modifying
    @Query("UPDATE User u SET u.analysesUsedThisMonth = 0, u.lastUsageReset = :resetDate WHERE u.lastUsageReset < :resetDate")
    int resetMonthlyUsageForUsers(@Param("resetDate") LocalDateTime resetDate);
    
    // Referral system queries
    Optional<User> findByReferralCode(String referralCode);
    
    List<User> findByReferredBy(UUID referrerId);
    
    @Query("SELECT u FROM User u WHERE u.referralCount > 0 ORDER BY u.referralCount DESC")
    List<User> findTopReferrers();
    
    // Account management queries
    List<User> findByIsActiveFalse();
    
    List<User> findByIsVerifiedFalse();
    
    Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByStripeCustomerId(String stripeCustomerId);
    
    // Analytics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countNewUsersAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.subscriptionTier != 'FREE'")
    long countPaidUsers();
    
    @Query("SELECT u.subscriptionTier, COUNT(u) FROM User u GROUP BY u.subscriptionTier")
    List<Object[]> countUsersByTier();
    
    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u WHERE u.createdAt >= :startDate GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserSignupsByDay(@Param("startDate") LocalDateTime startDate);
    
    // Business logic helpers
    @Query("SELECT CASE WHEN u.analysesUsedThisMonth < :limit THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean canUserAnalyze(@Param("userId") UUID userId, @Param("limit") int limit);
    
    @Modifying
    @Query("UPDATE User u SET u.analysesUsedThisMonth = u.analysesUsedThisMonth + 1, u.analysesUsedTotal = u.analysesUsedTotal + 1 WHERE u.id = :userId")
    int incrementUserUsage(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    int updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);
    
    @Modifying
    @Query("UPDATE User u SET u.referralCount = u.referralCount + 1 WHERE u.id = :userId")
    int incrementReferralCount(@Param("userId") UUID userId);
}
