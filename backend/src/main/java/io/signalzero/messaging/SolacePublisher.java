package io.signalzero.messaging;

import com.solacesystems.jcsmp.*;
import io.signalzero.config.SolaceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Solace PubSub+ Publisher Service
 * Reference: DETAILED_DESIGN.md Section 8.1
 * Handles publishing messages to Solace topics for agent communication
 */
@Service
public class SolacePublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(SolacePublisher.class);
    
    @Autowired
    private XMLMessageProducer producer;
    
    @Autowired
    private SolaceConfig solaceConfig;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Publish analysis request to all agents
     * This triggers the multi-agent analysis pipeline
     */
    public void publishAnalysisRequest(String userId, String analysisId, String query, String platform) {
        try {
            // Create analysis request message
            AnalysisRequestMessage requestMessage = new AnalysisRequestMessage();
            requestMessage.setUserId(userId);
            requestMessage.setAnalysisId(analysisId);
            requestMessage.setQuery(query);
            requestMessage.setPlatform(platform);
            requestMessage.setTimestamp(System.currentTimeMillis());
            requestMessage.setCorrelationId(UUID.randomUUID().toString());
            
            String messageJson = objectMapper.writeValueAsString(requestMessage);
            
            // Publish to all agent request topics
            for (String agentRequestTopic : SolaceTopics.ALL_AGENT_REQUEST_TOPICS) {
                publishToTopic(agentRequestTopic, messageJson, analysisId);
            }
            
            logger.info("Published analysis request for analysisId: {} to all agents", analysisId);
            
        } catch (Exception e) {
            logger.error("Failed to publish analysis request for analysisId: {}", analysisId, e);
            throw new RuntimeException("Failed to publish analysis request", e);
        }
    }
    
    /**
     * Publish score update to real-time UI
     */
    public void publishScoreUpdate(String analysisId, int botPercentage, int realityScore) {
        try {
            ScoreUpdateMessage scoreUpdate = new ScoreUpdateMessage();
            scoreUpdate.setAnalysisId(analysisId);
            scoreUpdate.setBotPercentage(botPercentage);
            scoreUpdate.setRealityScore(realityScore);
            scoreUpdate.setTimestamp(System.currentTimeMillis());
            
            String messageJson = objectMapper.writeValueAsString(scoreUpdate);
            String topic = SolaceTopics.scoreUpdate(analysisId);
            
            publishToTopic(topic, messageJson, analysisId);
            
            logger.debug("Published score update for analysisId: {} - Reality Score: {}%", analysisId, realityScore);
            
        } catch (Exception e) {
            logger.error("Failed to publish score update for analysisId: {}", analysisId, e);
        }
    }
    
    /**
     * Publish status update to real-time UI
     */
    public void publishStatusUpdate(String analysisId, String status, String message) {
        try {
            StatusUpdateMessage statusUpdate = new StatusUpdateMessage();
            statusUpdate.setAnalysisId(analysisId);
            statusUpdate.setStatus(status);
            statusUpdate.setMessage(message);
            statusUpdate.setTimestamp(System.currentTimeMillis());
            
            String messageJson = objectMapper.writeValueAsString(statusUpdate);
            String topic = SolaceTopics.statusUpdate(analysisId);
            
            publishToTopic(topic, messageJson, analysisId);
            
            logger.debug("Published status update for analysisId: {} - Status: {}", analysisId, status);
            
        } catch (Exception e) {
            logger.error("Failed to publish status update for analysisId: {}", analysisId, e);
        }
    }
    
    /**
     * Publish usage tracking event
     */
    public void publishUsageEvent(String userId, String eventType) {
        try {
            UsageEventMessage usageEvent = new UsageEventMessage();
            usageEvent.setUserId(userId);
            usageEvent.setEventType(eventType);
            usageEvent.setTimestamp(System.currentTimeMillis());
            
            String messageJson = objectMapper.writeValueAsString(usageEvent);
            String topic = SolaceTopics.usageAnalysis(userId);
            
            publishToTopic(topic, messageJson, userId);
            
            logger.debug("Published usage event for userId: {} - Event: {}", userId, eventType);
            
        } catch (Exception e) {
            logger.error("Failed to publish usage event for userId: {}", userId, e);
        }
    }
    
    /**
     * Publish Wall of Shame addition
     */
    public void publishWallOfShameAdd(String analysisId, String productName, int botPercentage, int realityScore) {
        try {
            WallOfShameMessage wallMessage = new WallOfShameMessage();
            wallMessage.setAnalysisId(analysisId);
            wallMessage.setProductName(productName);
            wallMessage.setBotPercentage(botPercentage);
            wallMessage.setRealityScore(realityScore);
            wallMessage.setTimestamp(System.currentTimeMillis());
            
            String messageJson = objectMapper.writeValueAsString(wallMessage);
            
            publishToTopic(SolaceTopics.WALL_OF_SHAME_ADD, messageJson, analysisId);
            
            logger.info("Published Wall of Shame addition: {} with {}% bots, {}% Reality Score", 
                       productName, botPercentage, realityScore);
            
        } catch (Exception e) {
            logger.error("Failed to publish Wall of Shame addition for: {}", productName, e);
        }
    }
    
    /**
     * Core method to publish message to any topic
     */
    private void publishToTopic(String topicName, String messageJson, String correlationId) {
        try {
            // Create topic destination
            Topic topic = solaceConfig.createTopic(topicName);
            
            // Create text message
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setText(messageJson);
            
            // Set message properties
            message.setCorrelationId(correlationId);
            message.setTimeToLive(300000); // 5 minutes TTL
            message.setDeliveryMode(DeliveryMode.DIRECT); // Using direct delivery for speed
            
            // Set custom properties for debugging
            SDTMap properties = JCSMPFactory.onlyInstance().createMap();
            properties.putString("source", "signalzero-backend");
            properties.putString("version", "1.0.0");
            properties.putLong("publishTime", System.currentTimeMillis());
            message.setProperties(properties);
            
            // Publish message
            producer.send(message, topic);
            
            logger.debug("Successfully published message to topic: {} with correlationId: {}", topicName, correlationId);
            
        } catch (JCSMPException e) {
            logger.error("Failed to publish message to topic: {} with correlationId: {}", topicName, correlationId, e);
            
            // For hackathon - don't fail the whole operation if message publishing fails
            // In production, we might want to implement retry logic or circuit breaker
            
        } catch (Exception e) {
            logger.error("Unexpected error publishing to topic: {} with correlationId: {}", topicName, correlationId, e);
        }
    }
    
    /**
     * Health check method
     */
    public boolean isHealthy() {
        return solaceConfig.isSessionHealthy() && producer != null;
    }
    
    /**
     * Get publisher statistics
     */
    public String getPublisherStats() {
        if (isHealthy()) {
            return "Publisher active, session: " + solaceConfig.getSessionInfo();
        }
        return "Publisher inactive";
    }
    
    // Message Classes
    public static class AnalysisRequestMessage {
        private String userId;
        private String analysisId;
        private String query;
        private String platform;
        private long timestamp;
        private String correlationId;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getAnalysisId() { return analysisId; }
        public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
        
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    }
    
    public static class ScoreUpdateMessage {
        private String analysisId;
        private int botPercentage;
        private int realityScore;
        private long timestamp;
        
        // Getters and Setters
        public String getAnalysisId() { return analysisId; }
        public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
        
        public int getBotPercentage() { return botPercentage; }
        public void setBotPercentage(int botPercentage) { this.botPercentage = botPercentage; }
        
        public int getRealityScore() { return realityScore; }
        public void setRealityScore(int realityScore) { this.realityScore = realityScore; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class StatusUpdateMessage {
        private String analysisId;
        private String status;
        private String message;
        private long timestamp;
        
        // Getters and Setters
        public String getAnalysisId() { return analysisId; }
        public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class UsageEventMessage {
        private String userId;
        private String eventType;
        private long timestamp;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class WallOfShameMessage {
        private String analysisId;
        private String productName;
        private int botPercentage;
        private int realityScore;
        private long timestamp;
        
        // Getters and Setters
        public String getAnalysisId() { return analysisId; }
        public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public int getBotPercentage() { return botPercentage; }
        public void setBotPercentage(int botPercentage) { this.botPercentage = botPercentage; }
        
        public int getRealityScore() { return realityScore; }
        public void setRealityScore(int realityScore) { this.realityScore = realityScore; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
