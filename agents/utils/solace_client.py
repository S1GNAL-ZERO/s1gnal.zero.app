"""
Solace Client Utility for S1GNAL.ZERO Agents
Provides production-ready Solace PubSub+ connectivity for all agents
"""

import os
import json
import time
import logging
import threading
from typing import Dict, Any, Optional, Callable
from dataclasses import dataclass
from datetime import datetime

try:
    from solace.messaging.messaging_service import MessagingService
    from solace.messaging.resources.topic import Topic
    from solace.messaging.config.solace_properties import SolaceProperties
    from solace.messaging.config.authentication_strategy import ClientCertificateAuthentication
    from solace.messaging.publisher.direct_message_publisher import DirectMessagePublisher
    from solace.messaging.receiver.direct_message_receiver import DirectMessageReceiver
    from solace.messaging.receiver.message_receiver import MessageHandler
    from solace.messaging.resources.topic_subscription import TopicSubscription
    from solace.messaging.errors.pubsubplus_client_error import PubSubPlusClientError
    SOLACE_AVAILABLE = True
except ImportError:
    SOLACE_AVAILABLE = False
    logging.warning("Solace Python API not available. Using mock mode.")

logger = logging.getLogger(__name__)

@dataclass
class SolaceConfig:
    """Solace connection configuration from environment variables"""
    host: str
    vpn: str
    username: str
    password: str
    client_name: str
    timeout_seconds: int = 30
    reconnect_retries: int = 3
    reconnect_delay: int = 5

    @classmethod
    def from_env(cls, agent_name: str) -> 'SolaceConfig':
        """Create configuration from environment variables"""
        return cls(
            host=os.getenv('SOLACE_HOST', 'tcp://localhost:55555'),
            vpn=os.getenv('SOLACE_VPN', 'default'),
            username=os.getenv('SOLACE_USERNAME', 'admin'),
            password=os.getenv('SOLACE_PASSWORD', 'admin'),
            client_name=f"{os.getenv('SOLACE_CLIENT_NAME_PREFIX', 'signalzero-agent')}-{agent_name}",
            timeout_seconds=int(os.getenv('AGENT_TIMEOUT_SECONDS', '30')),
            reconnect_retries=int(os.getenv('AGENT_MAX_RETRIES', '3')),
            reconnect_delay=int(os.getenv('AGENT_RETRY_DELAY_SECONDS', '5'))
        )

class SolaceClient:
    """
    Production-ready Solace client for S1GNAL.ZERO agents
    Handles connection management, publishing, subscribing, and error recovery
    """
    
    def __init__(self, agent_name: str):
        self.agent_name = agent_name
        self.config = SolaceConfig.from_env(agent_name)
        self.messaging_service: Optional[MessagingService] = None
        self.publisher: Optional[DirectMessagePublisher] = None
        self.receiver: Optional[DirectMessageReceiver] = None
        self.is_connected = False
        self.connection_lock = threading.Lock()
        self.message_handlers: Dict[str, Callable] = {}
        self.subscriptions: set = set()
        self.stats = {
            'messages_sent': 0,
            'messages_received': 0,
            'connection_attempts': 0,
            'last_error': None,
            'connected_at': None
        }
        
        # Setup logging
        self.logger = logging.getLogger(f"solace.{agent_name}")
        
    def connect(self) -> bool:
        """
        Establish connection to Solace PubSub+
        Returns True if successful, False otherwise
        """
        with self.connection_lock:
            if self.is_connected:
                return True
                
            if not SOLACE_AVAILABLE:
                self.logger.warning("Solace API not available, using mock mode")
                self.is_connected = True
                self.stats['connected_at'] = datetime.now()
                return True
                
            try:
                self.stats['connection_attempts'] += 1
                self.logger.info(f"Connecting to Solace at {self.config.host}")
                
                # Build broker properties
                broker_props = {
                    SolaceProperties.TransportLayerProperties.HOST: self.config.host,
                    SolaceProperties.ServiceProperties.VPN_NAME: self.config.vpn,
                    SolaceProperties.AuthenticationProperties.SCHEME_BASIC_USER_NAME: self.config.username,
                    SolaceProperties.AuthenticationProperties.SCHEME_BASIC_PASSWORD: self.config.password,
                    SolaceProperties.TransportLayerProperties.RECONNECTION_ATTEMPTS: self.config.reconnect_retries,
                    SolaceProperties.TransportLayerProperties.CONNECTION_RETRIES_PER_HOST: self.config.reconnect_retries
                }
                
                # Create messaging service
                self.messaging_service = MessagingService.builder().from_properties(broker_props).build()
                self.messaging_service.connect()
                
                # Create publisher
                self.publisher = self.messaging_service.create_direct_message_publisher_builder().build()
                self.publisher.start()
                
                # Create receiver
                self.receiver = self.messaging_service.create_direct_message_receiver_builder().build()
                self.receiver.start()
                
                self.is_connected = True
                self.stats['connected_at'] = datetime.now()
                self.logger.info(f"Successfully connected to Solace as {self.config.client_name}")
                return True
                
            except Exception as e:
                self.stats['last_error'] = str(e)
                self.logger.error(f"Failed to connect to Solace: {e}")
                self._cleanup_connection()
                return False
    
    def disconnect(self):
        """Gracefully disconnect from Solace"""
        with self.connection_lock:
            if not self.is_connected:
                return
                
            self.logger.info("Disconnecting from Solace")
            
            try:
                self._cleanup_connection()
                self.is_connected = False
                self.logger.info("Successfully disconnected from Solace")
            except Exception as e:
                self.logger.error(f"Error during disconnect: {e}")
    
    def _cleanup_connection(self):
        """Clean up Solace connection resources"""
        try:
            if self.receiver:
                self.receiver.terminate()
                self.receiver = None
                
            if self.publisher:
                self.publisher.terminate()
                self.publisher = None
                
            if self.messaging_service:
                self.messaging_service.disconnect()
                self.messaging_service = None
                
        except Exception as e:
            self.logger.error(f"Error cleaning up connection: {e}")
    
    def publish_message(self, topic: str, payload: Dict[str, Any], correlation_id: Optional[str] = None) -> bool:
        """
        Publish message to Solace topic
        
        Args:
            topic: Solace topic to publish to
            payload: Message payload as dictionary
            correlation_id: Optional correlation ID for message tracking
            
        Returns:
            True if message published successfully, False otherwise
        """
        if not self.is_connected:
            if not self.connect():
                return False
        
        if not SOLACE_AVAILABLE:
            # Mock mode - just log the message
            self.logger.info(f"MOCK: Publishing to {topic}: {json.dumps(payload, indent=2)}")
            self.stats['messages_sent'] += 1
            return True
            
        try:
            # Create message
            message_body = json.dumps(payload)
            destination_topic = Topic.of(topic)
            
            # Build message
            outbound_msg = self.messaging_service.message_builder() \
                .with_application_message_id(correlation_id) \
                .with_property("agent", self.agent_name) \
                .with_property("timestamp", datetime.now().isoformat()) \
                .build(message_body)
            
            # Publish message
            self.publisher.publish(destination=destination_topic, message=outbound_msg)
            
            self.stats['messages_sent'] += 1
            self.logger.debug(f"Published message to {topic} with correlation_id: {correlation_id}")
            return True
            
        except Exception as e:
            self.stats['last_error'] = str(e)
            self.logger.error(f"Failed to publish message to {topic}: {e}")
            return False
    
    def subscribe_to_topic(self, topic: str, message_handler: Callable[[Dict[str, Any]], None]) -> bool:
        """
        Subscribe to Solace topic with message handler
        
        Args:
            topic: Topic pattern to subscribe to
            message_handler: Function to handle received messages
            
        Returns:
            True if subscription successful, False otherwise
        """
        if not self.is_connected:
            if not self.connect():
                return False
        
        if not SOLACE_AVAILABLE:
            # Mock mode - just register handler
            self.message_handlers[topic] = message_handler
            self.subscriptions.add(topic)
            self.logger.info(f"MOCK: Subscribed to {topic}")
            return True
            
        try:
            # Create topic subscription
            topic_subscription = TopicSubscription.of(topic)
            
            # Create message handler wrapper
            def solace_message_handler(message):
                try:
                    # Extract message payload
                    payload_str = message.get_payload_as_string()
                    payload = json.loads(payload_str) if payload_str else {}
                    
                    # Add metadata
                    payload['_metadata'] = {
                        'topic': topic,
                        'timestamp': datetime.now().isoformat(),
                        'correlation_id': message.get_application_message_id(),
                        'sender_id': message.get_sender_id()
                    }
                    
                    # Call user handler
                    message_handler(payload)
                    self.stats['messages_received'] += 1
                    
                except Exception as e:
                    self.logger.error(f"Error processing message from {topic}: {e}")
            
            # Subscribe
            self.receiver.receive_async(topic_subscription, MessageHandler(solace_message_handler))
            
            self.message_handlers[topic] = message_handler
            self.subscriptions.add(topic)
            self.logger.info(f"Subscribed to topic: {topic}")
            return True
            
        except Exception as e:
            self.stats['last_error'] = str(e)
            self.logger.error(f"Failed to subscribe to {topic}: {e}")
            return False
    
    def unsubscribe_from_topic(self, topic: str) -> bool:
        """
        Unsubscribe from Solace topic
        
        Args:
            topic: Topic to unsubscribe from
            
        Returns:
            True if unsubscription successful, False otherwise
        """
        if topic not in self.subscriptions:
            return True
            
        if not SOLACE_AVAILABLE:
            # Mock mode
            self.subscriptions.discard(topic)
            self.message_handlers.pop(topic, None)
            self.logger.info(f"MOCK: Unsubscribed from {topic}")
            return True
            
        try:
            topic_subscription = TopicSubscription.of(topic)
            self.receiver.remove_subscription(topic_subscription)
            
            self.subscriptions.discard(topic)
            self.message_handlers.pop(topic, None)
            self.logger.info(f"Unsubscribed from topic: {topic}")
            return True
            
        except Exception as e:
            self.stats['last_error'] = str(e)
            self.logger.error(f"Failed to unsubscribe from {topic}: {e}")
            return False
    
    def send_heartbeat(self, status: str = "healthy") -> bool:
        """
        Send agent heartbeat message
        
        Args:
            status: Agent status (healthy, warning, error)
            
        Returns:
            True if heartbeat sent successfully
        """
        heartbeat_topic = f"signalzero/agent/{self.agent_name}/heartbeat"
        heartbeat_payload = {
            'agent_name': self.agent_name,
            'status': status,
            'timestamp': datetime.now().isoformat(),
            'stats': self.stats.copy(),
            'subscriptions': list(self.subscriptions)
        }
        
        return self.publish_message(heartbeat_topic, heartbeat_payload)
    
    def get_connection_status(self) -> Dict[str, Any]:
        """
        Get current connection status and statistics
        
        Returns:
            Dictionary with connection status and stats
        """
        return {
            'agent_name': self.agent_name,
            'is_connected': self.is_connected,
            'solace_available': SOLACE_AVAILABLE,
            'config': {
                'host': self.config.host,
                'vpn': self.config.vpn,
                'username': self.config.username,
                'client_name': self.config.client_name
            },
            'subscriptions': list(self.subscriptions),
            'stats': self.stats.copy()
        }
    
    def wait_for_connection(self, timeout_seconds: int = 30) -> bool:
        """
        Wait for connection to be established
        
        Args:
            timeout_seconds: Maximum time to wait
            
        Returns:
            True if connected within timeout, False otherwise
        """
        start_time = time.time()
        
        while time.time() - start_time < timeout_seconds:
            if self.is_connected:
                return True
            
            if not self.connect():
                time.sleep(1)
            else:
                return True
                
        return False
    
    def simulate_message_for_testing(self, topic: str, payload: Dict[str, Any]):
        """
        Simulate receiving a message (for testing in mock mode)
        
        Args:
            topic: Topic the message came from
            payload: Message payload
        """
        if topic in self.message_handlers:
            # Add metadata like real Solace messages
            payload['_metadata'] = {
                'topic': topic,
                'timestamp': datetime.now().isoformat(),
                'correlation_id': f"test-{int(time.time())}",
                'sender_id': 'test-sender'
            }
            
            try:
                self.message_handlers[topic](payload)
                self.stats['messages_received'] += 1
                self.logger.debug(f"Simulated message processed for {topic}")
            except Exception as e:
                self.logger.error(f"Error processing simulated message: {e}")

class SolaceTopics:
    """
    Solace topic constants for S1GNAL.ZERO system
    From DETAILED_DESIGN.md Section 3.3
    """
    
    # Core Analysis Flow
    ANALYSIS_REQUEST = "signalzero/analysis/request"
    ANALYSIS_RESPONSE = "signalzero/analysis/response"
    
    # Agent-Specific Topics
    BOT_DETECTOR_REQUEST = "signalzero/agent/bot-detector/request"
    BOT_DETECTOR_RESPONSE = "signalzero/agent/bot-detector/response"
    
    TREND_ANALYZER_REQUEST = "signalzero/agent/trend-analyzer/request"
    TREND_ANALYZER_RESPONSE = "signalzero/agent/trend-analyzer/response"
    
    REVIEW_VALIDATOR_REQUEST = "signalzero/agent/review-validator/request"
    REVIEW_VALIDATOR_RESPONSE = "signalzero/agent/review-validator/response"
    
    PAID_PROMOTION_REQUEST = "signalzero/agent/paid-promotion/request"
    PAID_PROMOTION_RESPONSE = "signalzero/agent/paid-promotion/response"
    
    SCORE_AGGREGATOR_REQUEST = "signalzero/agent/score-aggregator/request"
    SCORE_AGGREGATOR_RESPONSE = "signalzero/agent/score-aggregator/response"
    
    # Real-time Updates
    UPDATES_SCORE = "signalzero/updates/score"
    UPDATES_STATUS = "signalzero/updates/status"
    DASHBOARD_WALL_OF_SHAME = "signalzero/dashboard/wall-of-shame/add"
    
    # Usage Tracking
    USAGE_ANALYSIS = "signalzero/usage/analysis"
    USAGE_LIMIT_REACHED = "signalzero/usage/limit-reached"
    
    # Agent Management
    AGENT_HEARTBEAT = "signalzero/agent/+/heartbeat"
    AGENT_STATUS = "signalzero/agent/+/status"

def create_solace_client(agent_name: str) -> SolaceClient:
    """
    Factory function to create configured Solace client
    
    Args:
        agent_name: Name of the agent (used for client identification)
        
    Returns:
        Configured SolaceClient instance
    """
    return SolaceClient(agent_name)

# Example usage and testing
if __name__ == "__main__":
    # Configure logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Test client creation and connection
    client = create_solace_client("test-agent")
    
    print("Testing Solace client...")
    print(f"Connection status: {client.get_connection_status()}")
    
    # Test connection
    if client.connect():
        print("✅ Connection successful")
        
        # Test heartbeat
        if client.send_heartbeat("testing"):
            print("✅ Heartbeat sent")
        
        # Test subscription (mock handler)
        def test_handler(message):
            print(f"Received test message: {message}")
        
        if client.subscribe_to_topic("test/topic", test_handler):
            print("✅ Subscription successful")
        
        # Test publishing
        test_payload = {"test": "message", "timestamp": datetime.now().isoformat()}
        if client.publish_message("test/topic", test_payload, "test-correlation-id"):
            print("✅ Message published")
        
        # In mock mode, simulate receiving the message
        if not SOLACE_AVAILABLE:
            client.simulate_message_for_testing("test/topic", {"response": "test"})
        
        # Cleanup
        client.disconnect()
        print("✅ Disconnected successfully")
    else:
        print("❌ Connection failed")
    
    print(f"Final status: {client.get_connection_status()}")
