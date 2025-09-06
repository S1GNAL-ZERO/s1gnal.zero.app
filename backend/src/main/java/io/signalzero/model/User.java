package io.signalzero.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity matching database schema
 * Reference: DETAILED_DESIGN.md Section 6.1.1 - Users Table
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_stripe_customer", columnList = "stripeCustomerId"),
    @Index(name = "idx_users_referral_code", columnList = "referralCode"),
    @Index(name = "idx_users_subscription_tier", columnList = "subscriptionTier")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotBlank
    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Size(min = 8)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "full_name")
    private String fullName;
    
    // Subscription management
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", nullable = false)
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;
    
    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;
    
    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;
    
    @Column(name = "stripe_customer_id", unique = true)
    private String stripeCustomerId;
    
    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;
    
    // Usage tracking
    @Column(name = "analyses_used_this_month", nullable = false)
    private Integer analysesUsedThisMonth = 0;
    
    @Column(name = "analyses_used_total", nullable = false)
    private Integer analysesUsedTotal = 0;
    
    @Column(name = "last_usage_reset")
    private LocalDateTime lastUsageReset = LocalDateTime.now();
    
    // Referral system
    @Column(name = "referral_code", unique = true, length = 20)
    private String referralCode;
    
    @Column(name = "referred_by")
    private UUID referredBy;
    
    @Column(name = "referral_count", nullable = false)
    private Integer referralCount = 0;
    
    // Account status
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "verification_token")
    private String verificationToken;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    // Default constructor
    public User() {}
    
    // Constructor for registration
    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.subscriptionTier = SubscriptionTier.FREE;
        this.isActive = true;
        this.isVerified = false;
        this.analysesUsedThisMonth = 0;
        this.analysesUsedTotal = 0;
        this.referralCount = 0;
        this.lastUsageReset = LocalDateTime.now();
    }
    
    // Business logic methods
    public boolean canPerformAnalysis() {
        if (!isActive) return false;
        if (subscriptionTier.hasUnlimitedAnalyses()) return true;
        return analysesUsedThisMonth < subscriptionTier.getMonthlyAnalyses();
    }
    
    public void incrementAnalysisUsage() {
        this.analysesUsedThisMonth++;
        this.analysesUsedTotal++;
    }
    
    public void resetMonthlyUsage() {
        this.analysesUsedThisMonth = 0;
        this.lastUsageReset = LocalDateTime.now();
    }
    
    public int getRemainingAnalyses() {
        if (subscriptionTier.hasUnlimitedAnalyses()) return -1;
        return Math.max(0, subscriptionTier.getMonthlyAnalyses() - analysesUsedThisMonth);
    }
    
    public boolean isPaidSubscriber() {
        return subscriptionTier.isPaidTier();
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public SubscriptionTier getSubscriptionTier() {
        return subscriptionTier;
    }
    
    public void setSubscriptionTier(SubscriptionTier subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }
    
    public LocalDateTime getSubscriptionStartDate() {
        return subscriptionStartDate;
    }
    
    public void setSubscriptionStartDate(LocalDateTime subscriptionStartDate) {
        this.subscriptionStartDate = subscriptionStartDate;
    }
    
    public LocalDateTime getSubscriptionEndDate() {
        return subscriptionEndDate;
    }
    
    public void setSubscriptionEndDate(LocalDateTime subscriptionEndDate) {
        this.subscriptionEndDate = subscriptionEndDate;
    }
    
    public String getStripeCustomerId() {
        return stripeCustomerId;
    }
    
    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }
    
    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }
    
    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }
    
    public Integer getAnalysesUsedThisMonth() {
        return analysesUsedThisMonth;
    }
    
    public void setAnalysesUsedThisMonth(Integer analysesUsedThisMonth) {
        this.analysesUsedThisMonth = analysesUsedThisMonth;
    }
    
    public Integer getAnalysesUsedTotal() {
        return analysesUsedTotal;
    }
    
    public void setAnalysesUsedTotal(Integer analysesUsedTotal) {
        this.analysesUsedTotal = analysesUsedTotal;
    }
    
    public LocalDateTime getLastUsageReset() {
        return lastUsageReset;
    }
    
    public void setLastUsageReset(LocalDateTime lastUsageReset) {
        this.lastUsageReset = lastUsageReset;
    }
    
    public String getReferralCode() {
        return referralCode;
    }
    
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
    
    public UUID getReferredBy() {
        return referredBy;
    }
    
    public void setReferredBy(UUID referredBy) {
        this.referredBy = referredBy;
    }
    
    public Integer getReferralCount() {
        return referralCount;
    }
    
    public void setReferralCount(Integer referralCount) {
        this.referralCount = referralCount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public String getVerificationToken() {
        return verificationToken;
    }
    
    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%s, email='%s', subscriptionTier=%s, analysesUsed=%d}", 
                           id, email, subscriptionTier, analysesUsedThisMonth);
    }
}
