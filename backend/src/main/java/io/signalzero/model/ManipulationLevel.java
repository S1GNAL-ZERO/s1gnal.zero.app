package io.signalzero.model;

/**
 * Manipulation level classification for analyses
 * Reference: DETAILED_DESIGN.md Section 6.1.2
 * 
 * - GREEN: 67-100% Reality Score (Authentic engagement)
 * - YELLOW: 34-66% Reality Score (Mixed signals)
 * - RED: 0-33% Reality Score (Heavily manipulated)
 */
public enum ManipulationLevel {
    GREEN("Authentic", "67-100% Reality Score - Authentic engagement"),
    YELLOW("Mixed Signals", "34-66% Reality Score - Mixed signals"), 
    RED("Heavily Manipulated", "0-33% Reality Score - Heavily manipulated");
    
    private final String displayName;
    private final String description;
    
    ManipulationLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isAuthentic() {
        return this == GREEN;
    }
    
    public boolean isManipulated() {
        return this == RED;
    }
    
    public boolean isMixed() {
        return this == YELLOW;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
