package io.signalzero.messaging;

/**
 * Solace PubSub+ Topic Constants
 * Reference: DETAILED_DESIGN.md Section 3.3 - Exact naming from specification
 */
public final class SolaceTopics {
    
    // Core Analysis Flow
    public static final String ANALYSIS_REQUEST_BASE = "signalzero/analysis/request/";
    public static final String ANALYSIS_RESPONSE_BASE = "signalzero/analysis/response/";
    
    // Agent-Specific Topics - Request
    public static final String BOT_DETECTOR_REQUEST = "signalzero/agent/bot-detector/request";
    public static final String TREND_ANALYZER_REQUEST = "signalzero/agent/trend-analyzer/request";
    public static final String REVIEW_VALIDATOR_REQUEST = "signalzero/agent/review-validator/request";
    public static final String PROMOTION_DETECTOR_REQUEST = "signalzero/agent/promotion-detector/request";
    public static final String SCORE_AGGREGATOR_REQUEST = "signalzero/agent/score-aggregator/request";
    
    // Agent-Specific Topics - Response
    public static final String BOT_DETECTOR_RESPONSE = "signalzero/agent/bot-detector/response";
    public static final String TREND_ANALYZER_RESPONSE = "signalzero/agent/trend-analyzer/response";
    public static final String REVIEW_VALIDATOR_RESPONSE = "signalzero/agent/review-validator/response";
    public static final String PROMOTION_DETECTOR_RESPONSE = "signalzero/agent/promotion-detector/response";
    public static final String SCORE_AGGREGATOR_RESPONSE = "signalzero/agent/score-aggregator/response";
    
    // Real-time Updates
    public static final String SCORE_UPDATE_BASE = "signalzero/updates/score/";
    public static final String STATUS_UPDATE_BASE = "signalzero/updates/status/";
    public static final String WALL_OF_SHAME_ADD = "signalzero/dashboard/wall-of-shame/add";
    
    // Usage Tracking
    public static final String USAGE_ANALYSIS_BASE = "signalzero/usage/analysis/";
    public static final String USAGE_LIMIT_REACHED_BASE = "signalzero/usage/limit-reached/";
    
    // Topic Builder Methods
    public static String analysisRequest(String userId, String analysisId) {
        return ANALYSIS_REQUEST_BASE + userId + "/" + analysisId;
    }
    
    public static String analysisResponse(String userId, String analysisId) {
        return ANALYSIS_RESPONSE_BASE + userId + "/" + analysisId;
    }
    
    public static String scoreUpdate(String analysisId) {
        return SCORE_UPDATE_BASE + analysisId;
    }
    
    public static String statusUpdate(String analysisId) {
        return STATUS_UPDATE_BASE + analysisId;
    }
    
    public static String usageAnalysis(String userId) {
        return USAGE_ANALYSIS_BASE + userId;
    }
    
    public static String usageLimitReached(String userId) {
        return USAGE_LIMIT_REACHED_BASE + userId;
    }
    
    // Agent topic arrays for broadcasting
    public static final String[] ALL_AGENT_REQUEST_TOPICS = {
        BOT_DETECTOR_REQUEST,
        TREND_ANALYZER_REQUEST,
        REVIEW_VALIDATOR_REQUEST,
        PROMOTION_DETECTOR_REQUEST,
        SCORE_AGGREGATOR_REQUEST
    };
    
    public static final String[] ALL_AGENT_RESPONSE_TOPICS = {
        BOT_DETECTOR_RESPONSE,
        TREND_ANALYZER_RESPONSE,
        REVIEW_VALIDATOR_RESPONSE,
        PROMOTION_DETECTOR_RESPONSE,
        SCORE_AGGREGATOR_RESPONSE
    };
    
    // Agent type to topic mappings
    public static String getAgentRequestTopic(String agentType) {
        return switch (agentType.toLowerCase()) {
            case "bot-detector" -> BOT_DETECTOR_REQUEST;
            case "trend-analyzer" -> TREND_ANALYZER_REQUEST;
            case "review-validator" -> REVIEW_VALIDATOR_REQUEST;
            case "promotion-detector" -> PROMOTION_DETECTOR_REQUEST;
            case "score-aggregator" -> SCORE_AGGREGATOR_REQUEST;
            default -> throw new IllegalArgumentException("Unknown agent type: " + agentType);
        };
    }
    
    public static String getAgentResponseTopic(String agentType) {
        return switch (agentType.toLowerCase()) {
            case "bot-detector" -> BOT_DETECTOR_RESPONSE;
            case "trend-analyzer" -> TREND_ANALYZER_RESPONSE;
            case "review-validator" -> REVIEW_VALIDATOR_RESPONSE;
            case "promotion-detector" -> PROMOTION_DETECTOR_RESPONSE;
            case "score-aggregator" -> SCORE_AGGREGATOR_RESPONSE;
            default -> throw new IllegalArgumentException("Unknown agent type: " + agentType);
        };
    }
    
    // Private constructor to prevent instantiation
    private SolaceTopics() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
