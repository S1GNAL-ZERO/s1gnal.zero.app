"""
Score Aggregator Agent for S1GNAL.ZERO
Combines all agent signals into the final Reality Score™.
"""

import random
import time
from typing import Dict, Any, List
from datetime import datetime

from base.base_agent import BaseAgent


class ScoreAggregatorAgent(BaseAgent):
    """
    Specialized agent for aggregating all agent results into the final Reality Score™
    """
    
    def __init__(self):
        super().__init__('score-aggregator')
        # Weights from DETAILED_DESIGN.md Section 4.6 and CLAUDE.md
        self.bot_weight = self.config.SCORE_AGGREGATOR_BOT_WEIGHT  # 0.40
        self.trend_weight = self.config.SCORE_AGGREGATOR_TREND_WEIGHT  # 0.30
        self.review_weight = self.config.SCORE_AGGREGATOR_REVIEW_WEIGHT  # 0.20
        self.promotion_weight = self.config.SCORE_AGGREGATOR_PROMOTION_WEIGHT  # 0.10
        
    def process_analysis_request(self, request_data: Dict[str, Any]) -> Dict[str, Any]:
        """Process score aggregation request"""
        analysis_id = request_data.get('analysisId', '')
        query = request_data.get('query', '')
        agent_results = request_data.get('agentResults', {})
        
        self.logger.info(f"Processing score aggregation for query: {query}")
        
        # Check for hardcoded demo responses first
        demo_response = self.get_hardcoded_demo_response(query, analysis_id)
        if demo_response:
            self.logger.info(f"Returning hardcoded demo response for: {query}")
            return demo_response
            
        # Simulate processing delay for realism
        if self.config.DEMO_RESPONSE_DELAY_MS > 0:
            time.sleep(self.config.DEMO_RESPONSE_DELAY_MS / 1000.0)
            
        # Aggregate all agent results
        aggregation_result = self._aggregate_agent_results(query, agent_results)
        
        return self.create_standard_response(
            analysis_id,
            aggregation_result['reality_score'],
            aggregation_result['confidence'],
            aggregation_result['evidence'],
            aggregation_result['data_sources']
        )
        
    def _aggregate_agent_results(self, query: str, agent_results: Dict[str, Any]) -> Dict[str, Any]:
        """Aggregate results from all agents into final Reality Score™"""
        
        # Extract individual agent scores
        agent_scores = self._extract_agent_scores(agent_results)
        
        # Calculate weighted Reality Score™
        reality_score = self._calculate_reality_score(agent_scores)
        
        # Determine manipulation level
        manipulation_level = self._classify_manipulation_level(reality_score)
        
        # Calculate overall confidence
        confidence = self._calculate_overall_confidence(agent_results, agent_scores)
        
        # Aggregate evidence from all agents
        aggregated_evidence = self._aggregate_evidence(agent_results, agent_scores)
        
        # Combine data sources
        data_sources = self._combine_data_sources(agent_results)
        
        # Create comprehensive analysis summary
        analysis_summary = self._create_analysis_summary(query, reality_score, manipulation_level, agent_scores)
        
        # Create final evidence dictionary
        evidence = {
            'reality_score': reality_score,
            'manipulation_level': manipulation_level,
            'agent_scores': agent_scores,
            'score_breakdown': self._create_score_breakdown(agent_scores),
            'aggregated_evidence': aggregated_evidence,
            'analysis_summary': analysis_summary,
            'key_findings': self._extract_key_findings(agent_results, reality_score),
            'confidence_factors': self._get_confidence_factors(agent_results, confidence)
        }
        
        return {
            'reality_score': reality_score,
            'confidence': confidence,
            'evidence': evidence,
            'data_sources': data_sources
        }
        
    def _extract_agent_scores(self, agent_results: Dict[str, Any]) -> Dict[str, float]:
        """Extract scores from individual agent results"""
        
        agent_scores = {}
        
        # Bot Detection Agent - authenticity score (higher = more authentic)
        bot_result = agent_results.get('bot-detector', {})
        if bot_result:
            # Bot detector returns authenticity score (100 - bot_percentage)
            agent_scores['bot_detector'] = bot_result.get('score', 50.0)
        else:
            # Fallback if bot detector failed
            agent_scores['bot_detector'] = 50.0
            
        # Trend Analysis Agent - organic score (higher = more organic)
        trend_result = agent_results.get('trend-analyzer', {})
        if trend_result:
            agent_scores['trend_analyzer'] = trend_result.get('score', 50.0)
        else:
            agent_scores['trend_analyzer'] = 50.0
            
        # Review Validator Agent - authenticity score (higher = more authentic)
        review_result = agent_results.get('review-validator', {})
        if review_result:
            agent_scores['review_validator'] = review_result.get('score', 50.0)
        else:
            agent_scores['review_validator'] = 50.0
            
        # Paid Promotion Agent - transparency score (higher = more transparent)
        promotion_result = agent_results.get('paid-promotion', {})
        if promotion_result:
            agent_scores['paid_promotion'] = promotion_result.get('score', 50.0)
        else:
            agent_scores['paid_promotion'] = 50.0
            
        return agent_scores
        
    def _calculate_reality_score(self, agent_scores: Dict[str, float]) -> float:
        """Calculate weighted Reality Score™ using exact weights from DETAILED_DESIGN.md"""
        
        # Weighted average calculation from DETAILED_DESIGN.md Section 4.6
        reality_score = (
            agent_scores.get('bot_detector', 50.0) * self.bot_weight +
            agent_scores.get('trend_analyzer', 50.0) * self.trend_weight +
            agent_scores.get('review_validator', 50.0) * self.review_weight +
            agent_scores.get('paid_promotion', 50.0) * self.promotion_weight
        )
        
        # Ensure bounds
        return max(min(reality_score, 100.0), 0.0)
        
    def _classify_manipulation_level(self, reality_score: float) -> str:
        """Classify manipulation level based on Reality Score™"""
        
        # From DETAILED_DESIGN.md Section 1.2
        if reality_score >= 67:
            return 'GREEN'  # Authentic Engagement
        elif reality_score >= 34:
            return 'YELLOW'  # Mixed Signals
        else:
            return 'RED'  # Heavily Manipulated
            
    def _calculate_overall_confidence(self, agent_results: Dict[str, Any], agent_scores: Dict[str, float]) -> float:
        """Calculate overall confidence based on individual agent confidences"""
        
        confidences = []
        
        # Collect individual agent confidences
        for agent_type in ['bot-detector', 'trend-analyzer', 'review-validator', 'paid-promotion']:
            agent_result = agent_results.get(agent_type, {})
            if agent_result and 'confidence' in agent_result:
                confidences.append(agent_result['confidence'])
                
        if not confidences:
            return 70.0  # Default confidence
            
        # Calculate weighted average confidence
        base_confidence = sum(confidences) / len(confidences)
        
        # Adjust confidence based on score consistency
        score_values = list(agent_scores.values())
        if len(score_values) > 1:
            score_variance = self._calculate_variance(score_values)
            # Lower variance (more consistent scores) increases confidence
            consistency_bonus = max(0, 10 - (score_variance / 10))
            base_confidence += consistency_bonus
            
        # Adjust confidence based on number of agents that responded
        agent_count_bonus = (len(confidences) - 1) * 2  # +2% per additional agent beyond first
        base_confidence += agent_count_bonus
        
        return max(min(base_confidence, 98.0), 60.0)
        
    def _calculate_variance(self, values: List[float]) -> float:
        """Calculate variance of a list of values"""
        if len(values) < 2:
            return 0.0
            
        mean = sum(values) / len(values)
        variance = sum((x - mean) ** 2 for x in values) / len(values)
        return variance
        
    def _aggregate_evidence(self, agent_results: Dict[str, Any], agent_scores: Dict[str, float]) -> Dict[str, Any]:
        """Aggregate evidence from all agents"""
        
        aggregated = {
            'bot_detection': {},
            'trend_analysis': {},
            'review_validation': {},
            'paid_promotion': {},
            'cross_agent_patterns': {}
        }
        
        # Bot Detection Evidence
        bot_result = agent_results.get('bot-detector', {})
        if bot_result and 'evidence' in bot_result:
            evidence = bot_result['evidence']
            aggregated['bot_detection'] = {
                'bot_percentage': 100 - agent_scores.get('bot_detector', 50.0),
                'total_accounts_analyzed': evidence.get('total_accounts_analyzed', 0),
                'suspicious_accounts': evidence.get('suspicious_accounts', 0),
                'key_indicators': evidence.get('detection_methods', [])
            }
            
        # Trend Analysis Evidence
        trend_result = agent_results.get('trend-analyzer', {})
        if trend_result and 'evidence' in trend_result:
            evidence = trend_result['evidence']
            aggregated['trend_analysis'] = {
                'organic_score': agent_scores.get('trend_analyzer', 50.0),
                'velocity_analysis': evidence.get('velocity_analysis', {}),
                'spike_analysis': evidence.get('spike_analysis', {}),
                'manipulation_indicators': evidence.get('manipulation_indicators', [])
            }
            
        # Review Validation Evidence
        review_result = agent_results.get('review-validator', {})
        if review_result and 'evidence' in review_result:
            evidence = review_result['evidence']
            aggregated['review_validation'] = {
                'authenticity_score': agent_scores.get('review_validator', 50.0),
                'total_reviews_analyzed': evidence.get('total_reviews_analyzed', 0),
                'temporal_analysis': evidence.get('temporal_analysis', {}),
                'manipulation_indicators': evidence.get('manipulation_indicators', [])
            }
            
        # Paid Promotion Evidence
        promotion_result = agent_results.get('paid-promotion', {})
        if promotion_result and 'evidence' in promotion_result:
            evidence = promotion_result['evidence']
            aggregated['paid_promotion'] = {
                'transparency_score': agent_scores.get('paid_promotion', 50.0),
                'total_content_analyzed': evidence.get('total_content_analyzed', 0),
                'disclosure_analysis': evidence.get('disclosure_analysis', {}),
                'manipulation_indicators': evidence.get('manipulation_indicators', [])
            }
            
        # Cross-Agent Pattern Analysis
        aggregated['cross_agent_patterns'] = self._analyze_cross_agent_patterns(agent_results, agent_scores)
        
        return aggregated
        
    def _analyze_cross_agent_patterns(self, agent_results: Dict[str, Any], agent_scores: Dict[str, float]) -> Dict[str, Any]:
        """Analyze patterns across multiple agents"""
        
        patterns = {
            'consistency_analysis': {},
            'correlated_indicators': [],
            'conflicting_signals': [],
            'overall_pattern': ''
        }
        
        # Analyze score consistency
        scores = list(agent_scores.values())
        if len(scores) > 1:
            score_range = max(scores) - min(scores)
            avg_score = sum(scores) / len(scores)
            
            patterns['consistency_analysis'] = {
                'score_range': round(score_range, 1),
                'average_score': round(avg_score, 1),
                'consistency_level': 'high' if score_range < 20 else 'medium' if score_range < 40 else 'low'
            }
            
        # Look for correlated indicators
        manipulation_indicators = []
        for agent_type, result in agent_results.items():
            if 'evidence' in result and 'manipulation_indicators' in result['evidence']:
                manipulation_indicators.extend(result['evidence']['manipulation_indicators'])
                
        # Count common themes
        indicator_themes = {}
        for indicator in manipulation_indicators:
            # Simple keyword matching for themes
            if any(word in indicator.lower() for word in ['coordinated', 'coordination', 'timing']):
                indicator_themes['coordination'] = indicator_themes.get('coordination', 0) + 1
            if any(word in indicator.lower() for word in ['artificial', 'fake', 'bot']):
                indicator_themes['artificial_activity'] = indicator_themes.get('artificial_activity', 0) + 1
            if any(word in indicator.lower() for word in ['spike', 'surge', 'burst']):
                indicator_themes['suspicious_spikes'] = indicator_themes.get('suspicious_spikes', 0) + 1
                
        patterns['correlated_indicators'] = [
            f"{theme}: {count} agents detected"
            for theme, count in indicator_themes.items() if count > 1
        ]
        
        # Determine overall pattern
        avg_score = sum(agent_scores.values()) / len(agent_scores) if agent_scores else 50
        if avg_score < 30:
            patterns['overall_pattern'] = 'Heavy manipulation detected across multiple vectors'
        elif avg_score < 50:
            patterns['overall_pattern'] = 'Significant manipulation indicators present'
        elif avg_score < 70:
            patterns['overall_pattern'] = 'Mixed signals with some manipulation detected'
        else:
            patterns['overall_pattern'] = 'Mostly authentic with minimal manipulation'
            
        return patterns
        
    def _combine_data_sources(self, agent_results: Dict[str, Any]) -> List[str]:
        """Combine data sources from all agents"""
        
        all_sources = set()
        
        for agent_type, result in agent_results.items():
            if 'data_sources' in result:
                all_sources.update(result['data_sources'])
                
        # Add aggregation-specific sources
        all_sources.add('multi_agent_aggregation')
        all_sources.add('weighted_scoring_algorithm')
        
        return sorted(list(all_sources))
        
    def _create_analysis_summary(self, query: str, reality_score: float, manipulation_level: str, agent_scores: Dict[str, float]) -> Dict[str, Any]:
        """Create comprehensive analysis summary"""
        
        return {
            'query_analyzed': query,
            'final_reality_score': round(reality_score, 1),
            'manipulation_level': manipulation_level,
            'manipulation_percentage': round(100 - reality_score, 1),
            'authenticity_percentage': round(reality_score, 1),
            'agent_contributions': {
                'bot_detection_contribution': round(agent_scores.get('bot_detector', 50.0) * self.bot_weight, 1),
                'trend_analysis_contribution': round(agent_scores.get('trend_analyzer', 50.0) * self.trend_weight, 1),
                'review_validation_contribution': round(agent_scores.get('review_validator', 50.0) * self.review_weight, 1),
                'paid_promotion_contribution': round(agent_scores.get('paid_promotion', 50.0) * self.promotion_weight, 1)
            },
            'scoring_methodology': {
                'bot_detection_weight': f"{self.bot_weight * 100}%",
                'trend_analysis_weight': f"{self.trend_weight * 100}%",
                'review_validation_weight': f"{self.review_weight * 100}%",
                'paid_promotion_weight': f"{self.promotion_weight * 100}%"
            },
            'analysis_timestamp': datetime.now().isoformat(),
            'classification_thresholds': {
                'green_zone': '67-100% (Authentic Engagement)',
                'yellow_zone': '34-66% (Mixed Signals)',
                'red_zone': '0-33% (Heavily Manipulated)'
            }
        }
        
    def _create_score_breakdown(self, agent_scores: Dict[str, float]) -> Dict[str, Any]:
        """Create detailed score breakdown"""
        
        return {
            'individual_scores': {
                'bot_detection_score': round(agent_scores.get('bot_detector', 50.0), 1),
                'trend_analysis_score': round(agent_scores.get('trend_analyzer', 50.0), 1),
                'review_validation_score': round(agent_scores.get('review_validator', 50.0), 1),
                'paid_promotion_score': round(agent_scores.get('paid_promotion', 50.0), 1)
            },
            'weighted_contributions': {
                'bot_detection_weighted': round(agent_scores.get('bot_detector', 50.0) * self.bot_weight, 1),
                'trend_analysis_weighted': round(agent_scores.get('trend_analyzer', 50.0) * self.trend_weight, 1),
                'review_validation_weighted': round(agent_scores.get('review_validator', 50.0) * self.review_weight, 1),
                'paid_promotion_weighted': round(agent_scores.get('paid_promotion', 50.0) * self.promotion_weight, 1)
            },
            'score_interpretations': {
                'bot_detection': self._interpret_score(agent_scores.get('bot_detector', 50.0), 'authenticity'),
                'trend_analysis': self._interpret_score(agent_scores.get('trend_analyzer', 50.0), 'organic_growth'),
                'review_validation': self._interpret_score(agent_scores.get('review_validator', 50.0), 'review_authenticity'),
                'paid_promotion': self._interpret_score(agent_scores.get('paid_promotion', 50.0), 'transparency')
            }
        }
        
    def _interpret_score(self, score: float, score_type: str) -> str:
        """Interpret individual agent scores"""
        
        if score >= 80:
            level = 'Excellent'
        elif score >= 60:
            level = 'Good'
        elif score >= 40:
            level = 'Moderate'
        elif score >= 20:
            level = 'Poor'
        else:
            level = 'Very Poor'
            
        type_descriptions = {
            'authenticity': f'{level} authenticity detected',
            'organic_growth': f'{level} organic growth patterns',
            'review_authenticity': f'{level} review authenticity',
            'transparency': f'{level} promotional transparency'
        }
        
        return type_descriptions.get(score_type, f'{level} score')
        
    def _extract_key_findings(self, agent_results: Dict[str, Any], reality_score: float) -> List[str]:
        """Extract key findings from all agents"""
        
        findings = []
        
        # Overall finding
        if reality_score < 33:
            findings.append(f"HEAVILY MANIPULATED: {round(100 - reality_score, 1)}% manipulation detected")
        elif reality_score < 67:
            findings.append(f"MIXED SIGNALS: {round(100 - reality_score, 1)}% manipulation indicators present")
        else:
            findings.append(f"MOSTLY AUTHENTIC: {round(reality_score, 1)}% authenticity score")
            
        # Extract top findings from each agent
        for agent_type, result in agent_results.items():
            if 'evidence' in result:
                evidence = result['evidence']
                
                # Bot detection findings
                if agent_type == 'bot-detector' and 'key_findings' in evidence:
                    key_finding = evidence['key_findings'][0] if evidence['key_findings'] else None
                    if key_finding:
                        findings.append(f"Bot Detection: {key_finding}")
                        
                # Trend analysis findings
                elif agent_type == 'trend-analyzer' and 'key_findings' in evidence:
                    key_finding = evidence['key_findings'][0] if evidence['key_findings'] else None
                    if key_finding:
                        findings.append(f"Trend Analysis: {key_finding}")
                        
                # Review validation findings
                elif agent_type == 'review-validator' and 'manipulation_indicators' in evidence:
                    indicators = evidence['manipulation_indicators']
                    if indicators:
                        findings.append(f"Review Analysis: {indicators[0]}")
                        
                # Paid promotion findings
                elif agent_type == 'paid-promotion' and 'manipulation_indicators' in evidence:
                    indicators = evidence['manipulation_indicators']
                    if indicators:
                        findings.append(f"Promotion Analysis: {indicators[0]}")
                        
        return findings[:6]  # Limit to top 6 findings
        
    def _get_confidence_factors(self, agent_results: Dict[str, Any], confidence: float) -> List[str]:
        """Get factors that contribute to confidence level"""
        
        factors = []
        
        # Agent response count
        responding_agents = len([r for r in agent_results.values() if r])
        if responding_agents >= 4:
            factors.append("All agents provided analysis")
        elif responding_agents >= 3:
            factors.append("Most agents provided analysis")
        else:
            factors.append("Limited agent responses")
            
        # Confidence level interpretation
        if confidence >= 90:
            factors.append("Very high confidence in results")
        elif confidence >= 80:
            factors.append("High confidence in results")
        elif confidence >= 70:
            factors.append("Moderate confidence in results")
        else:
            factors.append("Lower confidence due to limited data")
            
        # Data source diversity
        all_sources = set()
        for result in agent_results.values():
            if 'data_sources' in result:
                all_sources.update(result['data_sources'])
                
        if len(all_sources) > 8:
            factors.append("Diverse data sources analyzed")
        elif len(all_sources) > 5:
            factors.append("Multiple data sources used")
        else:
            factors.append("Limited data source diversity")
            
        return factors
        
    def _get_demo_evidence(self, query: str, bot_percentage: int, reality_score: int) -> Dict[str, Any]:
        """Get score aggregator specific demo evidence"""
        
        # Calculate individual agent contributions for demo
        bot_score = 100 - bot_percentage
        trend_score = reality_score + random.randint(-10, 10)
        review_score = reality_score + random.randint(-15, 15)
        promotion_score = reality_score + random.randint(-20, 20)
        
        # Ensure bounds
        trend_score = max(min(trend_score, 100), 0)
        review_score = max(min(review_score, 100), 0)
        promotion_score = max(min(promotion_score, 100), 0)
        
        return {
            'final_reality_score': reality_score,
            'manipulation_level': 'RED' if reality_score < 34 else 'YELLOW' if reality_score < 67 else 'GREEN',
            'agent_scores': {
                'bot_detection': bot_score,
                'trend_analysis': trend_score,
                'review_validation': review_score,
                'paid_promotion': promotion_score
            },
            'weighted_contributions': {
                'bot_detection': round(bot_score * 0.4, 1),
                'trend_analysis': round(trend_score * 0.3, 1),
                'review_validation': round(review_score * 0.2, 1),
                'paid_promotion': round(promotion_score * 0.1, 1)
            },
            'key_findings': [
                f"Reality Score™: {reality_score}% authenticity",
                f"Bot Detection: {bot_percentage}% bot accounts identified",
                f"Trend Analysis: Artificial growth patterns detected",
                f"Review Validation: Suspicious review clustering",
                f"Promotion Analysis: Undisclosed sponsorships found"
            ],
            'scoring_methodology': {
                'algorithm': 'Weighted multi-agent aggregation',
                'weights': 'Bot 40%, Trend 30%, Review 20%, Promotion 10%',
                'classification': f'{reality_score}% = {"RED" if reality_score < 34 else "YELLOW" if reality_score < 67 else "GREEN"} Zone'
            },
            'confidence_factors': [
                'All 5 agents provided analysis',
                'High consistency across agents',
                'Multiple data sources analyzed',
                'Strong manipulation signals detected'
            ]
        }


def main():
    """Main function for running score aggregator agent standalone"""
    agent = ScoreAggregatorAgent()
    
    try:
        agent.start()
        agent.logger.info("Score Aggregator Agent started successfully")
        
        # Keep running until interrupted
        while agent.is_running:
            time.sleep(1)
            
    except KeyboardInterrupt:
        agent.logger.info("Received shutdown signal")
    finally:
        agent.stop()
        agent.logger.info("Score Aggregator Agent stopped")


if __name__ == "__main__":
    main()
