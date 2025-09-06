"""
Base Agent Class for S1GNAL.ZERO Multi-Agent System
Provides common functionality for all specialized agents including Solace integration and fallback mechanisms.
"""

import json
import logging
import threading
import time
import uuid
from abc import ABC, abstractmethod
from typing import Dict, Any, Optional, List
from datetime import datetime, timezone

try:
    from solace.messaging.messaging_service import MessagingService
    from solace.messaging.resources.topic import Topic
    from solace.messaging.publisher.direct_message_publisher import DirectMessagePublisher
    from solace.messaging.receiver.direct_message_receiver import DirectMessageReceiver
    from solace.messaging.config.solace_properties import SolaceProperties
    from solace.messaging.config.authentication_strategy import ClientCertificateAuthentication
    from solace.messaging.config.transport_security_strategy import TLS
    from solace.messaging.receiver.message_receiver import MessageHandler
    from solace.messaging.resources.topic_subscription import TopicSubscription
    SOLACE_AVAILABLE = True
except ImportError:
    SOLACE_AVAILABLE = False
    logging.warning("Solace PubSub+ library not available. Using fallback messaging.")

from config.config import Config


class InMemoryMessageBus:
    """Fallback in-memory message bus when Solace is unavailable"""
    
    def __init__(self):
        self.subscribers = {}
        self.lock = threading.Lock()
        
    def subscribe(self, topic: str, handler):
        with self.lock:
            if topic not in self.subscribers:
                self.subscribers[topic] = []
            self.subscribers[topic].append(handler)
            
    def publish(self, topic: str, message: str):
        with self.lock:
            if topic in self.subscribers:
                for handler in self.subscribers[topic]:
                    try:
                        # Simulate async delivery
                        threading.Thread(target=handler, args=(message,)).start()
                    except Exception as e:
                        logging.error(f"Error delivering message to handler: {e}")


# Global fallback message bus
_fallback_bus = InMemoryMessageBus()


class BaseAgent(ABC):
    """
    Base class for all S1GNAL.ZERO agents
    Provides Solace integration with automatic fallback to in-memory messaging
    """
    
    def __init__(self, agent_type: str):
        self.agent_type = agent_type
        self.config = Config()
        self.logger = self._setup_logging()
        self.is_running = False
        self.messaging_service = None
        self.publisher = None
        self.receiver = None
        self.use_fallback = not SOLACE_AVAILABLE
        self.processing_count = 0
        self.error_count = 0
        self.start_time = datetime.now(timezone.utc)
        
        # Agent-specific configuration
        self.timeout_seconds = self.config.AGENT_TIMEOUT_SECONDS
        self.max_retries = self.config.AGENT_MAX_RETRIES
        self.retry_delay = self.config.AGENT_RETRY_DELAY_SECONDS
        
        self.logger.info(f"Initializing {agent_type} agent")
        
    def _setup_logging(self) -> logging.Logger:
        """Setup logging for the agent"""
        logger = logging.getLogger(f"signalzero.{self.agent_type}")
        logger.setLevel(getattr(logging, self.config.PYTHON_LOG_LEVEL))
        
        if not logger.handlers:
            handler = logging.StreamHandler()
            formatter = logging.Formatter(self.config.LOG_FORMAT)
            handler.setFormatter(formatter)
            logger.addHandler(handler)
            
        return logger
        
    def start(self):
        """Start the agent and connect to messaging system"""
        self.logger.info(f"Starting {self.agent_type} agent")
        
        if not self.use_fallback:
            try:
                self._connect_solace()
                self.logger.info("Connected to Solace PubSub+")
            except Exception as e:
                self.logger.warning(f"Failed to connect to Solace: {e}. Using fallback messaging.")
                self.use_fallback = True
                
        if self.use_fallback:
            self._setup_fallback_messaging()
            
        self.is_running = True
        self._start_message_processing()
        
    def _connect_solace(self):
        """Connect to Solace PubSub+ broker"""
        broker_props = {
            SolaceProperties.TransportLayerProperties.HOST: self.config.SOLACE_HOST,
            SolaceProperties.ServiceProperties.VPN_NAME: self.config.SOLACE_VPN,
            SolaceProperties.AuthenticationProperties.SCHEME_BASIC_USER_NAME: self.config.SOLACE_USERNAME,
            SolaceProperties.AuthenticationProperties.SCHEME_BASIC_PASSWORD: self.config.SOLACE_PASSWORD
        }
        
        self.messaging_service = MessagingService.builder().from_properties(broker_props).build()
        self.messaging_service.connect()
        
        # Setup publisher
        self.publisher = self.messaging_service.create_direct_message_publisher_builder().build()
        self.publisher.start()
        
        # Setup receiver
        self.receiver = self.messaging_service.create_direct_message_receiver_builder().build()
        self.receiver.start()
        
        # Subscribe to request topic
        request_topic = self._get_request_topic()
        topic_subscription = TopicSubscription.of(request_topic)
        self.receiver.add_subscription(topic_subscription)
        self.receiver.receive_async(self._handle_solace_message)
        
    def _setup_fallback_messaging(self):
        """Setup fallback in-memory messaging"""
        request_topic = self._get_request_topic()
        _fallback_bus.subscribe(request_topic, self._handle_fallback_message)
        self.logger.info(f"Subscribed to fallback topic: {request_topic}")
        
    def _get_request_topic(self) -> str:
        """Get the request topic for this agent"""
        topic_map = {
            'bot-detector': self.config.BOT_DETECTOR_REQUEST_TOPIC,
            'trend-analyzer': self.config.TREND_ANALYZER_REQUEST_TOPIC,
            'review-validator': self.config.REVIEW_VALIDATOR_REQUEST_TOPIC,
            'paid-promotion': self.config.PAID_PROMOTION_REQUEST_TOPIC,
            'score-aggregator': self.config.SCORE_AGGREGATOR_REQUEST_TOPIC
        }
        return topic_map.get(self.agent_type, f"signalzero/agent/{self.agent_type}/request")
        
    def _get_response_topic(self) -> str:
        """Get the response topic for this agent"""
        topic_map = {
            'bot-detector': self.config.BOT_DETECTOR_RESPONSE_TOPIC,
            'trend-analyzer': self.config.TREND_ANALYZER_RESPONSE_TOPIC,
            'review-validator': self.config.REVIEW_VALIDATOR_RESPONSE_TOPIC,
            'paid-promotion': self.config.PAID_PROMOTION_RESPONSE_TOPIC,
            'score-aggregator': self.config.SCORE_AGGREGATOR_RESPONSE_TOPIC
        }
        return topic_map.get(self.agent_type, f"signalzero/agent/{self.agent_type}/response")
        
    def _handle_solace_message(self, message):
        """Handle incoming Solace message"""
        try:
            payload = message.get_payload_as_string()
            self._process_message(payload)
        except Exception as e:
            self.logger.error(f"Error handling Solace message: {e}")
            self.error_count += 1
            
    def _handle_fallback_message(self, message: str):
        """Handle incoming fallback message"""
        try:
            self._process_message(message)
        except Exception as e:
            self.logger.error(f"Error handling fallback message: {e}")
            self.error_count += 1
            
    def _process_message(self, payload: str):
        """Process incoming message payload"""
        try:
            message_data = json.loads(payload)
            self.logger.debug(f"Processing message: {message_data}")
            
            start_time = time.time()
            result = self.process_analysis_request(message_data)
            processing_time = int((time.time() - start_time) * 1000)
            
            # Add metadata to result
            result.update({
                'agent_type': self.agent_type,
                'processing_time_ms': processing_time,
                'timestamp': datetime.now(timezone.utc).isoformat(),
                'agent_version': '1.0.0'
            })
            
            self._publish_response(result)
            self.processing_count += 1
            
        except json.JSONDecodeError as e:
            self.logger.error(f"Invalid JSON payload: {e}")
            self.error_count += 1
        except Exception as e:
            self.logger.error(f"Error processing message: {e}")
            self.error_count += 1
            # Send error response
            self._publish_error_response(str(e))
            
    def _publish_response(self, result: Dict[str, Any]):
        """Publish response to appropriate topic"""
        response_topic = self._get_response_topic()
        response_payload = json.dumps(result)
        
        if not self.use_fallback and self.publisher:
            try:
                topic = Topic.of(response_topic)
                message = self.messaging_service.message_builder().build(response_payload)
                self.publisher.publish(message, topic)
                self.logger.debug(f"Published response to Solace topic: {response_topic}")
            except Exception as e:
                self.logger.error(f"Failed to publish to Solace: {e}")
                # Fallback to in-memory
                _fallback_bus.publish(response_topic, response_payload)
        else:
            _fallback_bus.publish(response_topic, response_payload)
            self.logger.debug(f"Published response to fallback topic: {response_topic}")
            
    def _publish_error_response(self, error_message: str):
        """Publish error response"""
        error_response = {
            'agent_type': self.agent_type,
            'status': 'ERROR',
            'error_message': error_message,
            'timestamp': datetime.now(timezone.utc).isoformat(),
            'score': 50.0,  # Neutral score on error
            'confidence': 0.0
        }
        self._publish_response(error_response)
        
    def _start_message_processing(self):
        """Start message processing loop"""
        def processing_loop():
            while self.is_running:
                try:
                    time.sleep(1)  # Heartbeat
                except KeyboardInterrupt:
                    self.logger.info("Received shutdown signal")
                    self.stop()
                    break
                except Exception as e:
                    self.logger.error(f"Error in processing loop: {e}")
                    
        processing_thread = threading.Thread(target=processing_loop, daemon=True)
        processing_thread.start()
        
    def stop(self):
        """Stop the agent and cleanup resources"""
        self.logger.info(f"Stopping {self.agent_type} agent")
        self.is_running = False
        
        if self.receiver:
            self.receiver.terminate()
        if self.publisher:
            self.publisher.terminate()
        if self.messaging_service:
            self.messaging_service.disconnect()
            
    def get_status(self) -> Dict[str, Any]:
        """Get agent status information"""
        uptime = (datetime.now(timezone.utc) - self.start_time).total_seconds()
        return {
            'agent_type': self.agent_type,
            'is_running': self.is_running,
            'uptime_seconds': uptime,
            'processing_count': self.processing_count,
            'error_count': self.error_count,
            'error_rate': self.error_count / max(self.processing_count, 1),
            'using_fallback': self.use_fallback
        }
        
    @abstractmethod
    def process_analysis_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Process analysis request - must be implemented by subclasses
        
        Args:
            request_data: Dictionary containing analysis request
            
        Returns:
            Dictionary containing analysis results with required fields:
            - analysisId: UUID of the analysis
            - score: Numeric score (0-100)
            - confidence: Confidence level (0-100)
            - evidence: Dictionary with supporting evidence
            - data_sources: List of data sources used
        """
        pass
        
    def create_standard_response(self, analysis_id: str, score: float, confidence: float, 
                               evidence: Dict[str, Any], data_sources: List[str]) -> Dict[str, Any]:
        """Create standardized response format"""
        return {
            'analysisId': analysis_id,
            'score': round(score, 2),
            'confidence': round(confidence, 2),
            'evidence': evidence,
            'data_sources': data_sources,
            'status': 'COMPLETE'
        }
        
    def get_hardcoded_demo_response(self, query: str, analysis_id: str) -> Optional[Dict[str, Any]]:
        """Get hardcoded response for demo queries"""
        query_lower = query.lower()
        
        # Stanley Cup demo
        if "stanley cup" in query_lower:
            return self._create_demo_response(
                analysis_id, query, 
                self.config.STANLEY_CUP_BOT_PERCENTAGE,
                self.config.STANLEY_CUP_REALITY_SCORE
            )
            
        # $BUZZ stock demo
        if "$buzz" in query_lower or "buzz stock" in query_lower:
            return self._create_demo_response(
                analysis_id, query,
                self.config.BUZZ_STOCK_BOT_PERCENTAGE,
                self.config.BUZZ_STOCK_REALITY_SCORE
            )
            
        # Prime Energy demo
        if "prime energy" in query_lower:
            return self._create_demo_response(
                analysis_id, query,
                self.config.PRIME_ENERGY_BOT_PERCENTAGE,
                self.config.PRIME_ENERGY_REALITY_SCORE
            )
            
        return None
        
    def _create_demo_response(self, analysis_id: str, query: str, bot_percentage: int, reality_score: int) -> Dict[str, Any]:
        """Create demo response with agent-specific evidence"""
        # Add small variance for realism
        if self.config.DEMO_ADD_REALISTIC_VARIANCE:
            import random
            bot_percentage += random.randint(-2, 2)
            reality_score += random.randint(-2, 2)
            
        # Ensure bounds
        bot_percentage = max(0, min(100, bot_percentage))
        reality_score = max(0, min(100, reality_score))
        
        # Agent-specific score calculation
        if self.agent_type == 'bot-detector':
            score = 100 - bot_percentage  # Lower bot percentage = higher authenticity
        elif self.agent_type == 'score-aggregator':
            score = reality_score
        else:
            # For other agents, derive score from reality score
            score = reality_score + random.randint(-5, 5) if self.config.DEMO_ADD_REALISTIC_VARIANCE else reality_score
            
        evidence = self._get_demo_evidence(query, bot_percentage, reality_score)
        
        return self.create_standard_response(
            analysis_id, score, self.config.DEMO_CONFIDENCE_SCORE,
            evidence, ['demo_data', 'hardcoded_values']
        )
        
    @abstractmethod
    def _get_demo_evidence(self, query: str, bot_percentage: int, reality_score: int) -> Dict[str, Any]:
        """Get agent-specific demo evidence - must be implemented by subclasses"""
        pass
