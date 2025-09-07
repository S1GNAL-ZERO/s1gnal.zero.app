package io.signalzero.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity for managing user's datasource API keys
 * Maps to the api_keys table but focused on datasource management
 */
@Entity
@Table(name = "api_keys", schema = "signalzero")
public class DataSourceKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(name = "key_hash", nullable = false)
    private String keyHash;
    
    @Column(name = "key_prefix", nullable = false, length = 10)
    private String keyPrefix;
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    /**
     * JSON array of scopes/permissions for this key
     * For datasource keys, this will contain the service name
     * e.g., ["youtube_api", "reddit_api", "twitter_api"]
     */
    @Column(name = "scopes", columnDefinition = "jsonb")
    @Convert(converter = JsonbListConverter.class)
    private List<String> scopes;
    
    @Column(name = "rate_limit")
    private Integer rateLimit = 100;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    // Transient fields for UI display
    @Transient
    private String serviceName; // youtube, reddit, twitter, etc.
    
    @Transient
    private String displayKey; // Masked key for display (e.g., "AIza****")
    
    // Constructors
    public DataSourceKey() {
        this.createdAt = LocalDateTime.now();
    }
    
    public DataSourceKey(UUID userId, String serviceName, String apiKey, String description) {
        this();
        this.userId = userId;
        this.serviceName = serviceName;
        this.name = serviceName.toUpperCase() + "_API_KEY";
        this.description = description;
        this.scopes = List.of(serviceName + "_api");
        
        // Store hash and prefix
        this.keyHash = hashKey(apiKey);
        this.keyPrefix = apiKey.length() >= 10 ? apiKey.substring(0, 10) : apiKey;
        this.displayKey = maskKey(apiKey);
    }
    
    // Constructor with User entity (for DataSourceService compatibility)
    public DataSourceKey(User user, String serviceName, String apiKey, String description) {
        this(user.getId(), serviceName, apiKey, description);
        this.user = user;
    }
    
    // Utility methods
    private String hashKey(String key) {
        // In production, use proper hashing like BCrypt
        // For demo/hackathon, simple approach
        return String.valueOf(key.hashCode());
    }
    
    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + (key.length() > 8 ? key.substring(key.length() - 4) : "");
    }
    
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isRevoked() {
        return revokedAt != null;
    }
    
    public boolean isValid() {
        return isActive && !isExpired() && !isRevoked();
    }
    
    /**
     * Get the actual API key value - NOTE: This is not stored, only hash is stored
     * This method returns null since we don't store the actual key for security
     * In production, this would require key vault integration
     */
    public String getKeyValue() {
        // For demo/hackathon: return null since we only store hash
        // In production: integrate with key vault service
        return null;
    }
    
    /**
     * Update the API key with a new value
     * Updates both hash and prefix
     */
    public void updateKey(String newApiKey) {
        if (newApiKey != null && !newApiKey.trim().isEmpty()) {
            this.keyHash = hashKey(newApiKey);
            this.keyPrefix = newApiKey.length() >= 10 ? newApiKey.substring(0, 10) : newApiKey;
            this.displayKey = maskKey(newApiKey);
            this.lastUsedAt = null; // Reset usage tracking
            this.usageCount = 0;
        }
    }
    
    /**
     * Increment usage count and update last used timestamp
     */
    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    /**
     * Revoke this API key
     */
    public void revoke() {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getKeyHash() {
        return keyHash;
    }
    
    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }
    
    public String getKeyPrefix() {
        return keyPrefix;
    }
    
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getScopes() {
        return scopes;
    }
    
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
    
    public Integer getRateLimit() {
        return rateLimit;
    }
    
    public void setRateLimit(Integer rateLimit) {
        this.rateLimit = rateLimit;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getDisplayKey() {
        return displayKey;
    }
    
    public void setDisplayKey(String displayKey) {
        this.displayKey = displayKey;
    }
}

/**
 * JPA converter for JSONB list fields
 */
@Converter
class JsonbListConverter implements AttributeConverter<List<String>, String> {
    
    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(list.get(i)).append("\"");
        }
        json.append("]");
        return json.toString();
    }
    
    @Override
    public List<String> convertToEntityAttribute(String json) {
        if (json == null || json.trim().equals("[]")) {
            return List.of();
        }
        
        // Simple JSON parsing for demo - in production use Jackson
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1);
            String[] items = json.split(",");
            return List.of(items).stream()
                .map(s -> s.trim().replaceAll("\"", ""))
                .toList();
        }
        
        return List.of();
    }
}
