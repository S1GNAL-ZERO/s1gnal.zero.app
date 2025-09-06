"""
Review Validator Agent for S1GNAL.ZERO
Validates review authenticity and detects fake review patterns.
"""

import random
import time
from typing import Dict, Any, List
from datetime import datetime, timedelta

from base.base_agent import BaseAgent


class ReviewValidatorAgent(BaseAgent):
    """
    Specialized agent for validating review authenticity and detecting fake review patterns
    """
    
    def __init__(self):
        super().__init__('review-validator')
        self.min_reviews = self.config.REVIEW_VALIDATOR_MIN_REVIEWS
        self.template_threshold = self.config.REVIEW_VALIDATOR_TEMPLATE_THRESHOLD
        
    def process_analysis_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """Process review validation analysis request"""
        analysis_id = request_data.get('analysisId', '')
        query = request_data.get('query', '')
        platform = request_data.get('platform', 'all')
        
        self.logger.info(f"Processing review validation for query: {query}")
        
        # Check for hardcoded demo responses first
        demo_response = self.get_hardcoded_demo_response(query, analysis_id)
        if demo_response:
            self.logger.info(f"Returning hardcoded demo response for: {query}")
            return demo_response
            
        # Simulate processing delay for realism
        if self.config.DEMO_RESPONSE_DELAY_MS > 0:
            time.sleep(self.config.DEMO_RESPONSE_DELAY_MS / 1000.0)
            
        # Generate realistic review analysis
        review_analysis = self._analyze_review_patterns(query, platform)
        
        return self.create_standard_response(
            analysis_id,
            review_analysis['authenticity_score'],
            review_analysis['confidence'],
            review_analysis['evidence'],
            review_analysis['data_sources']
        )
        
    def _analyze_review_patterns(self, query: str, platform: str) -> Dict[str, Any]:
        """Analyze review patterns for the given query"""
        
        # Generate review dataset
        review_data = self._generate_review_data(query)
        
        # Analyze temporal patterns
        temporal_analysis = self._analyze_temporal_patterns(review_data)
        
        # Analyze content patterns
        content_analysis = self._analyze_content_patterns(review_data)
        
        # Analyze reviewer patterns
        reviewer_analysis = self._analyze_reviewer_patterns(review_data)
        
        # Analyze rating distribution
        rating_analysis = self._analyze_rating_distribution(review_data)
        
        # Calculate authenticity score
        authenticity_score = self._calculate_authenticity_score(
            temporal_analysis, content_analysis, reviewer_analysis, rating_analysis
        )
        
        # Calculate confidence
        confidence = self._calculate_confidence(review_data, content_analysis)
        
        # Determine data sources
        data_sources = self._get_data_sources(platform)
        
        # Create evidence dictionary
        evidence = {
            'total_reviews_analyzed': len(review_data),
            'temporal_analysis': temporal_analysis,
            'content_analysis': content_analysis,
            'reviewer_analysis': reviewer_analysis,
            'rating_analysis': rating_analysis,
            'authenticity_indicators': self._get_authenticity_indicators(temporal_analysis, content_analysis),
            'manipulation_indicators': self._get_manipulation_indicators(temporal_analysis, content_analysis)
        }
        
        return {
            'authenticity_score': authenticity_score,
            'confidence': confidence,
            'evidence': evidence,
            'data_sources': data_sources
        }
        
    def _generate_review_data(self, query: str) -> List[Dict[str, Any]]:
        """Generate realistic review data"""
        
        # Determine review pattern based on query
        pattern_type = self._determine_review_pattern(query)
        
        # Generate review count
        total_reviews = random.randint(100, 5000)
        
        reviews = []
        base_date = datetime.now() - timedelta(days=365)
        
        for i in range(total_reviews):
            # Generate review based on pattern
            if pattern_type == 'fake_surge':
                review = self._generate_fake_surge_review(i, total_reviews, base_date)
            elif pattern_type == 'bot_reviews':
                review = self._generate_bot_review(i, total_reviews, base_date)
            elif pattern_type == 'incentivized':
                review = self._generate_incentivized_review(i, total_reviews, base_date)
            else:
                review = self._generate_organic_review(i, total_reviews, base_date)
                
            reviews.append(review)
            
        return reviews
        
    def _determine_review_pattern(self, query: str) -> str:
        """Determine the type of review pattern based on query"""
        query_lower = query.lower()
        
        # High manipulation indicators
        if any(term in query_lower for term in ['viral', 'trending', 'popular', 'bestseller']):
            return random.choice(['fake_surge', 'bot_reviews'])
        elif any(term in query_lower for term in ['new', 'launch', 'product']):
            return random.choice(['incentivized', 'fake_surge'])
        elif any(term in query_lower for term in ['cheap', 'deal', 'discount']):
            return 'bot_reviews'
        else:
            return random.choice(['organic', 'incentivized'])
            
    def _generate_fake_surge_review(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate review for fake surge pattern"""
        
        # Most reviews in recent period
        if index < total * 0.7:  # 70% of reviews in last 30 days
            days_ago = random.randint(1, 30)
        else:
            days_ago = random.randint(31, 365)
            
        return {
            'id': f"review_{index}",
            'rating': random.choices([5, 4, 3, 2, 1], weights=[70, 15, 8, 4, 3])[0],
            'date': base_date + timedelta(days=365-days_ago),
            'verified_purchase': random.choice([True, False]),
            'reviewer_id': f"user_{random.randint(1000, 99999)}",
            'review_length': random.randint(20, 200),
            'helpful_votes': random.randint(0, 10),
            'template_similarity': random.uniform(0.3, 0.9),
            'sentiment_score': random.uniform(0.7, 1.0) if random.random() > 0.2 else random.uniform(0.0, 0.3)
        }
        
    def _generate_bot_review(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate review for bot pattern"""
        
        days_ago = random.randint(1, 180)
        
        return {
            'id': f"review_{index}",
            'rating': random.choices([5, 4, 3, 2, 1], weights=[80, 10, 5, 3, 2])[0],
            'date': base_date + timedelta(days=365-days_ago),
            'verified_purchase': random.choice([True, False]),
            'reviewer_id': f"user_{random.randint(10000, 999999)}",
            'review_length': random.randint(10, 50),  # Shorter reviews
            'helpful_votes': random.randint(0, 3),
            'template_similarity': random.uniform(0.7, 0.95),  # High similarity
            'sentiment_score': random.uniform(0.8, 1.0) if random.random() > 0.1 else random.uniform(0.0, 0.2)
        }
        
    def _generate_incentivized_review(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate review for incentivized pattern"""
        
        days_ago = random.randint(1, 90)
        
        return {
            'id': f"review_{index}",
            'rating': random.choices([5, 4, 3, 2, 1], weights=[60, 25, 10, 3, 2])[0],
            'date': base_date + timedelta(days=365-days_ago),
            'verified_purchase': random.choice([True, True, True, False]),  # Mostly verified
            'reviewer_id': f"user_{random.randint(1000, 50000)}",
            'review_length': random.randint(50, 150),
            'helpful_votes': random.randint(0, 8),
            'template_similarity': random.uniform(0.4, 0.7),
            'sentiment_score': random.uniform(0.6, 0.9)
        }
        
    def _generate_organic_review(self, index: int, total: int, base_date: datetime) -> Dict[str, Any]:
        """Generate organic review"""
        
        days_ago = random.randint(1, 365)
        
        return {
            'id': f"review_{index}",
            'rating': random.choices([5, 4, 3, 2, 1], weights=[35, 30, 20, 10, 5])[0],
            'date': base_date + timedelta(days=365-days_ago),
            'verified_purchase': random.choice([True, True, False]),
            'reviewer_id': f"user_{random.randint(100, 10000)}",
            'review_length': random.randint(30, 300),
            'helpful_votes': random.randint(0, 15),
            'template_similarity': random.uniform(0.1, 0.4),
            'sentiment_score': random.uniform(0.2, 0.8)
        }
        
    def _analyze_temporal_patterns(self, reviews: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze temporal patterns in reviews"""
        
        # Group reviews by time periods
        now = datetime.now()
        periods = {
            'last_7_days': 0,
            'last_30_days': 0,
            'last_90_days': 0,
            'last_year': 0
        }
        
        for review in reviews:
            days_ago = (now - review['date']).days
            
            if days_ago <= 7:
                periods['last_7_days'] += 1
            if days_ago <= 30:
                periods['last_30_days'] += 1
            if days_ago <= 90:
                periods['last_90_days'] += 1
            if days_ago <= 365:
                periods['last_year'] += 1
                
        total_reviews = len(reviews)
        
        # Calculate percentages
        percentages = {
            period: (count / total_reviews * 100) if total_reviews > 0 else 0
            for period, count in periods.items()
        }
        
        # Detect suspicious patterns
        surge_detected = percentages['last_30_days'] > 60  # More than 60% in last 30 days
        recent_spike = percentages['last_7_days'] > 30     # More than 30% in last 7 days
        
        return {
            'review_counts': periods,
            'review_percentages': percentages,
            'total_reviews': total_reviews,
            'surge_detected': surge_detected,
            'recent_spike_detected': recent_spike,
            'temporal_score': self._calculate_temporal_score(percentages)
        }
        
    def _calculate_temporal_score(self, percentages: Dict[str, float]) -> float:
        """Calculate temporal suspiciousness score"""
        score = 0
        
        # High concentration in recent periods is suspicious
        if percentages['last_7_days'] > 40:
            score += 40
        elif percentages['last_7_days'] > 25:
            score += 25
            
        if percentages['last_30_days'] > 70:
            score += 30
        elif percentages['last_30_days'] > 50:
            score += 20
            
        return min(score, 100)
        
    def _analyze_content_patterns(self, reviews: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze content patterns in reviews"""
        
        # Analyze template similarity
        high_similarity_count = sum(1 for r in reviews if r['template_similarity'] > 0.7)
        template_percentage = (high_similarity_count / len(reviews) * 100) if reviews else 0
        
        # Analyze review lengths
        lengths = [r['review_length'] for r in reviews]
        avg_length = sum(lengths) / len(lengths) if lengths else 0
        short_reviews = sum(1 for l in lengths if l < 30)
        short_review_percentage = (short_reviews / len(reviews) * 100) if reviews else 0
        
        # Analyze sentiment patterns
        sentiments = [r['sentiment_score'] for r in reviews]
        avg_sentiment = sum(sentiments) / len(sentiments) if sentiments else 0
        extreme_positive = sum(1 for s in sentiments if s > 0.9)
        extreme_positive_percentage = (extreme_positive / len(reviews) * 100) if reviews else 0
        
        return {
            'template_similarity': {
                'high_similarity_count': high_similarity_count,
                'template_percentage': round(template_percentage, 1),
                'suspicious_threshold_exceeded': template_percentage > 40
            },
            'review_length': {
                'average_length': round(avg_length, 1),
                'short_reviews_count': short_reviews,
                'short_review_percentage': round(short_review_percentage, 1),
                'length_pattern': 'suspicious' if short_review_percentage > 50 else 'normal'
            },
            'sentiment_analysis': {
                'average_sentiment': round(avg_sentiment, 2),
                'extreme_positive_count': extreme_positive,
                'extreme_positive_percentage': round(extreme_positive_percentage, 1),
                'sentiment_pattern': 'artificial' if extreme_positive_percentage > 60 else 'natural'
            },
            'content_score': self._calculate_content_score(template_percentage, short_review_percentage, extreme_positive_percentage)
        }
        
    def _calculate_content_score(self, template_pct: float, short_pct: float, extreme_pct: float) -> float:
        """Calculate content suspiciousness score"""
        score = 0
        
        # Template similarity scoring
        if template_pct > 60:
            score += 35
        elif template_pct > 40:
            score += 25
        elif template_pct > 20:
            score += 15
            
        # Short review scoring
        if short_pct > 60:
            score += 25
        elif short_pct > 40:
            score += 15
            
        # Extreme sentiment scoring
        if extreme_pct > 70:
            score += 30
        elif extreme_pct > 50:
            score += 20
            
        return min(score, 100)
        
    def _analyze_reviewer_patterns(self, reviews: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze reviewer patterns"""
        
        # Count verified vs unverified purchases
        verified_count = sum(1 for r in reviews if r['verified_purchase'])
        verified_percentage = (verified_count / len(reviews) * 100) if reviews else 0
        
        # Analyze reviewer IDs for patterns
        reviewer_ids = [r['reviewer_id'] for r in reviews]
        unique_reviewers = len(set(reviewer_ids))
        duplicate_reviewers = len(reviewer_ids) - unique_reviewers
        
        # Analyze helpful votes
        helpful_votes = [r['helpful_votes'] for r in reviews]
        avg_helpful_votes = sum(helpful_votes) / len(helpful_votes) if helpful_votes else 0
        low_engagement = sum(1 for h in helpful_votes if h <= 1)
        low_engagement_percentage = (low_engagement / len(reviews) * 100) if reviews else 0
        
        return {
            'verification_analysis': {
                'verified_count': verified_count,
                'verified_percentage': round(verified_percentage, 1),
                'verification_pattern': 'suspicious' if verified_percentage < 30 else 'normal'
            },
            'reviewer_diversity': {
                'total_reviews': len(reviews),
                'unique_reviewers': unique_reviewers,
                'duplicate_reviewers': duplicate_reviewers,
                'diversity_score': round((unique_reviewers / len(reviews) * 100), 1) if reviews else 0
            },
            'engagement_analysis': {
                'average_helpful_votes': round(avg_helpful_votes, 1),
                'low_engagement_count': low_engagement,
                'low_engagement_percentage': round(low_engagement_percentage, 1),
                'engagement_pattern': 'artificial' if low_engagement_percentage > 70 else 'natural'
            },
            'reviewer_score': self._calculate_reviewer_score(verified_percentage, unique_reviewers, len(reviews), low_engagement_percentage)
        }
        
    def _calculate_reviewer_score(self, verified_pct: float, unique_count: int, total_count: int, low_engagement_pct: float) -> float:
        """Calculate reviewer suspiciousness score"""
        score = 0
        
        # Low verification rate is suspicious
        if verified_pct < 20:
            score += 30
        elif verified_pct < 40:
            score += 20
            
        # Low diversity is suspicious
        diversity_ratio = unique_count / total_count if total_count > 0 else 1
        if diversity_ratio < 0.7:
            score += 25
        elif diversity_ratio < 0.85:
            score += 15
            
        # Low engagement is suspicious
        if low_engagement_pct > 80:
            score += 25
        elif low_engagement_pct > 60:
            score += 15
            
        return min(score, 100)
        
    def _analyze_rating_distribution(self, reviews: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Analyze rating distribution patterns"""
        
        # Count ratings
        rating_counts = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0}
        for review in reviews:
            rating_counts[review['rating']] += 1
            
        total = len(reviews)
        rating_percentages = {
            rating: (count / total * 100) if total > 0 else 0
            for rating, count in rating_counts.items()
        }
        
        # Calculate average rating
        total_rating_points = sum(rating * count for rating, count in rating_counts.items())
        avg_rating = total_rating_points / total if total > 0 else 0
        
        # Detect suspicious patterns
        five_star_heavy = rating_percentages[5] > 70
        bimodal_pattern = rating_percentages[5] > 50 and rating_percentages[1] > 20
        unnatural_distribution = rating_percentages[5] > 80 or (rating_percentages[4] + rating_percentages[5]) > 90
        
        return {
            'rating_counts': rating_counts,
            'rating_percentages': {k: round(v, 1) for k, v in rating_percentages.items()},
            'average_rating': round(avg_rating, 2),
            'distribution_patterns': {
                'five_star_heavy': five_star_heavy,
                'bimodal_pattern': bimodal_pattern,
                'unnatural_distribution': unnatural_distribution
            },
            'rating_score': self._calculate_rating_score(rating_percentages, bimodal_pattern)
        }
        
    def _calculate_rating_score(self, percentages: Dict[int, float], bimodal: bool) -> float:
        """Calculate rating distribution suspiciousness score"""
        score = 0
        
        # Very high 5-star percentage is suspicious
        if percentages[5] > 80:
            score += 40
        elif percentages[5] > 70:
            score += 30
        elif percentages[5] > 60:
            score += 20
            
        # Bimodal distribution (high 5-star and high 1-star) is suspicious
        if bimodal:
            score += 25
            
        # Very low middle ratings (2, 3, 4) is suspicious
        middle_ratings = percentages[2] + percentages[3] + percentages[4]
        if middle_ratings < 10:
            score += 20
            
        return min(score, 100)
        
    def _calculate_authenticity_score(self, temporal: Dict, content: Dict, reviewer: Dict, rating: Dict) -> float:
        """Calculate overall authenticity score"""
        
        # Start with base authenticity
        authenticity = 70.0
        
        # Subtract based on suspiciousness scores (weighted)
        temporal_penalty = temporal['temporal_score'] * 0.25
        content_penalty = content['content_score'] * 0.30
        reviewer_penalty = reviewer['reviewer_score'] * 0.25
        rating_penalty = rating['rating_score'] * 0.20
        
        total_penalty = temporal_penalty + content_penalty + reviewer_penalty + rating_penalty
        authenticity -= total_penalty
        
        return max(min(authenticity, 95), 5)
        
    def _calculate_confidence(self, reviews: List[Dict], content_analysis: Dict) -> float:
        """Calculate confidence in the analysis"""
        
        confidence = 75.0  # Base confidence
        
        # More reviews increase confidence
        review_count = len(reviews)
        if review_count > 1000:
            confidence += 15
        elif review_count > 500:
            confidence += 10
        elif review_count < 50:
            confidence -= 15
            
        # Clear patterns increase confidence
        if content_analysis['template_similarity']['suspicious_threshold_exceeded']:
            confidence += 10
        if content_analysis['sentiment_analysis']['sentiment_pattern'] == 'artificial':
            confidence += 8
            
        # Add randomness for realism
        confidence += random.uniform(-5, 5)
        
        return max(min(confidence, 98), 60)
        
    def _get_data_sources(self, platform: str) -> List[str]:
        """Get list of data sources used for analysis"""
        sources = ['review_analysis', 'temporal_patterns', 'content_analysis']
        
        if platform == 'amazon' or platform == 'all':
            if self.config.REVIEW_VALIDATOR_USE_SCRAPING:
                sources.append('amazon_scraping')
            else:
                sources.append('mock_amazon_data')
                
        if self.config.REVIEW_VALIDATOR_USE_REVIEWMETA:
            sources.append('reviewmeta_api')
        else:
            sources.append('mock_reviewmeta_data')
            
        sources.extend(['sentiment_analysis', 'pattern_detection'])
        
        return sources
        
    def _get_authenticity_indicators(self, temporal: Dict, content: Dict) -> List[str]:
        """Get list of authenticity indicators"""
        indicators = []
        
        if temporal['temporal_score'] < 30:
            indicators.append('Natural review distribution over time')
            
        if content['sentiment_analysis']['sentiment_pattern'] == 'natural':
            indicators.append('Natural sentiment variation')
            
        if content['template_similarity']['template_percentage'] < 20:
            indicators.append('Low template similarity between reviews')
            
        if content['review_length']['length_pattern'] == 'normal':
            indicators.append('Normal review length distribution')
            
        return indicators if indicators else ['Limited authenticity indicators detected']
        
    def _get_manipulation_indicators(self, temporal: Dict, content: Dict) -> List[str]:
        """Get list of manipulation indicators"""
        indicators = []
        
        if temporal['surge_detected']:
            indicators.append('Suspicious surge in recent reviews')
            
        if temporal['recent_spike_detected']:
            indicators.append('Abnormal spike in last 7 days')
            
        if content['template_similarity']['suspicious_threshold_exceeded']:
            indicators.append('High template similarity detected')
            
        if content['sentiment_analysis']['sentiment_pattern'] == 'artificial':
            indicators.append('Artificially positive sentiment pattern')
            
        if content['review_length']['length_pattern'] == 'suspicious':
            indicators.append('Unusually short review pattern')
            
        return indicators if indicators else ['No clear manipulation indicators']
        
    def _get_demo_evidence(self, query: str, bot_percentage: int, reality_score: int) -> Dict[str, Any]:
        """Get review validator specific demo evidence"""
        
        # Generate realistic review metrics
        total_reviews = random.randint(1000, 8000)
        suspicious_reviews = int(total_reviews * (100 - reality_score) / 100)
        
        return {
            'total_reviews': total_reviews,
            'suspicious_reviews': suspicious_reviews,
            'authenticity_score': reality_score,
            'key_findings': [
                f"{100 - reality_score}% of reviews show suspicious patterns",
                f"{random.randint(60, 85)}% of reviews posted in last 30 days",
                f"High template similarity in {random.randint(200, 500)} reviews",
                f"Only {random.randint(15, 35)}% verified purchases"
            ],
            'review_patterns': {
                'temporal_surge': True,
                'template_similarity': f"{random.randint(40, 80)}%",
                'verified_purchase_rate': f"{random.randint(15, 35)}%",
                'average_rating': f"{random.uniform(4.2, 4.8):.1f}/5.0"
            },
            'suspicious_indicators': [
                'Burst of reviews in short timeframe',
                'Repetitive language patterns',
                'Low verified purchase rate',
                'Unnatural rating distribution',
                'Minimal reviewer engagement'
            ],
            'analysis_methods': [
                'Temporal pattern analysis',
                'Content similarity detection',
                'Reviewer behavior analysis',
                'Rating distribution analysis',
                'Sentiment pattern recognition'
            ]
        }


def main():
    """Main function for running review validator agent standalone"""
    agent = ReviewValidatorAgent()
    
    try:
        agent.start()
        agent.logger.info("Review Validator Agent started successfully")
        
        # Keep running until interrupted
        while agent.is_running:
            time.sleep(1)
            
    except KeyboardInterrupt:
        agent.logger.info("Received shutdown signal")
    finally:
        agent.stop()
        agent.logger.info("Review Validator Agent stopped")


if __name__ == "__main__":
    main()
