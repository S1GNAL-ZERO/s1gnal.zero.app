"""
Trend Analysis Agent for S1GNAL.ZERO
Detects abnormal growth patterns and viral velocity to identify manufactured trends.
"""

import random
import time
from typing import Dict, Any, List
from datetime import datetime, timedelta

from base.base_agent import BaseAgent


class TrendAnalysisAgent(BaseAgent):
    """
    Specialized agent for analyzing trend patterns and detecting artificial viral spikes
    """
    
    def __init__(self):
        super().__init__('trend-analyzer')
        self.time_window_hours = self.config.TREND_ANALYZER_TIME_WINDOW_HOURS
        self.spike_threshold = self.config.TREND_ANALYZER_SPIKE_THRESHOLD
        
    def process_analysis_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """Process trend analysis request"""
        analysis_id = request_data.get('analysisId', '')
        query = request_data.get('query', '')
        platform = request_data.get('platform', 'all')
        
        self.logger.info(f"Processing trend analysis for query: {query}")
        
        # Check for hardcoded demo responses first
        demo_response = self.get_hardcoded_demo_response(query, analysis_id)
        if demo_response:
            self.logger.info(f"Returning hardcoded demo response for: {query}")
            return demo_response
            
        # Simulate processing delay for realism
        if self.config.DEMO_RESPONSE_DELAY_MS > 0:
            time.sleep(self.config.DEMO_RESPONSE_DELAY_MS / 1000.0)
            
        # Generate realistic trend analysis
        trend_analysis = self._analyze_trend_patterns(query, platform)
        
        return self.create_standard_response(
            analysis_id,
            trend_analysis['organic_score'],
            trend_analysis['confidence'],
            trend_analysis['evidence'],
            trend_analysis['data_sources']
        )
        
    def _analyze_trend_patterns(self, query: str, platform: str) -> Dict[str, Any]:
        """Analyze trend patterns for the given query"""
        
        # Generate trend data points
        trend_data = self._generate_trend_data(query)
        
        # Analyze velocity and growth patterns
        velocity_analysis = self._analyze_velocity(trend_data)
        
        # Detect artificial spikes
        spike_analysis = self._detect_artificial_spikes(trend_data)
        
        # Cross-platform correlation analysis
        platform_analysis = self._analyze_cross_platform_patterns(query, platform)
        
        # Calculate organic probability
        organic_score = self._calculate_organic_score(velocity_analysis, spike_analysis, platform_analysis)
        
        # Calculate confidence
        confidence = self._calculate_confidence(trend_data, velocity_analysis)
        
        # Determine data sources
        data_sources = self._get_data_sources(platform)
        
        # Create evidence dictionary
        evidence = {
            'trend_data_points': len(trend_data),
            'velocity_analysis': velocity_analysis,
            'spike_analysis': spike_analysis,
            'platform_analysis': platform_analysis,
            'time_window_analyzed': f"{self.time_window_hours} hours",
            'organic_indicators': self._get_organic_indicators(velocity_analysis, spike_analysis),
            'manipulation_indicators': self._get_manipulation_indicators(velocity_analysis, spike_analysis)
        }
        
        return {
            'organic_score': organic_score,
            'confidence': confidence,
            'evidence': evidence,
            'data_sources': data_sources
        }
        
    def _generate_trend_data(self, query: str) -> List[Dict[str, Any]]:
        """Generate realistic trend data points"""
        
        # Determine trend pattern based on query
        pattern_type = self._determine_trend_pattern(query)
        
        # Generate hourly data points for the time window
        data_points = []
        base_volume = random.randint(100, 1000)
        
        for hour in range(self.time_window_hours):
            timestamp = datetime.now() - timedelta(hours=self.time_window_hours - hour)
            
            if pattern_type == 'artificial_spike':
                volume = self._generate_artificial_spike_volume(hour, base_volume)
            elif pattern_type == 'organic_growth':
                volume = self._generate_organic_growth_volume(hour, base_volume)
            elif pattern_type == 'coordinated_campaign':
                volume = self._generate_coordinated_campaign_volume(hour, base_volume)
            else:
                volume = self._generate_natural_volume(hour, base_volume)
                
            data_points.append({
                'timestamp': timestamp.isoformat(),
                'volume': volume,
                'mentions': random.randint(int(volume * 0.1), int(volume * 0.3)),
                'engagement_rate': random.uniform(0.02, 0.15),
                'unique_users': random.randint(int(volume * 0.6), int(volume * 0.9))
            })
            
        return data_points
        
    def _determine_trend_pattern(self, query: str) -> str:
        """Determine the type of trend pattern based on query"""
        query_lower = query.lower()
        
        # Viral/manufactured content indicators
        if any(term in query_lower for term in ['viral', 'challenge', 'trending', 'meme']):
            return random.choice(['artificial_spike', 'coordinated_campaign'])
        elif any(term in query_lower for term in ['product', 'launch', 'brand', 'company']):
            return random.choice(['coordinated_campaign', 'organic_growth'])
        elif any(term in query_lower for term in ['stock', 'crypto', 'investment']):
            return 'artificial_spike'
        else:
            return random.choice(['organic_growth', 'natural'])
            
    def _generate_artificial_spike_volume(self, hour: int, base_volume: int) -> int:
        """Generate volume pattern for artificial spike"""
        # Sudden spike followed by rapid decline
        if 48 <= hour <= 72:  # Spike period
            multiplier = random.uniform(5, 15)
        elif 72 < hour <= 96:  # Decline period
            multiplier = random.uniform(2, 4)
        else:
            multiplier = random.uniform(0.5, 1.5)
            
        return int(base_volume * multiplier)
        
    def _generate_organic_growth_volume(self, hour: int, base_volume: int) -> int:
        """Generate volume pattern for organic growth"""
        # Gradual increase with natural fluctuations
        growth_factor = 1 + (hour / self.time_window_hours) * 2
        noise = random.uniform(0.8, 1.2)
        return int(base_volume * growth_factor * noise)
        
    def _generate_coordinated_campaign_volume(self, hour: int, base_volume: int) -> int:
        """Generate volume pattern for coordinated campaign"""
        # Multiple smaller spikes at regular intervals
        if hour % 24 in [9, 13, 17, 21]:  # Peak posting times
            multiplier = random.uniform(3, 6)
        else:
            multiplier = random.uniform(0.8, 1.5)
            
        return int(base_volume * multiplier)
        
    def _generate_natural_volume(self, hour: int, base_volume: int) -> int:
        """Generate natural volume pattern"""
        # Natural daily cycles with random variation
        daily_cycle = 1 + 0.3 * abs(12 - (hour % 24)) / 12
        noise = random.uniform(0.7, 1.3)
        return int(base_volume * daily_cycle * noise)
        
    def _analyze_velocity(self, trend_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze trend velocity and growth patterns"""
        
        if len(trend_data) < 2:
            return {'error': 'Insufficient data for velocity analysis'}
            
        volumes = [point['volume'] for point in trend_data]
        
        # Calculate growth rates
        growth_rates = []
        for i in range(1, len(volumes)):
            if volumes[i-1] > 0:
                growth_rate = (volumes[i] - volumes[i-1]) / volumes[i-1] * 100
                growth_rates.append(growth_rate)
                
        # Find maximum growth rate
        max_growth_rate = max(growth_rates) if growth_rates else 0
        
        # Calculate average growth rate
        avg_growth_rate = sum(growth_rates) / len(growth_rates) if growth_rates else 0
        
        # Find peak volume and time to peak
        max_volume = max(volumes)
        max_volume_index = volumes.index(max_volume)
        time_to_peak = max_volume_index
        
        # Calculate velocity score (higher = more suspicious)
        velocity_score = min(max_growth_rate / 10, 100)
        
        return {
            'max_growth_rate_percent': round(max_growth_rate, 2),
            'avg_growth_rate_percent': round(avg_growth_rate, 2),
            'peak_volume': max_volume,
            'time_to_peak_hours': time_to_peak,
            'velocity_score': round(velocity_score, 2),
            'growth_pattern': self._classify_growth_pattern(growth_rates),
            'volatility': round(self._calculate_volatility(volumes), 2)
        }
        
    def _classify_growth_pattern(self, growth_rates: List[float]) -> str:
        """Classify the growth pattern"""
        if not growth_rates:
            return 'insufficient_data'
            
        max_growth = max(growth_rates)
        avg_growth = sum(growth_rates) / len(growth_rates)
        
        if max_growth > 500:
            return 'explosive_spike'
        elif max_growth > 200:
            return 'rapid_growth'
        elif avg_growth > 50:
            return 'sustained_growth'
        elif avg_growth > 0:
            return 'gradual_growth'
        else:
            return 'declining'
            
    def _calculate_volatility(self, volumes: List[int]) -> float:
        """Calculate volatility of the trend"""
        if len(volumes) < 2:
            return 0
            
        mean_volume = sum(volumes) / len(volumes)
        variance = sum((v - mean_volume) ** 2 for v in volumes) / len(volumes)
        return (variance ** 0.5) / mean_volume * 100 if mean_volume > 0 else 0
        
    def _detect_artificial_spikes(self, trend_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Detect artificial spikes in trend data"""
        
        volumes = [point['volume'] for point in trend_data]
        
        # Find spikes (volume > threshold * average)
        avg_volume = sum(volumes) / len(volumes) if volumes else 0
        threshold_multiplier = 3.0
        
        spikes = []
        for i, volume in enumerate(volumes):
            if volume > avg_volume * threshold_multiplier:
                spikes.append({
                    'hour': i,
                    'volume': volume,
                    'multiplier': round(volume / avg_volume, 2) if avg_volume > 0 else 0
                })
                
        # Analyze spike characteristics
        spike_analysis = {
            'spikes_detected': len(spikes),
            'spike_details': spikes,
            'avg_volume': round(avg_volume, 2),
            'threshold_used': threshold_multiplier,
            'artificial_probability': self._calculate_artificial_probability(spikes, volumes)
        }
        
        return spike_analysis
        
    def _calculate_artificial_probability(self, spikes: List[Dict], volumes: List[int]) -> float:
        """Calculate probability that spikes are artificial"""
        if not spikes:
            return 0.0
            
        # Factors that increase artificial probability
        probability = 0.0
        
        # Multiple spikes in short time
        if len(spikes) > 2:
            probability += 30.0
            
        # Very high spike multipliers
        max_multiplier = max(spike['multiplier'] for spike in spikes)
        if max_multiplier > 10:
            probability += 40.0
        elif max_multiplier > 5:
            probability += 25.0
            
        # Rapid decline after spike
        for spike in spikes:
            spike_hour = spike['hour']
            if spike_hour < len(volumes) - 2:
                decline_rate = (volumes[spike_hour] - volumes[spike_hour + 2]) / volumes[spike_hour]
                if decline_rate > 0.7:  # 70% decline
                    probability += 20.0
                    
        return min(probability, 95.0)
        
    def _analyze_cross_platform_patterns(self, query: str, platform: str) -> Dict[str, Any]:
        """Analyze cross-platform correlation patterns"""
        
        # Simulate cross-platform data
        platforms = ['twitter', 'reddit', 'instagram', 'tiktok', 'youtube']
        if platform != 'all':
            platforms = [platform]
            
        platform_data = {}
        for p in platforms:
            # Generate platform-specific metrics
            platform_data[p] = {
                'volume': random.randint(100, 2000),
                'growth_rate': random.uniform(-20, 300),
                'engagement_rate': random.uniform(0.01, 0.20),
                'unique_users': random.randint(50, 1500),
                'timing_correlation': random.uniform(0.3, 0.95)
            }
            
        # Calculate cross-platform correlation
        correlation_score = self._calculate_platform_correlation(platform_data)
        
        return {
            'platforms_analyzed': list(platform_data.keys()),
            'platform_metrics': platform_data,
            'correlation_score': correlation_score,
            'synchronized_activity': correlation_score > 0.8,
            'dominant_platform': max(platform_data.keys(), key=lambda k: platform_data[k]['volume'])
        }
        
    def _calculate_platform_correlation(self, platform_data: Dict[str, Dict]) -> float:
        """Calculate correlation between platforms"""
        if len(platform_data) < 2:
            return 0.0
            
        # Simple correlation based on timing and volume patterns
        correlations = []
        platforms = list(platform_data.keys())
        
        for i in range(len(platforms)):
            for j in range(i + 1, len(platforms)):
                p1, p2 = platforms[i], platforms[j]
                timing_corr = min(platform_data[p1]['timing_correlation'], 
                                platform_data[p2]['timing_correlation'])
                correlations.append(timing_corr)
                
        return sum(correlations) / len(correlations) if correlations else 0.0
        
    def _calculate_organic_score(self, velocity_analysis: Dict, spike_analysis: Dict, 
                               platform_analysis: Dict) -> float:
        """Calculate overall organic score"""
        
        organic_score = 70.0  # Base organic score
        
        # Velocity factors
        if velocity_analysis.get('max_growth_rate_percent', 0) > 1000:
            organic_score -= 30
        elif velocity_analysis.get('max_growth_rate_percent', 0) > 500:
            organic_score -= 20
        elif velocity_analysis.get('max_growth_rate_percent', 0) > 200:
            organic_score -= 10
            
        # Spike factors
        artificial_prob = spike_analysis.get('artificial_probability', 0)
        organic_score -= artificial_prob * 0.5
        
        # Cross-platform factors
        if platform_analysis.get('synchronized_activity', False):
            organic_score -= 15
            
        correlation = platform_analysis.get('correlation_score', 0)
        if correlation > 0.9:
            organic_score -= 20
        elif correlation > 0.8:
            organic_score -= 10
            
        # Ensure bounds
        return max(min(organic_score, 95), 5)
        
    def _calculate_confidence(self, trend_data: List[Dict], velocity_analysis: Dict) -> float:
        """Calculate confidence in the analysis"""
        
        confidence = 75.0  # Base confidence
        
        # More data points increase confidence
        data_points = len(trend_data)
        if data_points > 100:
            confidence += 15
        elif data_points > 50:
            confidence += 10
        elif data_points < 20:
            confidence -= 10
            
        # Clear patterns increase confidence
        growth_pattern = velocity_analysis.get('growth_pattern', '')
        if growth_pattern in ['explosive_spike', 'sustained_growth']:
            confidence += 10
        elif growth_pattern == 'insufficient_data':
            confidence -= 20
            
        # Add randomness for realism
        confidence += random.uniform(-5, 5)
        
        return max(min(confidence, 98), 60)
        
    def _get_data_sources(self, platform: str) -> List[str]:
        """Get list of data sources used for analysis"""
        sources = ['trend_analysis', 'volume_metrics']
        
        if self.config.TREND_ANALYZER_USE_GOOGLE_TRENDS:
            sources.append('google_trends')
        else:
            sources.append('mock_google_trends')
            
        if self.config.TREND_ANALYZER_USE_NEWS_API:
            sources.append('news_api')
        else:
            sources.append('mock_news_data')
            
        if platform == 'twitter' or platform == 'all':
            sources.append('twitter_trending')
        if platform == 'reddit' or platform == 'all':
            sources.append('reddit_hot_posts')
        if platform == 'youtube' or platform == 'all':
            sources.append('youtube_trending')
            
        return sources
        
    def _get_organic_indicators(self, velocity_analysis: Dict, spike_analysis: Dict) -> List[str]:
        """Get list of organic growth indicators"""
        indicators = []
        
        if velocity_analysis.get('growth_pattern') == 'gradual_growth':
            indicators.append('Gradual, sustained growth pattern')
            
        if velocity_analysis.get('max_growth_rate_percent', 0) < 200:
            indicators.append('Moderate growth velocity')
            
        if spike_analysis.get('artificial_probability', 0) < 30:
            indicators.append('Low artificial spike probability')
            
        if velocity_analysis.get('volatility', 0) < 50:
            indicators.append('Low volatility in trend data')
            
        return indicators if indicators else ['Limited organic indicators detected']
        
    def _get_manipulation_indicators(self, velocity_analysis: Dict, spike_analysis: Dict) -> List[str]:
        """Get list of manipulation indicators"""
        indicators = []
        
        if velocity_analysis.get('growth_pattern') == 'explosive_spike':
            indicators.append('Explosive growth spike detected')
            
        if velocity_analysis.get('max_growth_rate_percent', 0) > 500:
            indicators.append('Abnormally high growth rate')
            
        if spike_analysis.get('artificial_probability', 0) > 60:
            indicators.append('High artificial spike probability')
            
        if velocity_analysis.get('volatility', 0) > 100:
            indicators.append('Extreme volatility in trend data')
            
        spikes = spike_analysis.get('spikes_detected', 0)
        if spikes > 3:
            indicators.append(f'Multiple suspicious spikes ({spikes} detected)')
            
        return indicators if indicators else ['No clear manipulation indicators']
        
    def _get_demo_evidence(self, query: str, bot_percentage: int, reality_score: int) -> Dict[str, Any]:
        """Get trend analyzer specific demo evidence"""
        
        # Generate realistic trend metrics
        peak_volume = random.randint(5000, 25000)
        growth_rate = random.randint(200, 1500)
        
        return {
            'peak_volume': peak_volume,
            'max_growth_rate': f"{growth_rate}%",
            'time_to_peak': f"{random.randint(6, 48)} hours",
            'platforms_affected': random.randint(3, 6),
            'key_findings': [
                f"Growth rate of {growth_rate}% in {random.randint(6, 24)} hours",
                f"Peak volume reached: {peak_volume:,} mentions",
                f"Detected {random.randint(2, 5)} artificial spikes",
                f"Cross-platform correlation: {random.randint(75, 95)}%"
            ],
            'trend_pattern': random.choice(['explosive_spike', 'coordinated_campaign', 'artificial_boost']),
            'organic_probability': f"{reality_score}%",
            'velocity_analysis': {
                'classification': 'Highly suspicious',
                'spike_count': random.randint(2, 6),
                'decline_rate': f"{random.randint(60, 85)}%"
            },
            'timeline_analysis': [
                'Initial organic interest detected',
                'Sudden acceleration in mentions',
                'Peak activity reached',
                'Rapid decline following peak',
                'Return to baseline levels'
            ]
        }


def main():
    """Main function for running trend analysis agent standalone"""
    agent = TrendAnalysisAgent()
    
    try:
        agent.start()
        agent.logger.info("Trend Analysis Agent started successfully")
        
        # Keep running until interrupted
        while agent.is_running:
            time.sleep(1)
            
    except KeyboardInterrupt:
        agent.logger.info("Received shutdown signal")
    finally:
        agent.stop()
        agent.logger.info("Trend Analysis Agent stopped")


if __name__ == "__main__":
    main()
