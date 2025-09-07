package io.signalzero.repository;

import io.signalzero.model.DataSourceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing user datasource API keys
 */
@Repository
public interface DataSourceKeyRepository extends JpaRepository<DataSourceKey, UUID> {
    
    /**
     * Find all active keys for a specific user
     */
    List<DataSourceKey> findByUserIdAndIsActiveTrue(UUID userId);
    
    /**
     * Find all keys for a specific user (including inactive)
     */
    List<DataSourceKey> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find all keys for a specific user (simple method for DataSourceService)
     */
    List<DataSourceKey> findByUserId(UUID userId);
    
    /**
     * Find a specific key by user and service name (from scopes)
     */
    @Query(value = "SELECT * FROM signalzero.api_keys d WHERE d.user_id = :userId AND d.is_active = true AND d.revoked_at IS NULL " +
           "AND d.scopes::text LIKE CONCAT('%', :serviceName, '%')", nativeQuery = true)
    Optional<DataSourceKey> findByUserIdAndServiceName(@Param("userId") UUID userId, @Param("serviceName") String serviceName);
    
    /**
     * Find key by hash - used for validation
     */
    Optional<DataSourceKey> findByKeyHashAndIsActiveTrue(String keyHash);
    
    /**
     * Find keys that are about to expire (within 7 days)
     */
    @Query(value = "SELECT * FROM signalzero.api_keys d WHERE d.user_id = :userId AND d.is_active = true " +
           "AND d.expires_at IS NOT NULL AND d.expires_at <= CURRENT_TIMESTAMP + INTERVAL '7 days'", nativeQuery = true)
    List<DataSourceKey> findExpiringKeysForUser(@Param("userId") UUID userId);
    
    /**
     * Count active keys for a user (for limits)
     */
    long countByUserIdAndIsActiveTrue(UUID userId);
    
    /**
     * Find all keys for a service across all users (admin function)
     */
    @Query(value = "SELECT * FROM signalzero.api_keys d WHERE d.scopes::text LIKE CONCAT('%', :serviceName, '%') AND d.is_active = true", nativeQuery = true)
    List<DataSourceKey> findByServiceName(@Param("serviceName") String serviceName);
    
    /**
     * Update usage statistics
     */
    @Query("UPDATE DataSourceKey d SET d.usageCount = d.usageCount + 1, d.lastUsedAt = CURRENT_TIMESTAMP WHERE d.id = :keyId")
    void incrementUsage(@Param("keyId") UUID keyId);
    
    /**
     * Revoke a key (soft delete)
     */
    @Query("UPDATE DataSourceKey d SET d.isActive = false, d.revokedAt = CURRENT_TIMESTAMP WHERE d.id = :keyId AND d.userId = :userId")
    void revokeKey(@Param("keyId") UUID keyId, @Param("userId") UUID userId);
    
    /**
     * Check if user has a valid key for a specific service
     */
    @Query(value = "SELECT CASE WHEN COUNT(d.*) > 0 THEN true ELSE false END FROM signalzero.api_keys d " +
           "WHERE d.user_id = :userId AND d.is_active = true AND d.revoked_at IS NULL " +
           "AND (d.expires_at IS NULL OR d.expires_at > CURRENT_TIMESTAMP) " +
           "AND d.scopes::text LIKE CONCAT('%', :serviceName, '%')", nativeQuery = true)
    boolean hasValidKeyForService(@Param("userId") UUID userId, @Param("serviceName") String serviceName);
}
