package io.signalzero.model;

/**
 * Analysis status enumeration
 * Reference: DETAILED_DESIGN.md Section 6.1.2 - Analysis processing states
 */
public enum AnalysisStatus {
    PENDING("Pending", "Analysis request received"),
    PROCESSING("Processing", "Agents are analyzing"),
    COMPLETE("Complete", "Analysis finished successfully"),
    FAILED("Failed", "Analysis failed due to error"),
    TIMEOUT("Timeout", "Analysis exceeded time limit");
    
    private final String displayName;
    private final String description;
    
    AnalysisStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompleted() {
        return this == COMPLETE;
    }
    
    public boolean isFailed() {
        return this == FAILED || this == TIMEOUT;
    }
    
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING;
    }
    
    public boolean canBeRetried() {
        return this == FAILED || this == TIMEOUT;
    }
}
