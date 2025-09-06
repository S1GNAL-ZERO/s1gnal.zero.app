package io.signalzero.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Wall of Shame entity for featured manipulated products/trends
 * Reference: DETAILED_DESIGN.md Section 6.1.4 - Wall of Shame Table
 */
@Entity
@Table(name = "wall_of_shame", indexes = {
    @Index(name = "idx_wall_of_shame_active", columnList = "isActive"),
    @Index(name = "idx_wall_of_shame_bot_percentage", columnList = "botPercentage DESC"),
    @Index(name = "idx_wall_of_shame_created_at", columnList = "createdAt DESC")
})
public class WallOfShame {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotNull
    @Column(name = "analysis_id", nullable = false)
    private UUID analysisId;
    
    // Display information
    @NotBlank
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "category", length = 100)
    private String category;
    
    // Metrics
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "bot_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal botPercentage;
    
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "reality_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal realityScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "manipulation_level", nullable = false, length = 20)
    private ManipulationLevel manipulationLevel;
    
    // Evidence summary
    @Column(name = "evidence_summary", columnDefinition = "TEXT")
    private String evidenceSummary;
    
    @Column(name = "key_findings", columnDefinition = "jsonb")
    private String keyFindings; // JSON array as string for compatibility
    
    // Engagement metrics
    @Column(name = "views")
    private Integer views = 0;
    
    @Column(name = "shares")
    private Integer shares = 0;
    
    @Column(name = "reports")
    private Integer reports = 0;
    
    // Display control
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Analysis analysis;
    
    // Default constructor
    public WallOfShame() {}
    
    // Constructor from analysis
    public WallOfShame(Analysis analysis) {
        this.analysisId = analysis.getId();
        this.productName = analysis.getQuery();
        this.botPercentage = analysis.getBotPercentage();
        this.realityScore = analysis.getRealityScore();
        this.manipulationLevel = analysis.getManipulationLevel();
        this.category = analysis.getQueryType();
        this.isActive = true;
        
        // Generate evidence summary
        this.evidenceSummary = String.format("Coordinated inauthentic activity detected: %s%% bot accounts identified", 
                                           botPercentage.intValue());
        
        // Set display order based on bot percentage (higher = more prominent)
        this.displayOrder = botPercentage.intValue();
    }
    
    // Business logic methods
    public boolean isFeatured() {
        return isActive && (featuredUntil == null || featuredUntil.isAfter(LocalDateTime.now()));
    }
    
    public void incrementViews() {
        this.views = (this.views != null ? this.views : 0) + 1;
    }
    
    public void incrementShares() {
        this.shares = (this.shares != null ? this.shares : 0) + 1;
    }
    
    public void incrementReports() {
        this.reports = (this.reports != null ? this.reports : 0) + 1;
    }
    
    public boolean isHighlyReported() {
        return reports != null && reports >= 10;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void extendFeature(int hours) {
        if (this.featuredUntil == null) {
            this.featuredUntil = LocalDateTime.now().plusHours(hours);
        } else {
            this.featuredUntil = this.featuredUntil.plusHours(hours);
        }
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAnalysisId() {
        return analysisId;
    }
    
    public void setAnalysisId(UUID analysisId) {
        this.analysisId = analysisId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public BigDecimal getBotPercentage() {
        return botPercentage;
    }
    
    public void setBotPercentage(BigDecimal botPercentage) {
        this.botPercentage = botPercentage;
    }
    
    public BigDecimal getRealityScore() {
        return realityScore;
    }
    
    public void setRealityScore(BigDecimal realityScore) {
        this.realityScore = realityScore;
    }
    
    public ManipulationLevel getManipulationLevel() {
        return manipulationLevel;
    }
    
    public void setManipulationLevel(ManipulationLevel manipulationLevel) {
        this.manipulationLevel = manipulationLevel;
    }
    
    public String getEvidenceSummary() {
        return evidenceSummary;
    }
    
    public void setEvidenceSummary(String evidenceSummary) {
        this.evidenceSummary = evidenceSummary;
    }
    
    public String getKeyFindings() {
        return keyFindings;
    }
    
    public void setKeyFindings(String keyFindings) {
        this.keyFindings = keyFindings;
    }
    
    public Integer getViews() {
        return views;
    }
    
    public void setViews(Integer views) {
        this.views = views;
    }
    
    public Integer getShares() {
        return shares;
    }
    
    public void setShares(Integer shares) {
        this.shares = shares;
    }
    
    public Integer getReports() {
        return reports;
    }
    
    public void setReports(Integer reports) {
        this.reports = reports;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getFeaturedUntil() {
        return featuredUntil;
    }
    
    public void setFeaturedUntil(LocalDateTime featuredUntil) {
        this.featuredUntil = featuredUntil;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Analysis getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }
    
    @Override
    public String toString() {
        return String.format("WallOfShame{id=%s, productName='%s', botPercentage=%s, realityScore=%s}", 
                           id, productName, botPercentage, realityScore);
    }
}
