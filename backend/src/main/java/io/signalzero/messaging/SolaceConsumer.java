package io.signalzero.messaging;

import com.solacesystems.jcsmp.*;
import io.signalzero.config.SolaceProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * Production-ready Solace Consumer Service
 * Handles all agent response messages with robust error handling
 * Reference: DETAILED_DESIGN.md Section 8.2
 */
@Service
public class SolaceConsumer implements XMLMessageListener {
    
    private static final Logger log = LoggerFactory.getLogger(SolaceConsumer.class);
    
    @Autowired
    private JCSMPSession session;
    
    @Autowired
    private SolaceProperties solaceProperties;
    
    @Autowired
    private AgentResponseHandler agentResponseHandler;
    
    private boolean isInitialized = false;
    private final CountDownLatch latch = new CountDownLatch(1);
    
    @PostConstruct
    public void initialize() {
        try {
            setupConsumers();
            isInitialized = true;
            log.info("✅ Solace Consumer initialized successfully with message listener");
        } catch (Exception e) {
            log.error("❌ Failed to initialize Solace Consumer", e);
            throw new RuntimeException("Failed to initialize Solace Consumer", e);
        }
    }
    
    private void setupConsumers() throws JCSMPException {
        // Set up the message listener for direct messages
        session.getMessageConsumer(this);
        
        // Subscribe to all agent response topics and request topics for debugging
        subscribeToAgentTopics();
        
        log.info("🔗 Solace message listener configured and subscriptions active");
    }
    
    private void subscribeToAgentTopics() throws JCSMPException {
        // Subscribe to individual agent response topics
        Topic botDetectorResponse = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.BOT_DETECTOR_RESPONSE);
        Topic trendAnalyzerResponse = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.TREND_ANALYZER_RESPONSE);
        Topic reviewValidatorResponse = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.REVIEW_VALIDATOR_RESPONSE);
        Topic promotionDetectorResponse = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.PROMOTION_DETECTOR_RESPONSE);
        Topic scoreAggregatorResponse = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.SCORE_AGGREGATOR_RESPONSE);
        
        // Also subscribe to request topics for debugging (to see what we're publishing)
        Topic botDetectorRequest = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.BOT_DETECTOR_REQUEST);
        Topic trendAnalyzerRequest = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.TREND_ANALYZER_REQUEST);
        Topic reviewValidatorRequest = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.REVIEW_VALIDATOR_REQUEST);
        Topic promotionDetectorRequest = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.PROMOTION_DETECTOR_REQUEST);
        Topic scoreAggregatorRequest = JCSMPFactory.onlyInstance().createTopic(SolaceTopics.SCORE_AGGREGATOR_REQUEST);
        
        // Subscribe to response topics
        session.addSubscription(botDetectorResponse);
        session.addSubscription(trendAnalyzerResponse);
        session.addSubscription(reviewValidatorResponse);
        session.addSubscription(promotionDetectorResponse);
        session.addSubscription(scoreAggregatorResponse);
        
        // Subscribe to request topics for debugging
        session.addSubscription(botDetectorRequest);
        session.addSubscription(trendAnalyzerRequest);
        session.addSubscription(reviewValidatorRequest);
        session.addSubscription(promotionDetectorRequest);
        session.addSubscription(scoreAggregatorRequest);
        
        // Subscribe to status and score update topics
        Topic statusUpdates = JCSMPFactory.onlyInstance().createTopic("signalzero/updates/status/>");
        Topic scoreUpdates = JCSMPFactory.onlyInstance().createTopic("signalzero/updates/score/>");
        Topic wallOfShameUpdates = JCSMPFactory.onlyInstance().createTopic("signalzero/dashboard/wall-of-shame/>");
        
        session.addSubscription(statusUpdates);
        session.addSubscription(scoreUpdates);
        session.addSubscription(wallOfShameUpdates);
        
        log.info("📡 Subscribed to all agent topics (requests, responses, and updates)");
    }

    @Override
    public void onReceive(BytesXMLMessage message) {
        try {
            String topicName = message.getDestination().getName();
            String messageBody = message.getBytes() != null ? new String(message.getBytes()) : "null";
            String correlationId = message.getCorrelationId();
            
            log.info("📨 Received message from topic: {} | correlationId: {} | body: {}", 
                    topicName, correlationId, messageBody.length() > 200 ? messageBody.substring(0, 200) + "..." : messageBody);
            
            // Handle different types of messages
            if (topicName.contains("/response")) {
                handleAgentResponse(message);
            } else if (topicName.contains("/request")) {
                log.info("🚀 DEBUG - Saw request message on topic: {}", topicName);
            } else if (topicName.contains("/updates/")) {
                log.info("📊 Received update message on topic: {}", topicName);
            } else {
                log.info("❓ Received unknown message type on topic: {}", topicName);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to process incoming message", e);
        }
    }

    @Override
    public void onException(JCSMPException exception) {
        log.error("❌ Solace Consumer Exception: {}", exception.getMessage(), exception);
    }
    
    private void handleAgentResponse(BytesXMLMessage message) {
        try {
            String topicName = message.getDestination().getName();
            String messageBody = new String(message.getBytes());
            String correlationId = message.getCorrelationId();
            
            log.debug("Received agent response from topic: {}, correlationId: {}", topicName, correlationId);
            
            // Determine agent type from topic
            String agentType = extractAgentType(topicName);
            
            // Parse and handle the response
            agentResponseHandler.handleResponse(agentType, messageBody, correlationId);
            
            log.debug("Successfully processed agent response from: {}", agentType);
            
        } catch (Exception e) {
            log.error("Failed to handle agent response message", e);
            throw new RuntimeException("Failed to process agent response", e);
        }
    }
    
    private String extractAgentType(String topicName) {
        // Extract agent type from topic name
        // signalzero/agent/bot-detector/response -> bot-detector
        String[] parts = topicName.split("/");
        if (parts.length >= 3) {
            return parts[2]; // agent type is the 3rd part
        }
        return "unknown";
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            isInitialized = false;
            latch.countDown();
            log.info("Solace Consumer cleanup completed");
        } catch (Exception e) {
            log.error("Error during Solace Consumer cleanup", e);
        }
    }
    
    // Health check method
    public boolean isHealthy() {
        return isInitialized && session != null && !session.isClosed();
    }
    
    // Get consumer statistics
    public String getConsumerStats() {
        if (!isInitialized) {
            return "Consumer not initialized";
        }
        
        return String.format("Consumer Status: %s, Session Closed: %s", 
            isInitialized ? "ACTIVE" : "INACTIVE",
            session.isClosed() ? "YES" : "NO");
    }
}
