"""
S1GNAL.ZERO Agent Configuration
Loads configuration from environment variables and provides centralized config access.
"""

import os
from dotenv import load_dotenv
from typing import Optional

# Load environment variables from .env file
load_dotenv()

class Config:
    """Centralized configuration for all agents"""
    
    # Solace PubSub+ Configuration
    SOLACE_HOST = os.getenv('SOLACE_HOST', 'tcp://localhost:55555')
    SOLACE_VPN = os.getenv('SOLACE_VPN', 'default')
    SOLACE_USERNAME = os.getenv('SOLACE_USERNAME', 'admin')
    SOLACE_PASSWORD = os.getenv('SOLACE_PASSWORD', 'admin')
    SOLACE_CLIENT_NAME_PREFIX = os.getenv('SOLACE_CLIENT_NAME_PREFIX', 'signalzero-agent')
    
    # Agent Configuration
    PYTHON_LOG_LEVEL = os.getenv('PYTHON_LOG_LEVEL', 'INFO')
    USE_MOCK_DATA = os.getenv('USE_MOCK_DATA', 'true').lower() == 'true'
    DEMO_MODE = os.getenv('DEMO_MODE', 'true').lower() == 'true'
    
    # Agent Timeouts and Retries
    AGENT_TIMEOUT_SECONDS = int(os.getenv('AGENT_TIMEOUT_SECONDS', '5'))
    AGENT_MAX_RETRIES = int(os.getenv('AGENT_MAX_RETRIES', '3'))
    AGENT_RETRY_DELAY_SECONDS = int(os.getenv('AGENT_RETRY_DELAY_SECONDS', '1'))
    AGENT_HEARTBEAT_INTERVAL = int(os.getenv('AGENT_HEARTBEAT_INTERVAL', '30'))
    
    # Concurrency Settings
    MAX_CONCURRENT_ANALYSES = int(os.getenv('MAX_CONCURRENT_ANALYSES', '5'))
    AGENT_WORKER_THREADS = int(os.getenv('AGENT_WORKER_THREADS', '3'))
    MESSAGE_BATCH_SIZE = int(os.getenv('MESSAGE_BATCH_SIZE', '10'))
    
    # External API Keys (Optional)
    REDDIT_CLIENT_ID = os.getenv('REDDIT_CLIENT_ID')
    REDDIT_CLIENT_SECRET = os.getenv('REDDIT_CLIENT_SECRET')
    REDDIT_USER_AGENT = os.getenv('REDDIT_USER_AGENT', 'python:s1gnal.zero:v1.0 (by u/S1GNAL-ZERO)')
    
    YOUTUBE_API_KEY = os.getenv('YOUTUBE_API_KEY')
    NEWSAPI_KEY = os.getenv('NEWSAPI_KEY')
    TWITTER_BEARER_TOKEN = os.getenv('TWITTER_BEARER_TOKEN')
    
    # Agent-Specific Configuration
    BOT_DETECTOR_ENABLED = os.getenv('BOT_DETECTOR_ENABLED', 'true').lower() == 'true'
    BOT_DETECTOR_MIN_CONFIDENCE = float(os.getenv('BOT_DETECTOR_MIN_CONFIDENCE', '0.7'))
    BOT_DETECTOR_SAMPLE_SIZE = int(os.getenv('BOT_DETECTOR_SAMPLE_SIZE', '1000'))
    BOT_DETECTOR_USE_TWITTER_API = os.getenv('BOT_DETECTOR_USE_TWITTER_API', 'false').lower() == 'true'
    BOT_DETECTOR_USE_REDDIT_API = os.getenv('BOT_DETECTOR_USE_REDDIT_API', 'false').lower() == 'true'
    
    TREND_ANALYZER_ENABLED = os.getenv('TREND_ANALYZER_ENABLED', 'true').lower() == 'true'
    TREND_ANALYZER_TIME_WINDOW_HOURS = int(os.getenv('TREND_ANALYZER_TIME_WINDOW_HOURS', '168'))
    TREND_ANALYZER_SPIKE_THRESHOLD = int(os.getenv('TREND_ANALYZER_SPIKE_THRESHOLD', '500'))
    TREND_ANALYZER_USE_GOOGLE_TRENDS = os.getenv('TREND_ANALYZER_USE_GOOGLE_TRENDS', 'true').lower() == 'true'
    TREND_ANALYZER_USE_NEWS_API = os.getenv('TREND_ANALYZER_USE_NEWS_API', 'false').lower() == 'true'
    
    REVIEW_VALIDATOR_ENABLED = os.getenv('REVIEW_VALIDATOR_ENABLED', 'true').lower() == 'true'
    REVIEW_VALIDATOR_MIN_REVIEWS = int(os.getenv('REVIEW_VALIDATOR_MIN_REVIEWS', '10'))
    REVIEW_VALIDATOR_TEMPLATE_THRESHOLD = float(os.getenv('REVIEW_VALIDATOR_TEMPLATE_THRESHOLD', '0.8'))
    REVIEW_VALIDATOR_USE_SCRAPING = os.getenv('REVIEW_VALIDATOR_USE_SCRAPING', 'false').lower() == 'true'
    REVIEW_VALIDATOR_USE_REVIEWMETA = os.getenv('REVIEW_VALIDATOR_USE_REVIEWMETA', 'false').lower() == 'true'
    
    PAID_PROMOTION_ENABLED = os.getenv('PAID_PROMOTION_ENABLED', 'true').lower() == 'true'
    PAID_PROMOTION_KEYWORD_THRESHOLD = int(os.getenv('PAID_PROMOTION_KEYWORD_THRESHOLD', '5'))
    PAID_PROMOTION_USE_YOUTUBE_API = os.getenv('PAID_PROMOTION_USE_YOUTUBE_API', 'false').lower() == 'true'
    PAID_PROMOTION_FTC_COMPLIANCE_CHECK = os.getenv('PAID_PROMOTION_FTC_COMPLIANCE_CHECK', 'true').lower() == 'true'
    
    SCORE_AGGREGATOR_ENABLED = os.getenv('SCORE_AGGREGATOR_ENABLED', 'true').lower() == 'true'
    
    # Score Aggregator Weights (EXACT from DETAILED_DESIGN.md Section 9.2)
    SCORE_AGGREGATOR_BOT_WEIGHT = float(os.getenv('SCORE_AGGREGATOR_BOT_WEIGHT', '0.40'))
    SCORE_AGGREGATOR_TREND_WEIGHT = float(os.getenv('SCORE_AGGREGATOR_TREND_WEIGHT', '0.30'))
    SCORE_AGGREGATOR_REVIEW_WEIGHT = float(os.getenv('SCORE_AGGREGATOR_REVIEW_WEIGHT', '0.20'))
    SCORE_AGGREGATOR_PROMOTION_WEIGHT = float(os.getenv('SCORE_AGGREGATOR_PROMOTION_WEIGHT', '0.10'))
    
    # Hardcoded Demo Values (EXACT from requirements)
    STANLEY_CUP_BOT_PERCENTAGE = int(os.getenv('STANLEY_CUP_BOT_PERCENTAGE', '62'))
    STANLEY_CUP_REALITY_SCORE = int(os.getenv('STANLEY_CUP_REALITY_SCORE', '34'))
    
    BUZZ_STOCK_BOT_PERCENTAGE = int(os.getenv('BUZZ_STOCK_BOT_PERCENTAGE', '87'))
    BUZZ_STOCK_REALITY_SCORE = int(os.getenv('BUZZ_STOCK_REALITY_SCORE', '12'))
    
    PRIME_ENERGY_BOT_PERCENTAGE = int(os.getenv('PRIME_ENERGY_BOT_PERCENTAGE', '71'))
    PRIME_ENERGY_REALITY_SCORE = int(os.getenv('PRIME_ENERGY_REALITY_SCORE', '29'))
    
    # Demo Mode Settings
    DEMO_RESPONSE_DELAY_MS = int(os.getenv('DEMO_RESPONSE_DELAY_MS', '800'))
    DEMO_ADD_REALISTIC_VARIANCE = os.getenv('DEMO_ADD_REALISTIC_VARIANCE', 'true').lower() == 'true'
    DEMO_CONFIDENCE_SCORE = float(os.getenv('DEMO_CONFIDENCE_SCORE', '94.5'))
    
    # Logging Configuration
    LOG_FORMAT = os.getenv('LOG_FORMAT', '%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    ENABLE_CONSOLE_LOGGING = os.getenv('ENABLE_CONSOLE_LOGGING', 'true').lower() == 'true'
    
    # Error Handling
    CONTINUE_ON_AGENT_FAILURE = os.getenv('CONTINUE_ON_AGENT_FAILURE', 'true').lower() == 'true'
    FALLBACK_TO_MOCK_ON_ERROR = os.getenv('FALLBACK_TO_MOCK_ON_ERROR', 'true').lower() == 'true'
    
    # Solace Topics (EXACT from DETAILED_DESIGN.md Section 5.1)
    TOPICS = {
        'BOT_DETECTOR_REQUEST': 'signalzero/agent/bot-detector/request',
        'BOT_DETECTOR_RESPONSE': 'signalzero/agent/bot-detector/response',
        'TREND_ANALYZER_REQUEST': 'signalzero/agent/trend-analyzer/request',
        'TREND_ANALYZER_RESPONSE': 'signalzero/agent/trend-analyzer/response',
        'REVIEW_VALIDATOR_REQUEST': 'signalzero/agent/review-validator/request',
        'REVIEW_VALIDATOR_RESPONSE': 'signalzero/agent/review-validator/response',
        'PROMOTION_DETECTOR_REQUEST': 'signalzero/agent/promotion-detector/request',
        'PROMOTION_DETECTOR_RESPONSE': 'signalzero/agent/promotion-detector/response',
        'SCORE_AGGREGATOR_REQUEST': 'signalzero/agent/score-aggregator/request',
        'SCORE_AGGREGATOR_RESPONSE': 'signalzero/agent/score-aggregator/response',
        'ANALYSIS_REQUEST': 'signalzero/analysis/request',
        'ANALYSIS_RESPONSE': 'signalzero/analysis/response',
        'UPDATES_SCORE': 'signalzero/updates/score',
        'UPDATES_STATUS': 'signalzero/updates/status'
    }
    
    @classmethod
    def get_topic(cls, topic_name: str) -> str:
        """Get topic name by key"""
        return cls.TOPICS.get(topic_name, '')
    
    @classmethod
    def validate_config(cls) -> bool:
        """Validate critical configuration values"""
        required_configs = [
            cls.SOLACE_HOST,
            cls.SOLACE_USERNAME,
            cls.SOLACE_PASSWORD
        ]
        
        return all(config for config in required_configs)
    
    @classmethod
    def get_solace_connection_params(cls) -> dict:
        """Get Solace connection parameters"""
        return {
            'host': cls.SOLACE_HOST,
            'vpn': cls.SOLACE_VPN,
            'username': cls.SOLACE_USERNAME,
            'password': cls.SOLACE_PASSWORD,
            'client_name_prefix': cls.SOLACE_CLIENT_NAME_PREFIX
        }
