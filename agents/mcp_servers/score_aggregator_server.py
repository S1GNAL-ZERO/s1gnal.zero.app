#!/usr/bin/env python3
"""
Score Aggregator FastMCP Server
Exposes ScoreAggregatorAgent capabilities as MCP tools for SAM integration
"""

import sys
import os
import json
import argparse
from pathlib import Path
from typing import Any, Dict

# Add the parent directory to Python path to import our agents
sys.path.insert(0, str(Path(__file__).parent.parent))

from fastmcp import FastMCP
from score_aggregator_agent import ScoreAggregatorAgent


class ScoreAggregatorMCPServer:
    """FastMCP server wrapping ScoreAggregatorAgent functionality"""
    
    def __init__(self):
        self.agent = ScoreAggregatorAgent()
        self.mcp = FastMCP("ScoreAggregatorServer")
        self._setup_tools()
        
    def _setup_tools(self):
        """Register MCP tools for score aggregation functionality"""
        
        @self.mcp.tool()
        def aggregate_reality_scores(
            analysis_id: str,
            agent_results: Dict[str, Any],
            user_id: str = None
        ) -> Dict[str, Any]:
            """
            Aggregate analysis results from all agents into a final Reality Score.
            
            Args:
                analysis_id: Unique identifier for this analysis request
                agent_results: Dictionary containing results from all analysis agents
                user_id: Optional user ID for the analysis
                
            Returns:
                Dictionary containing final aggregated Reality Score and analysis
            """
            try:
                # Prepare request data in the format expected by the agent
                request_data = {
                    'analysisId': analysis_id,
                    'agentResults': agent_results,
                    'userId': user_id
                }
                
                # Process the analysis using the existing agent logic
                result = self.agent.process_analysis_request(request_data)
                
                return {
                    'success': True,
                    'analysis_id': analysis_id,
                    'agent_type': 'score-aggregator',
                    'result': result
                }
                
            except Exception as e:
                return {
                    'success': False,
                    'analysis_id': analysis_id,
                    'agent_type': 'score-aggregator',
                    'error': str(e)
                }
        
        @self.mcp.tool()
        def calculate_reality_score(
            bot_score: float,
            trend_score: float,
            review_score: float,
            promotion_score: float,
            analysis_id: str,
            user_id: str = None
        ) -> Dict[str, Any]:
            """
            Calculate final Reality Score from individual agent scores.
            
            Args:
                bot_score: Score from bot detection agent (0-100)
                trend_score: Score from trend analysis agent (0-100)
                review_score: Score from review validation agent (0-100)
                promotion_score: Score from paid promotion agent (0-100)
                analysis_id: Unique identifier for this analysis
                user_id: Optional user ID for the analysis
                
            Returns:
                Dictionary containing calculated Reality Score and breakdown
            """
            try:
                # Prepare individual scores
                agent_results = {
                    'bot-detector': {'realityScore': bot_score},
                    'trend-analyzer': {'realityScore': trend_score},
                    'review-validator': {'realityScore': review_score},
                    'paid-promotion': {'realityScore': promotion_score}
                }
                
                # Use the aggregate function
                return self.aggregate_reality_scores(analysis_id, agent_results, user_id)
                
            except Exception as e:
                return {
                    'success': False,
                    'analysis_id': analysis_id,
                    'agent_type': 'score-aggregator',
                    'error': str(e)
                }
        
        @self.mcp.tool()
        def get_score_aggregator_config() -> Dict[str, Any]:
            """
            Get configuration information for the score aggregator agent.
            
            Returns:
                Dictionary containing agent configuration and capabilities
            """
            return {
                'agent_type': 'score-aggregator',
                'capabilities': [
                    'weighted_score_aggregation',
                    'confidence_calculation',
                    'reality_score_computation',
                    'agent_result_validation',
                    'final_analysis_compilation'
                ],
                'agent_weights': {
                    'bot-detector': getattr(self.agent.config, 'SCORE_AGGREGATOR_BOT_WEIGHT', 0.40),
                    'trend-analyzer': getattr(self.agent.config, 'SCORE_AGGREGATOR_TREND_WEIGHT', 0.30),
                    'review-validator': getattr(self.agent.config, 'SCORE_AGGREGATOR_REVIEW_WEIGHT', 0.20),
                    'paid-promotion': getattr(self.agent.config, 'SCORE_AGGREGATOR_PROMOTION_WEIGHT', 0.10)
                },
                'min_required_agents': getattr(self.agent.config, 'SCORE_AGGREGATOR_MIN_AGENTS', 3),
                'status': 'running' if self.agent.is_running else 'stopped'
            }
            
        @self.mcp.tool()
        def health_check() -> Dict[str, Any]:
            """
            Perform a health check on the score aggregator agent.
            
            Returns:
                Dictionary containing health status information
            """
            return {
                'agent_type': 'score-aggregator',
                'status': 'healthy' if self.agent.is_running else 'stopped',
                'last_analysis': getattr(self.agent, 'last_analysis_time', None),
                'total_analyses': getattr(self.agent, 'total_analyses_processed', 0)
            }
            
    def run_server(self):
        """Start the FastMCP server"""
        print("🚀 Starting Score Aggregator MCP Server")
        print("Available tools:")
        print("  - aggregate_reality_scores: Main score aggregation from all agents")  
        print("  - calculate_reality_score: Calculate final Reality Score from individual scores")
        print("  - get_score_aggregator_config: Get agent configuration")
        print("  - health_check: Check agent health status")
        print("📡 Running in stdin/stdout mode")
        
        try:
            # Start the agent
            self.agent.start()
            print("✅ Score Aggregator Agent started successfully")
            
            # Start the MCP server (uses stdin/stdout)
            self.mcp.run()
            
        except Exception as e:
            print(f"❌ Failed to start server: {e}")
            raise
        finally:
            if self.agent.is_running:
                self.agent.stop()
                print("🛑 Score Aggregator Agent stopped")


def main():
    """Main entry point for the FastMCP server"""
    server = ScoreAggregatorMCPServer()
    server.run_server()


if __name__ == "__main__":
    main()
