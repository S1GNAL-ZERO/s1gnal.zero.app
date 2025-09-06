package io.signalzero.model;

/**
 * Subscription tier enumeration
 * Reference: DETAILED_DESIGN.md Section 9 - Monetization Model
 */
public enum SubscriptionTier {
    PUBLIC("Public", 0, "View Only"),
    FREE("Free", 3, "Basic Reality Score"),
    PRO("Pro", 100, "Detailed reports + API"),
    BUSINESS("Business", 1000, "Team seats + priority"),
    ENTERPRISE("Enterprise", -1, "Unlimited + white-label");
    
    private final String displayName;
    private final int monthlyAnalyses;
    private final String features;
    
    SubscriptionTier(String displayName, int monthlyAnalyses, String features) {
        this.displayName = displayName;
        this.monthlyAnalyses = monthlyAnalyses;
        this.features = features;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getMonthlyAnalyses() {
        return monthlyAnalyses;
    }
    
    public String getFeatures() {
        return features;
    }
    
    public boolean hasUnlimitedAnalyses() {
        return monthlyAnalyses == -1;
    }
    
    public boolean canPerformAnalysis() {
        return monthlyAnalyses > 0 || hasUnlimitedAnalyses();
    }
    
    public boolean isPublicViewOnly() {
        return this == PUBLIC;
    }
    
    public boolean isPaidTier() {
        return this == PRO || this == BUSINESS || this == ENTERPRISE;
    }
}
