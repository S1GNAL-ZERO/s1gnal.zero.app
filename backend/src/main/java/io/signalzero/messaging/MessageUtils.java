package io.signalzero.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for message serialization/deserialization
 * Works with entities directly - no DTO conversion needed
 * Reference: DETAILED_DESIGN.md Section 8 - Repository Pattern
 */
public class MessageUtils {
    
    private static final Logger log = LoggerFactory.getLogger(MessageUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
    
    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to object: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize from JSON", e);
        }
    }
    
    /**
     * Create analysis request message from parameters
     */
    public static AnalysisRequestMessage createAnalysisRequest(String analysisId, String userId, 
                                                              String query, String queryType, String platform) {
        return new AnalysisRequestMessage(analysisId, userId, query, queryType, platform);
    }
    
    /**
     * Create successful agent response message
     */
    public static AgentResponseMessage createSuccessResponse(String analysisId, String agentType,
                                                           double score, double confidence) {
        return new AgentResponseMessage(analysisId, agentType, 
            java.math.BigDecimal.valueOf(score), 
            java.math.BigDecimal.valueOf(confidence), 
            null);
    }
    
    /**
     * Create error agent response message
     */
    public static AgentResponseMessage createErrorResponse(String analysisId, String agentType, 
                                                         String errorMessage) {
        return new AgentResponseMessage(analysisId, agentType, errorMessage);
    }
    
    /**
     * Extract agent type from Solace topic name
     * signalzero/agent/bot-detector/response -> bot-detector
     */
    public static String extractAgentTypeFromTopic(String topicName) {
        if (topicName == null || topicName.trim().isEmpty()) {
            return "unknown";
        }
        
        String[] parts = topicName.split("/");
        if (parts.length >= 3) {
            return parts[2]; // agent type is the 3rd part
        }
        return "unknown";
    }
    
    /**
     * Validate required fields in analysis request
     */
    public static boolean isValidAnalysisRequest(AnalysisRequestMessage request) {
        return request != null &&
               request.getAnalysisId() != null && !request.getAnalysisId().trim().isEmpty() &&
               request.getUserId() != null && !request.getUserId().trim().isEmpty() &&
               request.getQuery() != null && !request.getQuery().trim().isEmpty();
    }
    
    /**
     * Validate required fields in agent response
     */
    public static boolean isValidAgentResponse(AgentResponseMessage response) {
        return response != null &&
               response.getAnalysisId() != null && !response.getAnalysisId().trim().isEmpty() &&
               response.getAgentType() != null && !response.getAgentType().trim().isEmpty() &&
               response.getStatus() != null && !response.getStatus().trim().isEmpty();
    }
    
    /**
     * Generate correlation ID for analysis
     */
    public static String generateCorrelationId(String analysisId) {
        return analysisId + "_" + System.currentTimeMillis();
    }
    
    /**
     * Check if message is from demo hardcoded values
     */
    public static boolean isDemoQuery(String query) {
        if (query == null) return false;
        
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("stanley cup") || 
               lowerQuery.contains("$buzz") || 
               lowerQuery.contains("prime energy");
    }
    
    /**
     * Get hardcoded demo Reality Score for specific queries
     * Reference: DETAILED_DESIGN.md Section 14 - Demo Values
     */
    public static Integer getDemoRealityScore(String query) {
        if (query == null) return null;
        
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("stanley cup")) {
            return 34; // 62% bots = 34% Reality Score
        } else if (lowerQuery.contains("$buzz")) {
            return 12; // 87% bots = 12% Reality Score
        } else if (lowerQuery.contains("prime energy")) {
            return 29; // 71% bots = 29% Reality Score
        }
        
        return null;
    }
    
    /**
     * Get hardcoded demo bot percentage for specific queries
     * Reference: DETAILED_DESIGN.md Section 14 - Demo Values
     */
    public static Integer getDemoBotPercentage(String query) {
        if (query == null) return null;
        
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("stanley cup")) {
            return 62;
        } else if (lowerQuery.contains("$buzz")) {
            return 87;
        } else if (lowerQuery.contains("prime energy")) {
            return 71;
        }
        
        return null;
    }
}
