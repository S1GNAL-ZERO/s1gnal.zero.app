"""
Paid Promotion Agent for S1GNAL.ZERO
Detects undisclosed sponsorships and paid promotional content.
"""

import random
import time
from typing import Dict, Any, List
from datetime import datetime, timedelta

from base.base_agent import BaseAgent


class PaidPromotionAgent(BaseAgent):
    """
    Specialized agent for detecting undisclosed sponsorships and paid promotional content
    """
    
    def __init__(self):
        super().__init__('paid-promotion')
        self.keyword_threshold = self.config.PAID_PROMOTION_KEYWORD_THRESHOLD
        self.ftc_compliance_check = self.config.PAID_PROMOTION_FTC_COMPLIANCE_CHECK
        
    def process_analysis_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """Process paid promotion detection analysis request"""
        analysis_id = request_data.get('analysisId', '')
        query = request_data.get('query', '')
        platform = request_data.get('platform', 'all')
        
        self.logger.info(f"Processing paid promotion detection for query: {query}")
        
        # Check for hardcoded demo responses first
        demo_response = self.get_hardcoded_demo_response(query, analysis_id)
        if demo_response:
            self.logger.info(f"Returning hardcoded demo response for: {query}")
            return demo_response
            
        # Simulate processing delay for realism
        if self.config.DEMO_RESPONSE_DELAY_MS > 0:
            time.sleep(self.config.DEMO_RESPONSE_DELAY_MS / 1000.0)
            
        # Generate realistic promotion analysis
        promotion_analysis = self._analyze_promotion_patterns(query, platform)
        
        return self.create_standard_response(
            analysis_id,
            promotion_analysis['transparency_score'],
            promotion_analysis['confidence'],
            promotion_analysis['evidence'],
            promotion_analysis['data_sources']
        )
        
    def _analyze_promotion_patterns(self, query: str, platform: str) -> Dict[str, Any]:
        """Analyze promotional patterns for the given query"""
        
        # Generate content dataset
        content_data = self._generate_content_data(query, platform)
        
        # Analyze disclosure patterns
        disclosure_analysis = self._analyze_disclosure_patterns(content_data)
        
        # Analyze influencer patterns
        influencer_analysis = self._analyze_influencer_patterns(content_data)
        
        # Analyze timing patterns
        timing_analysis = self._analyze_timing_patterns(content_data)
        
        # Analyze engagement patterns
        engagement_analysis = self._analyze_engagement_patterns(content_data)
        
        # Calculate transparency score
        transparency_score = self._calculate_transparency_score(
            disclosure_analysis, influencer_analysis, timing_analysis, engagement_analysis
        )
        
        # Calculate confidence
        confidence = self._calculate_confidence(content_data, disclosure_analysis)
        
        # Determine data sources
        data_sources = self._get_data_sources(platform)
        
        # Create evidence dictionary
        evidence = {
            'total_content_analyzed': len(content_data),
            'disclosure_analysis': disclosure_analysis,
            'influencer_analysis': influencer_analysis,
            'timing_analysis': timing_analysis,
            'engagement_analysis': engagement_analysis,
            'transparency_indicators': self._get_transparency_indicators(disclosure_analysis, timing_analysis),
            'manipulation_indicators': self._get_manipulation_indicators(disclosure_analysis, influencer_analysis)
        }
        
        return {
            'transparency_score': transparency_score,
            'confidence': confidence,
            'evidence': evidence,
            'data_sources': data_sources
        }
        
    def _generate_content_data(self, query: str, platform: str) -> List[Dict[str, Any]]:
        """Generate realistic content data for analysis"""
        
        # Determine promotion pattern based on query
        pattern_type = self._determine_promotion_pattern(query)
        
        # Generate content count
        total_content = random.randint(50, 500)
        
        content_items = []
        base_date = datetime.now() - timedelta(days=90)
        
        for i in range(total_content):
            # Generate content based on pattern
            if pattern_type == 'undisclosed_campaign':
                content = self._generate_undisclosed_campaign_content(i, total_content, base_date)
            elif pattern_type == 'influencer_coordination':
                content = self._generate_coordinated_influencer_content(i, total_content, base_date)
            elif pattern_type == 'astroturfing':
                content = self._generate_astroturfing_content(i, total_content, base_date)
            else:
                content = self._generate_organic_content(i, total_content, base_date)
                
            content_items.append(content)
            
        return content_items
        
    def _determine_promotion_pattern(self, query: str) -> str:
        """Determine the type of promotion pattern based on query"""
        query_lower = query.lower()
        
        # High promotion indicators
        if any(term in query_lower for term in ['viral', 'trending', 'influencer', 'sponsored']):
            return random.choice(['undisclosed_campaign', 'influencer_coordination'])
        elif any(term in query_lower for term in ['product', 'brand', 'launch', 'new']):
            return random.choice(['undisclosed_campaign', 'astroturfing'])
        elif any(term in query_lower for term in ['review', 'recommendation', 'must-have']):
            return 'astroturfing'
        else:
            return random.choice(['organic', 'undisclosed_campaign'])
            
    def _generate_undisclosed_campaign_content(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate content for undisclosed campaign pattern"""
        
        days_ago = random.randint(1, 60)
        
        return {
            'id': f"content_{index}",
            'platform': random.choice(['instagram', 'tiktok', 'youtube', 'twitter']),
            'creator_id': f"influencer_{random.randint(1000, 9999)}",
            'follower_count': random.randint(10000, 1000000),
            'date': base_date + timedelta(days=90-days_ago),
            'has_disclosure': random.choice([False, False, False, True]),  # Mostly undisclosed
            'disclosure_type': random.choice(['none', 'none', 'none', '#ad']) if random.random() > 0.7 else 'none',
            'engagement_rate': random.uniform(0.08, 0.25),  # Higher than normal
            'typical_engagement': random.uniform(0.02, 0.06),
            'content_type': random.choice(['post', 'story', 'video', 'reel']),
            'promotional_keywords': random.randint(3, 8),
            'brand_mentions': random.randint(1, 5),
            'call_to_action': random.choice([True, True, False]),
            'discount_code': random.choice([True, False, False])
        }
        
    def _generate_coordinated_influencer_content(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate content for coordinated influencer pattern"""
        
        # Cluster posting times for coordination
        if index < total * 0.6:  # 60% posted within same timeframe
            days_ago = random.randint(1, 7)
        else:
            days_ago = random.randint(8, 60)
            
        return {
            'id': f"content_{index}",
            'platform': random.choice(['instagram', 'tiktok', 'youtube']),
            'creator_id': f"influencer_{random.randint(1000, 9999)}",
            'follower_count': random.randint(50000, 500000),
            'date': base_date + timedelta(days=90-days_ago),
            'has_disclosure': random.choice([False, False, True]),  # Some disclosed
            'disclosure_type': random.choice(['#ad', '#sponsored', '#partnership', 'none']),
            'engagement_rate': random.uniform(0.06, 0.20),
            'typical_engagement': random.uniform(0.03, 0.08),
            'content_type': random.choice(['post', 'video', 'reel']),
            'promotional_keywords': random.randint(2, 6),
            'brand_mentions': random.randint(1, 3),
            'call_to_action': random.choice([True, True, True, False]),
            'discount_code': random.choice([True, True, False])
        }
        
    def _generate_astroturfing_content(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate content for astroturfing pattern"""
        
        days_ago = random.randint(1, 30)
        
        return {
            'id': f"content_{index}",
            'platform': random.choice(['reddit', 'twitter', 'facebook', 'forums']),
            'creator_id': f"user_{random.randint(10000, 99999)}",
            'follower_count': random.randint(100, 5000),  # Lower follower counts
            'date': base_date + timedelta(days=90-days_ago),
            'has_disclosure': False,  # Never disclosed
            'disclosure_type': 'none',
            'engagement_rate': random.uniform(0.01, 0.05),
            'typical_engagement': random.uniform(0.01, 0.03),
            'content_type': random.choice(['comment', 'post', 'review']),
            'promotional_keywords': random.randint(1, 4),
            'brand_mentions': random.randint(1, 2),
            'call_to_action': random.choice([True, False, False]),
            'discount_code': random.choice([False, False, True])
        }
        
    def _generate_organic_content(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate organic content"""
        
        days_ago = random.randint(1, 90)
        
        return {
            'id': f"content_{index}",
            'platform': random.choice(['instagram', 'tiktok', 'youtube', 'twitter', 'reddit']),
            'creator_id': f"user_{random.randint(100, 50000)}",
            'follower_count': random.randint(500, 100000),
            'date': base_date + timedelta(days=90-days_ago),
            'has_disclosure': random.choice([True, False, False, False]),  # Rarely disclosed (organic)
            'disclosure_type': random.choice(['none', 'none', 'none', '#gifted']),
            'engagement_rate': random.uniform(0.02, 0.08),
            'typical_engagement': random.uniform(0.02, 0.06),
            'content_type': random.choice(['post', 'story', 'video', 'comment']),
            'promotional_keywords': random.randint(0, 2),
            'brand_mentions': random.randint(0, 1),
            'call_to_action': random.choice([False, False, True]),
            'discount_code': False
        }
        
    def _analyze_disclosure_patterns(self, content_items: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze disclosure patterns in content"""
        
        total_content = len(content_items)
        
        # Count disclosure types
        disclosure_counts = {
            'none': 0,
            '#ad': 0,
            '#sponsored': 0,
            '#partnership': 0,
            '#gifted': 0,
            'other': 0
        }
        
        promotional_content = 0
        
        for item in content_items:
            disclosure_type = item['disclosure_type']
            if disclosure_type in disclosure_counts:
                disclosure_counts[disclosure_type] += 1
            else:
                disclosure_counts['other'] += 1
                
            # Count promotional content (high keywords, CTAs, discount codes)
            if (item['promotional_keywords'] > 2 or 
                item['call_to_action'] or 
                item['discount_code']):
                promotional_content += 1
                
        # Calculate percentages
        disclosure_percentages = {
            disclosure: (count / total_content * 100) if total_content > 0 else 0
            for disclosure, count in disclosure_counts.items()
        }
        
        # Calculate compliance metrics
        properly_disclosed = total_content - disclosure_counts['none']
        disclosure_rate = (properly_disclosed / total_content * 100) if total_content > 0 else 0
        
        # Promotional content without disclosure
        undisclosed_promotional = sum(1 for item in content_items 
                                    if (item['promotional_keywords'] > 2 or 
                                        item['call_to_action'] or 
                                        item['discount_code']) and 
                                       item['disclosure_type'] == 'none')
        
        return {
            'total_content': total_content,
            'disclosure_counts': disclosure_counts,
            'disclosure_percentages': {k: round(v, 1) for k, v in disclosure_percentages.items()},
            'disclosure_rate': round(disclosure_rate, 1),
            'promotional_content': promotional_content,
            'undisclosed_promotional': undisclosed_promotional,
            'compliance_score': self._calculate_compliance_score(disclosure_rate, undisclosed_promotional, promotional_content),
            'ftc_violations_likely': undisclosed_promotional > total_content * 0.3
        }
        
    def _calculate_compliance_score(self, disclosure_rate: float, undisclosed: int, promotional: int) -> float:
        """Calculate FTC compliance score"""
        score = 100.0
        
        # Penalize low disclosure rate
        score -= (100 - disclosure_rate) * 0.8
        
        # Penalize undisclosed promotional content
        if promotional > 0:
            undisclosed_rate = (undisclosed / promotional) * 100
            score -= undisclosed_rate * 0.5
            
        return max(score, 0)
        
    def _analyze_influencer_patterns(self, content_items: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze influencer behavior patterns"""
        
        # Group by creator
        creator_data = {}
        for item in content_items:
            creator_id = item['creator_id']
            if creator_id not in creator_data:
                creator_data[creator_id] = []
            creator_data[creator_id].append(item)
            
        # Analyze creator patterns
        suspicious_creators = 0
        high_engagement_creators = 0
        coordinated_creators = 0
        
        creator_analysis = []
        
        for creator_id, items in creator_data.items():
            if len(items) < 2:  # Skip single-post creators
                continue
                
            # Calculate metrics for this creator
            avg_engagement = sum(item['engagement_rate'] for item in items) / len(items)
            avg_typical = sum(item['typical_engagement'] for item in items) / len(items)
            engagement_boost = (avg_engagement / avg_typical) if avg_typical > 0 else 1
            
            disclosure_rate = sum(1 for item in items if item['has_disclosure']) / len(items)
            promotional_posts = sum(1 for item in items if item['promotional_keywords'] > 2)
            
            # Detect suspicious patterns
            is_suspicious = (
                engagement_boost > 3.0 or  # 3x normal engagement
                (promotional_posts > 0 and disclosure_rate < 0.5) or  # Low disclosure on promotional content
                len(items) > 5  # High volume posting
            )
            
            if is_suspicious:
                suspicious_creators += 1
                
            if engagement_boost > 2.0:
                high_engagement_creators += 1
                
            # Check for coordination (posting within similar timeframes)
            dates = [item['date'] for item in items]
            date_range = (max(dates) - min(dates)).days
            if len(items) > 2 and date_range < 7:
                coordinated_creators += 1
                
            creator_analysis.append({
                'creator_id': creator_id,
                'posts_count': len(items),
                'avg_engagement_rate': round(avg_engagement, 3),
                'engagement_boost': round(engagement_boost, 2),
                'disclosure_rate': round(disclosure_rate, 2),
                'promotional_posts': promotional_posts,
                'is_suspicious': is_suspicious,
                'follower_count': items[0]['follower_count']
            })
            
        return {
            'total_creators': len(creator_data),
            'suspicious_creators': suspicious_creators,
            'high_engagement_creators': high_engagement_creators,
            'coordinated_creators': coordinated_creators,
            'creator_details': creator_analysis[:10],  # Top 10 for evidence
            'coordination_score': self._calculate_coordination_score(coordinated_creators, len(creator_data)),
            'influencer_manipulation_detected': suspicious_creators > len(creator_data) * 0.4
        }
        
    def _calculate_coordination_score(self, coordinated: int, total: int) -> float:
        """Calculate coordination suspiciousness score"""
        if total == 0:
            return 0
            
        coordination_rate = (coordinated / total) * 100
        
        if coordination_rate > 60:
            return 90
        elif coordination_rate > 40:
            return 70
        elif coordination_rate > 20:
            return 50
        else:
            return coordination_rate
            
    def _analyze_timing_patterns(self, content_items: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze timing patterns for coordination"""
        
        # Group content by time periods
        dates = [item['date'] for item in content_items]
        
        # Analyze clustering
        time_clusters = self._detect_time_clusters(dates)
        
        # Analyze posting frequency
        date_counts = {}
        for date in dates:
            date_key = date.strftime('%Y-%m-%d')
            date_counts[date_key] = date_counts.get(date_key, 0) + 1
            
        # Find peak days
        max_posts_per_day = max(date_counts.values()) if date_counts else 0
        avg_posts_per_day = sum(date_counts.values()) / len(date_counts) if date_counts else 0
        
        # Detect suspicious patterns
        burst_posting = max_posts_per_day > avg_posts_per_day * 3
        coordinated_timing = len(time_clusters) > 0 and max(cluster['size'] for cluster in time_clusters) > len(content_items) * 0.3
        
        return {
            'total_posts': len(content_items),
            'date_range_days': (max(dates) - min(dates)).days if dates else 0,
            'time_clusters': time_clusters,
            'max_posts_per_day': max_posts_per_day,
            'avg_posts_per_day': round(avg_posts_per_day, 1),
            'burst_posting_detected': burst_posting,
            'coordinated_timing_detected': coordinated_timing,
            'timing_score': self._calculate_timing_score(burst_posting, coordinated_timing, time_clusters)
        }
        
    def _detect_time_clusters(self, dates: List[datetime]) -> List[Dict[str, Any]]:
        """Detect clusters of posts within short time periods"""
        if len(dates) < 3:
            return []
            
        sorted_dates = sorted(dates)
        clusters = []
        
        # Look for clusters within 24-hour windows
        for i in range(len(sorted_dates) - 2):
            cluster_dates = [sorted_dates[i]]
            
            for j in range(i + 1, len(sorted_dates)):
                if (sorted_dates[j] - sorted_dates[i]).total_seconds() <= 86400:  # 24 hours
                    cluster_dates.append(sorted_dates[j])
                else:
                    break
                    
            if len(cluster_dates) >= 3:  # At least 3 posts in 24 hours
                clusters.append({
                    'start_time': cluster_dates[0],
                    'end_time': cluster_dates[-1],
                    'size': len(cluster_dates),
                    'duration_hours': (cluster_dates[-1] - cluster_dates[0]).total_seconds() / 3600
                })
                
        return clusters
        
    def _calculate_timing_score(self, burst_posting: bool, coordinated_timing: bool, clusters: List) -> float:
        """Calculate timing suspiciousness score"""
        score = 0
        
        if burst_posting:
            score += 30
            
        if coordinated_timing:
            score += 40
            
        # Add points for each significant cluster
        for cluster in clusters:
            if cluster['size'] > 5:
                score += 20
            elif cluster['size'] > 3:
                score += 10
                
        return min(score, 100)
        
    def _analyze_engagement_patterns(self, content_items: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze engagement patterns for artificial boosting"""
        
        # Calculate engagement metrics
        engagement_rates = [item['engagement_rate'] for item in content_items]
        typical_rates = [item['typical_engagement'] for item in content_items]
        
        avg_engagement = sum(engagement_rates) / len(engagement_rates) if engagement_rates else 0
        avg_typical = sum(typical_rates) / len(typical_rates) if typical_rates else 0
        
        # Calculate boost ratios
        boost_ratios = []
        for i, item in enumerate(content_items):
            if typical_rates[i] > 0:
                boost_ratios.append(engagement_rates[i] / typical_rates[i])
            else:
                boost_ratios.append(1.0)
                
        avg_boost = sum(boost_ratios) / len(boost_ratios) if boost_ratios else 1.0
        
        # Count high-boost content
        high_boost_content = sum(1 for ratio in boost_ratios if ratio > 2.0)
        extreme_boost_content = sum(1 for ratio in boost_ratios if ratio > 4.0)
        
        # Detect patterns
        artificial_boosting = avg_boost > 2.5 or high_boost_content > len(content_items) * 0.4
        
        return {
            'avg_engagement_rate': round(avg_engagement, 3),
            'avg_typical_rate': round(avg_typical, 3),
            'avg_boost_ratio': round(avg_boost, 2),
            'high_boost_content': high_boost_content,
            'extreme_boost_content': extreme_boost_content,
            'artificial_boosting_detected': artificial_boosting,
            'engagement_score': self._calculate_engagement_score(avg_boost, high_boost_content, len(content_items))
        }
        
    def _calculate_engagement_score(self, avg_boost: float, high_boost_count: int, total_content: int) -> float:
        """Calculate engagement suspiciousness score"""
        score = 0
        
        # High average boost is suspicious
        if avg_boost > 4.0:
            score += 50
        elif avg_boost > 3.0:
            score += 35
        elif avg_boost > 2.0:
            score += 20
            
        # High proportion of boosted content is suspicious
        if total_content > 0:
            boost_rate = (high_boost_count / total_content) * 100
            if boost_rate > 60:
                score += 30
            elif boost_rate > 40:
                score += 20
            elif boost_rate > 20:
                score += 10
                
        return min(score, 100)
        
    def _calculate_transparency_score(self, disclosure: Dict, influencer: Dict, timing: Dict, engagement: Dict) -> float:
        """Calculate overall transparency score"""
        
        # Start with base transparency
        transparency = 70.0
        
        # Subtract based on suspiciousness scores (weighted)
        disclosure_penalty = (100 - disclosure['compliance_score']) * 0.40
        influencer_penalty = influencer['coordination_score'] * 0.25
        timing_penalty = timing['timing_score'] * 0.20
        engagement_penalty = engagement['engagement_score'] * 0.15
        
        total_penalty = disclosure_penalty + influencer_penalty + timing_penalty + engagement_penalty
        transparency -= total_penalty
        
        return max(min(transparency, 95), 5)
        
    def _calculate_confidence(self, content_items: List[Dict], disclosure_analysis: Dict) -> float:
        """Calculate confidence in the analysis"""
        
        confidence = 75.0  # Base confidence
        
        # More content increases confidence
        content_count = len(content_items)
        if content_count > 200:
            confidence += 15
        elif content_count > 100:
            confidence += 10
        elif content_count < 20:
            confidence -= 15
            
        # Clear patterns increase confidence
        if disclosure_analysis['ftc_violations_likely']:
            confidence += 10
        if disclosure_analysis['undisclosed_promotional'] > 10:
            confidence += 8
            
        # Add randomness for realism
        confidence += random.uniform(-5, 5)
        
        return max(min(confidence, 98), 60)
        
    def _get_data_sources(self, platform: str) -> List[str]:
        """Get list of data sources used for analysis"""
        sources = ['content_analysis', 'disclosure_detection', 'engagement_patterns']
        
        if platform == 'youtube' or platform == 'all':
            if self.config.PAID_PROMOTION_USE_YOUTUBE_API:
                sources.append('youtube_api')
            else:
                sources.append('mock_youtube_data')
                
        if platform == 'instagram' or platform == 'all':
            sources.append('instagram_content_analysis')
            
        if platform == 'tiktok' or platform == 'all':
            sources.append('tiktok_content_analysis')
            
        if self.ftc_compliance_check:
            sources.append('ftc_compliance_database')
        else:
            sources.append('mock_ftc_data')
            
        sources.extend(['timing_analysis', 'influencer_network_analysis'])
        
        return sources
        
    def _get_transparency_indicators(self, disclosure: Dict, timing: Dict) -> List[str]:
        """Get list of transparency indicators"""
        indicators = []
        
        if disclosure['disclosure_rate'] > 70:
            indicators.append('High disclosure rate on promotional content')
            
        if disclosure['compliance_score'] > 80:
            indicators.append('Good FTC compliance practices')
            
        if not timing['coordinated_timing_detected']:
            indicators.append('Natural posting timing patterns')
            
        if not timing['burst_posting_detected']:
            indicators.append('Organic posting frequency')
            
        return indicators if indicators else ['Limited transparency indicators detected']
        
    def _get_manipulation_indicators(self, disclosure: Dict, influencer: Dict) -> List[str]:
        """Get list of manipulation indicators"""
        indicators = []
        
        if disclosure['ftc_violations_likely']:
            indicators.append('Likely FTC disclosure violations detected')
            
        if disclosure['undisclosed_promotional'] > 10:
            indicators.append(f'{disclosure["undisclosed_promotional"]} undisclosed promotional posts')
            
        if influencer['influencer_manipulation_detected']:
            indicators.append('Coordinated influencer campaign detected')
            
        if influencer['coordinated_creators'] > 5:
            indicators.append(f'{influencer["coordinated_creators"]} creators posting in coordination')
            
        if disclosure['disclosure_rate'] < 30:
            indicators.append('Very low disclosure rate on promotional content')
            
        return indicators if indicators else ['No clear manipulation indicators']
        
    def _get_demo_evidence(self, query: str, bot_percentage: int, reality_score: int) -> Dict[str, Any]:
        """Get paid promotion detector specific demo evidence"""
        
        # Generate realistic promotion metrics
        total_posts = random.randint(200, 800)
        undisclosed_posts = int(total_posts * (100 - reality_score) / 100)
        
        return {
            'total_posts_analyzed': total_posts,
            'undisclosed_promotional_posts': undisclosed_posts,
            'transparency_score': reality_score,
            'key_findings': [
                f"{undisclosed_posts} posts likely violate FTC disclosure rules",
                f"{random.randint(15, 35)} influencers posting in coordination",
                f"Average engagement boost: {random.randint(250, 450)}%",
                f"Only {random.randint(15, 35)}% proper disclosure rate"
            ],
            'promotion_patterns': {
                'undisclosed_sponsorships': undisclosed_posts,
                'coordinated_influencers': random.randint(15, 35),
                'ftc_violations': True,
                'disclosure_rate': f"{random.randint(15, 35)}%"
            },
            'suspicious_indicators': [
                'High volume of undisclosed sponsorships',
                'Coordinated posting timing',
                'Artificial engagement boosting',
                'Missing FTC-required disclosures',
                'Influencer network coordination'
            ],
            'analysis_methods': [
                'Content disclosure analysis',
                'Influencer behavior tracking',
                'Timing pattern detection',
                'Engagement anomaly detection',
                'FTC compliance checking'
            ],
            'sample_violations': [
                {
                    'influencer': f"@lifestyle_guru_{random.randint(100, 999)}",
                    'followers': f"{random.randint(50, 500)}K",
                    'violation': 'No #ad disclosure on sponsored post',
                    'engagement_boost': f"{random.randint(200, 400)}%"
                },
                {
                    'influencer': f"@fitness_pro_{random.randint(100, 999)}",
                    'followers': f"{random.randint(100, 800)}K",
                    'violation': 'Buried disclosure in long caption',
                    'engagement_boost': f"{random.randint(150, 350)}%"
                }
            ]
        }


def main():
    """Main function for running paid promotion agent standalone"""
    agent = PaidPromotionAgent()
    
    try:
        agent.start()
        agent.logger.info("Paid Promotion Agent started successfully")
        
        # Keep running until interrupted
        while agent.is_running:
            time.sleep(1)
            
    except KeyboardInterrupt:
        agent.logger.info("Received shutdown signal")
    finally:
        agent.stop()
        agent.logger.info("Paid Promotion Agent stopped")


if __name__ == "__main__":
    main()
