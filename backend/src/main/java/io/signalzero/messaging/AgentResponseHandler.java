package io.signalzero.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles agent response messages and coordinates analysis completion
 * Reference: DETAILED_DESIGN.md Section 8.2 - Repository Pattern
 */
@Component
public class AgentResponseHandler {
    
    private static final Logger log = LoggerFactory.getLogger(AgentResponseHandler.class);
    
    @Autowired
    private SolacePublisher solacePublisher;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Track agent responses per analysis
    private final Map<String, Map<String, Object>> agentResponses = new ConcurrentHashMap<>();
    
    /**
     * Handle incoming agent response
     */
    public void handleResponse(String agentType, String messageBody, String correlationId) {
        try {
            log.debug("Processing response from agent: {}", agentType);
            
            // Parse the JSON response
            Map<String, Object> responseData = objectMapper.readValue(messageBody, Map.class);
            String analysisId = (String) responseData.get("analysisId");
            
            if (analysisId == null) {
                log.error("Analysis ID missing from agent response: {}", agentType);
                return;
            }
            
            // Store agent response
            storeAgentResponse(analysisId, agentType, responseData);
            
            // Update analysis with individual agent result (will implement when AnalysisService exists)
            updateAnalysisWithAgentResult(analysisId, agentType, responseData);
            
            // Check if all agents have responded
            if (allAgentsResponded(analysisId)) {
                completeAnalysis(analysisId);
            }
            
        } catch (Exception e) {
            log.error("Failed to handle agent response from {}: {}", agentType, e.getMessage(), e);
        }
    }
    
    private void storeAgentResponse(String analysisId, String agentType, Map<String, Object> responseData) {
        agentResponses.computeIfAbsent(analysisId, k -> new ConcurrentHashMap<>())
                     .put(agentType, responseData);
        
        log.debug("Stored response from {} for analysis: {}", agentType, analysisId);
    }
    
    private void updateAnalysisWithAgentResult(String analysisId, String agentType, Map<String, Object> responseData) {
        try {
            // Extract agent-specific data
            BigDecimal score = extractScore(responseData);
            BigDecimal confidence = extractConfidence(responseData);
            
            // TODO: Save agent result using service when AnalysisService is created
            log.debug("Agent result - Analysis: {}, Agent: {}, Score: {}, Confidence: {}", 
                     analysisId, agentType, score, confidence);
            
            // Publish real-time update
            solacePublisher.publishStatusUpdate(analysisId, "PROCESSING", "Agent " + agentType + " completed");
            
            log.debug("Updated analysis {} with {} result", analysisId, agentType);
            
        } catch (Exception e) {
            log.error("Failed to update analysis with agent result: {}", e.getMessage(), e);
        }
    }
    
    private BigDecimal extractScore(Map<String, Object> responseData) {
        Object scoreObj = responseData.get("score");
        if (scoreObj instanceof Number) {
            return BigDecimal.valueOf(((Number) scoreObj).doubleValue());
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal extractConfidence(Map<String, Object> responseData) {
        Object confidenceObj = responseData.get("confidence");
        if (confidenceObj instanceof Number) {
            return BigDecimal.valueOf(((Number) confidenceObj).doubleValue());
        }
        return BigDecimal.valueOf(50.0); // Default confidence
    }
    
    private boolean allAgentsResponded(String analysisId) {
        Map<String, Object> responses = agentResponses.get(analysisId);
        if (responses == null) return false;
        
        // Check if all 5 agents have responded
        return responses.size() >= 5 && 
               responses.containsKey("bot-detector") &&
               responses.containsKey("trend-analyzer") &&
               responses.containsKey("review-validator") &&
               responses.containsKey("promotion-detector") &&
               responses.containsKey("score-aggregator");
    }
    
    private void completeAnalysis(String analysisId) {
        try {
            log.info("All agents responded for analysis: {}, completing analysis", analysisId);
            
            Map<String, Object> allResponses = agentResponses.get(analysisId);
            
            // Extract individual scores for Reality Score calculation
            BigDecimal botScore = extractAgentScore(allResponses, "bot-detector");
            BigDecimal trendScore = extractAgentScore(allResponses, "trend-analyzer");
            BigDecimal reviewScore = extractAgentScore(allResponses, "review-validator");
            BigDecimal promotionScore = extractAgentScore(allResponses, "promotion-detector");
            
            // Calculate Reality Score using exact weights from DETAILED_DESIGN.md Section 9.2
            BigDecimal realityScore = calculateRealityScore(botScore, trendScore, reviewScore, promotionScore);
            
            // TODO: Complete the analysis using AnalysisService when it's created
            log.info("Analysis {} completed with Reality Score: {}", analysisId, realityScore);
            
            // Extract bot percentage for score update
            BigDecimal botPercentage = extractAgentScore(allResponses, "bot-detector");
            
            // Publish final score update (convert to int for SolacePublisher)
            solacePublisher.publishScoreUpdate(analysisId, botPercentage.intValue(), realityScore.intValue());
            
            // Add to Wall of Shame if highly manipulated (< 35% Reality Score)
            if (realityScore.compareTo(BigDecimal.valueOf(35)) < 0) {
                // Extract product name from analysis (will get from AnalysisService later)
                String productName = "Unknown Product"; // TODO: Get from analysis when AnalysisService exists
                solacePublisher.publishWallOfShameAdd(analysisId, productName, botPercentage.intValue(), realityScore.intValue());
            }
            
            // Clean up stored responses
            agentResponses.remove(analysisId);
            
        } catch (Exception e) {
            log.error("Failed to complete analysis {}: {}", analysisId, e.getMessage(), e);
        }
    }
    
    private BigDecimal extractAgentScore(Map<String, Object> allResponses, String agentType) {
        Map<String, Object> agentResponse = (Map<String, Object>) allResponses.get(agentType);
        if (agentResponse != null) {
            return extractScore(agentResponse);
        }
        return BigDecimal.valueOf(50); // Default fallback
    }
    
    /**
     * Calculate Reality Score using EXACT weights from DETAILED_DESIGN.md Section 9.2
     * Bot: 40%, Trend: 30%, Review: 20%, Promotion: 10%
     */
    private BigDecimal calculateRealityScore(BigDecimal botScore, BigDecimal trendScore, 
                                           BigDecimal reviewScore, BigDecimal promotionScore) {
        return botScore.multiply(BigDecimal.valueOf(0.4))
                .add(trendScore.multiply(BigDecimal.valueOf(0.3)))
                .add(reviewScore.multiply(BigDecimal.valueOf(0.2)))
                .add(promotionScore.multiply(BigDecimal.valueOf(0.1)))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    private int calculateProgress(String analysisId) {
        Map<String, Object> responses = agentResponses.get(analysisId);
        if (responses == null) return 0;
        
        // Progress: 20% per agent
        return Math.min(100, responses.size() * 20);
    }
    
    // Health check and monitoring
    public int getPendingAnalysesCount() {
        return agentResponses.size();
    }
    
    public String[] getPendingAnalysisIds() {
        return agentResponses.keySet().toArray(new String[0]);
    }
}
