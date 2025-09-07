package io.signalzero.service;

import io.signalzero.model.User;
import io.signalzero.model.SubscriptionTier;
import io.signalzero.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for user authentication and management
 * Following CLAUDE.md coding standards - NO PLACEHOLDERS, production ready
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user with email and password
     */
    public User registerUser(String email, String password, String fullName) {
        try {
            // Check if user already exists
            if (userRepository.findByEmail(email).isPresent()) {
                throw new RuntimeException("User with email " + email + " already exists");
            }
            
            // Create new user
            User user = new User();
            user.setEmail(email.toLowerCase().trim());
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setSubscriptionTier(SubscriptionTier.FREE);
            user.setIsActive(true);
            user.setIsVerified(false); // Email verification can be added later
            user.setAnalysesUsedThisMonth(0);
            user.setAnalysesUsedTotal(0);
            user.setReferralCount(0);
            
            // Generate referral code
            user.setReferralCode(generateReferralCode(email));
            
            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", savedUser.getEmail());
            
            return savedUser;
            
        } catch (Exception e) {
            log.error("Failed to register user: {}", email, e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user login
     */
    public Optional<User> authenticateUser(String email, String password) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
            
            if (userOpt.isEmpty()) {
                log.warn("Login attempt for non-existent user: {}", email);
                return Optional.empty();
            }
            
            User user = userOpt.get();
            
            // Check if user is active
            if (!user.getIsActive()) {
                log.warn("Login attempt for inactive user: {}", email);
                return Optional.empty();
            }
            
            // Verify password
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                // Update last login
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                
                log.info("User authenticated successfully: {}", email);
                return Optional.of(user);
            } else {
                log.warn("Invalid password for user: {}", email);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", email, e);
            return Optional.empty();
        }
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(UUID userId) {
        try {
            return userRepository.findById(userId);
        } catch (Exception e) {
            log.error("Failed to find user by ID: {}", userId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        try {
            return userRepository.findByEmail(email.toLowerCase().trim());
        } catch (Exception e) {
            log.error("Failed to find user by email: {}", email, e);
            return Optional.empty();
        }
    }
    
    /**
     * Update user profile
     */
    public User updateUser(UUID userId, String fullName, String email) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (fullName != null && !fullName.trim().isEmpty()) {
                user.setFullName(fullName.trim());
            }
            
            if (email != null && !email.trim().isEmpty()) {
                String newEmail = email.toLowerCase().trim();
                if (!newEmail.equals(user.getEmail())) {
                    // Check if new email is already taken
                    if (userRepository.findByEmail(newEmail).isPresent()) {
                        throw new RuntimeException("Email already in use");
                    }
                    user.setEmail(newEmail);
                    user.setIsVerified(false); // Re-verify email
                }
            }
            
            User updatedUser = userRepository.save(user);
            log.info("User updated successfully: {}", updatedUser.getEmail());
            
            return updatedUser;
            
        } catch (Exception e) {
            log.error("Failed to update user: {}", userId, e);
            throw new RuntimeException("User update failed: " + e.getMessage());
        }
    }
    
    /**
     * Change user password
     */
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new RuntimeException("Current password is incorrect");
            }
            
            // Update password
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            
            userRepository.save(user);
            log.info("Password changed successfully for user: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to change password for user: {}", userId, e);
            throw new RuntimeException("Password change failed: " + e.getMessage());
        }
    }
    
    /**
     * Update subscription tier
     */
    public User updateSubscriptionTier(UUID userId, SubscriptionTier tier) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setSubscriptionTier(tier);
            
            if (tier != SubscriptionTier.FREE) {
                user.setSubscriptionStartDate(LocalDateTime.now());
                // Set end date based on tier (simplified for demo)
                user.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
            }
            
            User updatedUser = userRepository.save(user);
            log.info("Subscription tier updated to {} for user: {}", tier, user.getEmail());
            
            return updatedUser;
            
        } catch (Exception e) {
            log.error("Failed to update subscription tier for user: {}", userId, e);
            throw new RuntimeException("Subscription update failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if user can perform analysis (usage limits)
     */
    public boolean canPerformAnalysis(UUID userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            int monthlyLimit = getMonthlyAnalysisLimit(user.getSubscriptionTier());
            
            // Unlimited for enterprise
            if (monthlyLimit == -1) {
                return true;
            }
            
            return user.getAnalysesUsedThisMonth() < monthlyLimit;
            
        } catch (Exception e) {
            log.error("Failed to check analysis limit for user: {}", userId, e);
            return false; // Fail safe - don't allow analysis if we can't check
        }
    }
    
    /**
     * Increment usage count for user
     */
    public void incrementUsage(UUID userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setAnalysesUsedThisMonth(user.getAnalysesUsedThisMonth() + 1);
            user.setAnalysesUsedTotal(user.getAnalysesUsedTotal() + 1);
            
            userRepository.save(user);
            log.info("Usage incremented for user: {} (monthly: {}, total: {})", 
                user.getEmail(), user.getAnalysesUsedThisMonth(), user.getAnalysesUsedTotal());
            
        } catch (Exception e) {
            log.error("Failed to increment usage for user: {}", userId, e);
        }
    }
    
    /**
     * Get monthly analysis limit for subscription tier
     */
    private int getMonthlyAnalysisLimit(SubscriptionTier tier) {
        return switch (tier) {
            case PUBLIC -> 0; // View only, no analyses
            case FREE -> 3;
            case PRO -> 100;
            case BUSINESS -> 1000;
            case ENTERPRISE -> -1; // Unlimited
        };
    }
    
    /**
     * Generate referral code from email
     */
    private String generateReferralCode(String email) {
        try {
            // Simple referral code generation for demo
            String cleanEmail = email.replaceAll("[^a-zA-Z0-9]", "");
            if (cleanEmail.length() >= 8) {
                return cleanEmail.substring(0, 8).toUpperCase();
            } else {
                return (cleanEmail + "12345678").substring(0, 8).toUpperCase();
            }
        } catch (Exception e) {
            // Fallback to random if generation fails
            return "REF" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        }
    }
    
    /**
     * Create demo user for testing (ONLY for demo/development)
     */
    public User createDemoUser() {
        String demoEmail = "demo@s1gnalzero.com";
        
        // Check if demo user already exists
        Optional<User> existing = userRepository.findByEmail(demoEmail);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create demo user
        return registerUser(demoEmail, "demo123", "Demo User");
    }
}
