package io.signalzero.controller;

import io.signalzero.model.SubscriptionTier;
import io.signalzero.model.User;
import io.signalzero.repository.UserRepository;
import io.signalzero.service.UsageTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * REST Controller for user authentication and registration
 * Reference: DETAILED_DESIGN.md Section 10
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:8081", "http://localhost:3000"})
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsageTrackingService usageTrackingService;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$");

    /**
     * User registration
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String fullName = request.get("fullName");

            // Validate input
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
            }

            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 6 characters"));
            }

            if (!EMAIL_PATTERN.matcher(email.toLowerCase()).matches()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid email format"));
            }

            // Check if user already exists
            if (userRepository.findByEmail(email.toLowerCase()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already registered"));
            }

            // Create new user
            User user = new User();
            user.setEmail(email.toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFullName(fullName != null ? fullName.trim() : "");
            user.setSubscriptionTier(SubscriptionTier.FREE);
            user.setAnalysesUsedThisMonth(0);
            user.setAnalysesUsedTotal(0);
            user.setLastUsageReset(LocalDateTime.now());
            user.setIsActive(true);
            user.setIsVerified(false);
            user.setReferralCode(generateReferralCode());
            user.setReferralCount(0);
            // Note: createdAt and updatedAt are managed by JPA annotations

            // Save user
            User savedUser = userRepository.save(user);

            log.info("New user registered: {}", savedUser.getEmail());

            // Return user info (without password)
            return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "user", Map.of(
                    "id", savedUser.getId(),
                    "email", savedUser.getEmail(),
                    "fullName", savedUser.getFullName(),
                    "subscriptionTier", savedUser.getSubscriptionTier().name(),
                    "analysesRemaining", savedUser.getRemainingAnalyses(),
                    "monthlyLimit", savedUser.getSubscriptionTier().getMonthlyAnalyses(),
                    "referralCode", savedUser.getReferralCode()
                )
            ));

        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.get("email"), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * User login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password are required"));
            }

            // Find user
            User user = userRepository.findByEmail(email.toLowerCase()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
            }

            // Check password
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
            }

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("User logged in: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "subscriptionTier", user.getSubscriptionTier().name(),
                    "analysesUsed", user.getAnalysesUsedThisMonth(),
                    "analysesRemaining", user.getRemainingAnalyses(),
                    "monthlyLimit", user.getSubscriptionTier().getMonthlyAnalyses(),
                    "isVerified", user.getIsVerified(),
                    "referralCode", user.getReferralCode()
                )
            ));

        } catch (Exception e) {
            log.error("Login failed for email: {}", request.get("email"), e);
            return ResponseEntity.status(401)
                .body(Map.of("error", "Invalid credentials"));
        }
    }

    /**
     * Get current user profile
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
            }

            // Build user data map to avoid Map.of() argument limit
            Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());
            userData.put("subscriptionTier", user.getSubscriptionTier().name());
            userData.put("analysesUsed", user.getAnalysesUsedThisMonth());
            userData.put("analysesTotal", user.getAnalysesUsedTotal());
            userData.put("analysesRemaining", user.getRemainingAnalyses());
            userData.put("monthlyLimit", user.getSubscriptionTier().getMonthlyAnalyses());
            userData.put("isVerified", user.getIsVerified());
            userData.put("referralCode", user.getReferralCode());
            userData.put("referralCount", user.getReferralCount());
            userData.put("subscriptionStartDate", user.getSubscriptionStartDate());
            userData.put("subscriptionEndDate", user.getSubscriptionEndDate());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("lastLoginAt", user.getLastLoginAt());

            return ResponseEntity.ok(Map.of("user", userData));

        } catch (Exception e) {
            log.error("Failed to get user profile", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve profile"));
        }
    }

    /**
     * Update user profile
     * PUT /api/auth/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
            }

            String fullName = request.get("fullName");
            if (fullName != null) {
                user.setFullName(fullName.trim());
            }

            // Note: updatedAt is managed by JPA @UpdateTimestamp annotation
            User updatedUser = userRepository.save(user);

            log.info("Profile updated for user: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "fullName", updatedUser.getFullName(),
                    "subscriptionTier", updatedUser.getSubscriptionTier().name(),
                    "updatedAt", updatedUser.getUpdatedAt()
                )
            ));

        } catch (Exception e) {
            log.error("Failed to update profile", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to update profile"));
        }
    }

    /**
     * Change password
     * PUT /api/auth/password
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current and new passwords are required"));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "New password must be at least 6 characters"));
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current password is incorrect"));
            }

            // Update password
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            // Note: updatedAt is managed by JPA @UpdateTimestamp annotation
            userRepository.save(user);

            log.info("Password changed for user: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                "message", "Password changed successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to change password", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to change password"));
        }
    }

    /**
     * Get usage statistics
     * GET /api/auth/usage
     */
    @GetMapping("/usage")
    public ResponseEntity<?> getUsageStats() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Authentication required"));
            }

            int monthlyLimit = user.getSubscriptionTier().getMonthlyAnalyses();
            int remaining = user.getRemainingAnalyses();
            double usagePercent = (user.getAnalysesUsedThisMonth() * 100.0) / Math.max(1, monthlyLimit);

            return ResponseEntity.ok(Map.of(
                "subscriptionTier", user.getSubscriptionTier().name(),
                "monthlyLimit", monthlyLimit,
                "used", user.getAnalysesUsedThisMonth(),
                "remaining", remaining,
                "usagePercent", Math.round(usagePercent * 100.0) / 100.0,
                "totalAnalyses", user.getAnalysesUsedTotal(),
                "lastReset", user.getLastUsageReset(),
                "canAnalyze", user.canPerformAnalysis(),
                "upgradeRequired", remaining <= 0 && user.getSubscriptionTier() != SubscriptionTier.ENTERPRISE
            ));

        } catch (Exception e) {
            log.error("Failed to get usage stats", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve usage statistics"));
        }
    }

    /**
     * Logout user
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Logout failed"));
        }
    }

    // Helper methods

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return null;
            }

            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            return null;
        }
    }

    private String generateReferralCode() {
        // Generate a 8-character referral code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        
        // Ensure uniqueness
        String generatedCode = code.toString();
        while (userRepository.findByReferralCode(generatedCode).isPresent()) {
            code = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                code.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            generatedCode = code.toString();
        }
        
        return generatedCode;
    }
}
