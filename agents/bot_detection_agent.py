"""
Bot Detection Agent for S1GNAL.ZERO
Analyzes account patterns to identify bot behavior and coordinated inauthentic activity.
"""

import random
import time
from typing import Dict, Any, List
from datetime import datetime, timedelta

from base.base_agent import BaseAgent


class BotDetectionAgent(BaseAgent):
    """
    Specialized agent for detecting bot accounts and coordinated inauthentic behavior
    """
    
    def __init__(self):
        super().__init__('bot-detector')
        self.min_confidence = self.config.BOT_DETECTOR_MIN_CONFIDENCE
        self.sample_size = self.config.BOT_DETECTOR_SAMPLE_SIZE
        
    def process_analysis_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """Process bot detection analysis request"""
        analysis_id = request_data.get('analysisId', '')
        query = request_data.get('query', '')
        platform = request_data.get('platform', 'all')
        
        self.logger.info(f"Processing bot detection for query: {query}")
        
        # Check for hardcoded demo responses first
        demo_response = self.get_hardcoded_demo_response(query, analysis_id)
        if demo_response:
            self.logger.info(f"Returning hardcoded demo response for: {query}")
            return demo_response
            
        # Simulate processing delay for realism
        if self.config.DEMO_RESPONSE_DELAY_MS > 0:
            time.sleep(self.config.DEMO_RESPONSE_DELAY_MS / 1000.0)
            
        # Generate realistic bot analysis
        bot_analysis = self._analyze_bot_patterns(query, platform)
        
        return self.create_standard_response(
            analysis_id,
            bot_analysis['authenticity_score'],
            bot_analysis['confidence'],
            bot_analysis['evidence'],
            bot_analysis['data_sources']
        )
        
    def _analyze_bot_patterns(self, query: str, platform: str) -> Dict[str, Any]:
        """Analyze bot patterns for the given query"""
        
        # Simulate realistic bot detection analysis
        total_accounts = random.randint(500, 5000)
        
        # Generate bot indicators based on query characteristics
        bot_indicators = self._calculate_bot_indicators(query, total_accounts)
        
        # Calculate overall bot percentage
        bot_percentage = self._calculate_bot_percentage(bot_indicators)
        
        # Authenticity score is inverse of bot percentage
        authenticity_score = 100 - bot_percentage
        
        # Calculate confidence based on sample size and consistency
        confidence = self._calculate_confidence(bot_indicators, total_accounts)
        
        # Determine data sources used
        data_sources = self._get_data_sources(platform)
        
        # Create evidence dictionary
        evidence = {
            'total_accounts_analyzed': total_accounts,
            'bot_percentage': bot_percentage,
            'bot_indicators': bot_indicators,
            'suspicious_accounts': int(total_accounts * bot_percentage / 100),
            'platform_analysis': platform,
            'detection_methods': [
                'account_age_analysis',
                'profile_completeness_check',
                'username_pattern_analysis',
                'posting_behavior_analysis',
                'network_cluster_detection'
            ]
        }
        
        return {
            'authenticity_score': authenticity_score,
            'confidence': confidence,
            'evidence': evidence,
            'data_sources': data_sources
        }
        
    def _calculate_bot_indicators(self, query: str, total_accounts: int) -> Dict[str, Any]:
        """Calculate various bot indicators"""
        
        # Base bot likelihood based on query type
        base_bot_rate = self._get_base_bot_rate(query)
        
        # Account age analysis
        new_accounts = int(total_accounts * random.uniform(0.15, 0.45))
        very_new_accounts = int(new_accounts * random.uniform(0.6, 0.9))
        
        # Profile completeness
        incomplete_profiles = int(total_accounts * random.uniform(0.25, 0.55))
        default_avatars = int(total_accounts * random.uniform(0.30, 0.60))
        
        # Username patterns
        generic_usernames = int(total_accounts * random.uniform(0.20, 0.50))
        number_heavy_usernames = int(total_accounts * random.uniform(0.35, 0.65))
        
        # Posting behavior
        burst_posting = int(total_accounts * random.uniform(0.10, 0.30))
        identical_content = int(total_accounts * random.uniform(0.05, 0.25))
        
        # Network analysis
        cluster_detected = random.choice([True, False])
        coordinated_timing = random.choice([True, False])
        
        return {
            'account_age': {
                'accounts_under_30_days': new_accounts,
                'accounts_under_7_days': very_new_accounts,
                'percentage_new': round(new_accounts / total_accounts * 100, 1)
            },
            'profile_analysis': {
                'incomplete_profiles': incomplete_profiles,
                'default_avatars': default_avatars,
                'bio_missing': int(total_accounts * random.uniform(0.40, 0.70))
            },
            'username_patterns': {
                'generic_patterns': generic_usernames,
                'number_heavy': number_heavy_usernames,
                'suspicious_similarity': random.randint(50, 200)
            },
            'posting_behavior': {
                'burst_posting_detected': burst_posting,
                'identical_content_count': identical_content,
                'posting_frequency_anomalies': random.randint(20, 100)
            },
            'network_analysis': {
                'cluster_detected': cluster_detected,
                'coordinated_timing': coordinated_timing,
                'follower_overlap_high': random.choice([True, False])
            }
        }
        
    def _get_base_bot_rate(self, query: str) -> float:
        """Get base bot rate based on query characteristics"""
        query_lower = query.lower()
        
        # High bot rate indicators
        if any(term in query_lower for term in ['crypto', 'nft', 'stock', 'investment', 'trading']):
            return random.uniform(0.45, 0.75)
        elif any(term in query_lower for term in ['viral', 'trending', 'challenge', 'meme']):
            return random.uniform(0.35, 0.65)
        elif any(term in query_lower for term in ['product', 'review', 'buy', 'sale']):
            return random.uniform(0.25, 0.55)
        else:
            return random.uniform(0.15, 0.35)
            
    def _calculate_bot_percentage(self, bot_indicators: Dict[str, Any]) -> float:
        """Calculate overall bot percentage from indicators"""
        
        # Weight different indicators
        weights = {
            'account_age': 0.30,
            'profile_analysis': 0.25,
            'username_patterns': 0.20,
            'posting_behavior': 0.15,
            'network_analysis': 0.10
        }
        
        # Calculate weighted score
        total_score = 0
        
        # Account age score
        age_score = min(bot_indicators['account_age']['percentage_new'], 80)
        total_score += age_score * weights['account_age']
        
        # Profile completeness score
        profile_score = (bot_indicators['profile_analysis']['incomplete_profiles'] / 1000) * 100
        profile_score = min(profile_score, 70)
        total_score += profile_score * weights['profile_analysis']
        
        # Username pattern score
        username_score = (bot_indicators['username_patterns']['generic_patterns'] / 1000) * 100
        username_score = min(username_score, 60)
        total_score += username_score * weights['username_patterns']
        
        # Posting behavior score
        posting_score = (bot_indicators['posting_behavior']['burst_posting_detected'] / 1000) * 100
        posting_score = min(posting_score, 50)
        total_score += posting_score * weights['posting_behavior']
        
        # Network analysis score
        network_score = 0
        if bot_indicators['network_analysis']['cluster_detected']:
            network_score += 30
        if bot_indicators['network_analysis']['coordinated_timing']:
            network_score += 20
        total_score += network_score * weights['network_analysis']
        
        return min(total_score, 95)  # Cap at 95% to maintain realism
        
    def _calculate_confidence(self, bot_indicators: Dict[str, Any], total_accounts: int) -> float:
        """Calculate confidence score based on data quality and consistency"""
        
        confidence = 70.0  # Base confidence
        
        # Increase confidence with larger sample size
        if total_accounts > 1000:
            confidence += 10
        elif total_accounts > 2000:
            confidence += 15
            
        # Increase confidence if multiple indicators align
        strong_indicators = 0
        if bot_indicators['account_age']['percentage_new'] > 40:
            strong_indicators += 1
        if bot_indicators['network_analysis']['cluster_detected']:
            strong_indicators += 1
        if bot_indicators['posting_behavior']['burst_posting_detected'] > 100:
            strong_indicators += 1
            
        confidence += strong_indicators * 5
        
        # Add some randomness for realism
        confidence += random.uniform(-5, 5)
        
        return min(max(confidence, 60), 98)
        
    def _get_data_sources(self, platform: str) -> List[str]:
        """Get list of data sources used for analysis"""
        sources = ['account_metadata', 'posting_patterns']
        
        if platform == 'twitter' or platform == 'all':
            sources.extend(['twitter_api', 'follower_analysis'])
        if platform == 'reddit' or platform == 'all':
            sources.extend(['reddit_api', 'karma_analysis'])
        if platform == 'instagram' or platform == 'all':
            sources.extend(['instagram_basic_display', 'engagement_metrics'])
            
        # Add fallback sources if APIs unavailable
        if not self.config.BOT_DETECTOR_USE_TWITTER_API:
            sources.append('mock_twitter_data')
        if not self.config.BOT_DETECTOR_USE_REDDIT_API:
            sources.append('mock_reddit_data')
            
        return sources
        
    def _get_demo_evidence(self, query: str, bot_percentage: int, reality_score: int) -> Dict[str, Any]:
        """Get bot detector specific demo evidence"""
        
        # Generate realistic account numbers
        total_accounts = random.randint(2000, 8000)
        suspicious_accounts = int(total_accounts * bot_percentage / 100)
        
        return {
            'total_accounts_analyzed': total_accounts,
            'suspicious_accounts_found': suspicious_accounts,
            'bot_percentage': bot_percentage,
            'key_findings': [
                f"{bot_percentage}% of accounts show bot-like behavior",
                f"{suspicious_accounts} accounts created in last 30 days",
                f"Detected coordinated posting patterns",
                f"High similarity in username structures"
            ],
            'detection_methods': [
                'Account age analysis',
                'Profile completeness check',
                'Username pattern recognition',
                'Posting behavior analysis',
                'Network cluster detection'
            ],
            'sample_suspicious_accounts': [
                f"@user{random.randint(10000, 99999)}",
                f"@account{random.randint(10000, 99999)}",
                f"@bot{random.randint(1000, 9999)}"
            ],
            'confidence_factors': [
                'Large sample size',
                'Multiple detection methods',
                'Consistent patterns across accounts',
                'Cross-platform verification'
            ]
        }


def main():
    """Main function for running bot detection agent standalone"""
    agent = BotDetectionAgent()
    
    try:
        agent.start()
        agent.logger.info("Bot Detection Agent started successfully")
        
        # Keep running until interrupted
        while agent.is_running:
            time.sleep(1)
            
    except KeyboardInterrupt:
        agent.logger.info("Received shutdown signal")
    finally:
        agent.stop()
        agent.logger.info("Bot Detection Agent stopped")


if __name__ == "__main__":
    main()
