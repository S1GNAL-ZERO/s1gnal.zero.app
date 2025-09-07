#!/usr/bin/env python3
"""
Review Validator FastMCP Server
Exposes ReviewValidatorAgent capabilities as MCP tools for SAM integration
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
from review_validator_agent import ReviewValidatorAgent


class ReviewValidatorMCPServer:
    """FastMCP server wrapping ReviewValidatorAgent functionality"""
    
    def __init__(self):
        self.agent = ReviewValidatorAgent()
        self.mcp = FastMCP("ReviewValidatorServer")
        self._setup_tools()
        
    def _setup_tools(self):
        """Register MCP tools for review validation functionality"""
        
        @self.mcp.tool()
        def validate_review_authenticity(
            analysis_id: str,
            query: str,
            platform: str = "all",
            user_id: str = None
        ) -> Dict[str, Any]:
            """
            Validate the authenticity of review content and detect fake reviews.
            
            Args:
                analysis_id: Unique identifier for this analysis request
                query: Review content to validate for authenticity
                platform: Platform to analyze (amazon, google, yelp, all)
                user_id: Optional user ID for the analysis
                
            Returns:
                Dictionary containing review validation results with authenticity scores
            """
            try:
                # Prepare request data in the format expected by the agent
                request_data = {
                    'analysisId': analysis_id,
                    'query': query,
                    'platform': platform,
                    'userId': user_id
                }
                
                # Process the analysis using the existing agent logic
                result = self.agent.process_analysis_request(request_data)
                
                return {
                    'success': True,
                    'analysis_id': analysis_id,
                    'agent_type': 'review-validator',
                    'result': result
                }
                
            except Exception as e:
                return {
                    'success': False,
                    'analysis_id': analysis_id,
                    'agent_type': 'review-validator',
                    'error': str(e)
                }
        
        @self.mcp.tool()
        def get_review_validator_config() -> Dict[str, Any]:
            """
            Get configuration information for the review validator agent.
            
            Returns:
                Dictionary containing agent configuration and capabilities
            """
            return {
                'agent_type': 'review-validator',
                'capabilities': [
                    'fake_review_detection',
                    'sentiment_consistency_analysis',
                    'reviewer_pattern_analysis',
                    'linguistic_authenticity_check',
                    'temporal_pattern_detection'
                ],
                'supported_platforms': ['amazon', 'google', 'yelp', 'tripadvisor', 'all'],
                'min_confidence_threshold': getattr(self.agent.config, 'REVIEW_VALIDATOR_MIN_CONFIDENCE', 0.7),
                'language_models': getattr(self.agent.config, 'REVIEW_VALIDATOR_LANGUAGES', ['en']),
                'status': 'running' if self.agent.is_running else 'stopped'
            }
            
        @self.mcp.tool()
        def health_check() -> Dict[str, Any]:
            """
            Perform a health check on the review validator agent.
            
            Returns:
                Dictionary containing health status information
            """
            return {
                'agent_type': 'review-validator',
                'status': 'healthy' if self.agent.is_running else 'stopped',
                'last_analysis': getattr(self.agent, 'last_analysis_time', None),
                'total_analyses': getattr(self.agent, 'total_analyses_processed', 0)
            }
            
    def run_server(self):
        """Start the FastMCP server"""
        print("🚀 Starting Review Validator MCP Server")
        print("Available tools:")
        print("  - validate_review_authenticity: Main review validation analysis")  
        print("  - get_review_validator_config: Get agent configuration")
        print("  - health_check: Check agent health status")
        print("📡 Running in stdin/stdout mode")
        
        try:
            # Start the agent
            self.agent.start()
            print("✅ Review Validator Agent started successfully")
            
            # Start the MCP server (uses stdin/stdout)
            self.mcp.run()
            
        except Exception as e:
            print(f"❌ Failed to start server: {e}")
            raise
        finally:
            if self.agent.is_running:
                self.agent.stop()
                print("🛑 Review Validator Agent stopped")


def main():
    """Main entry point for the FastMCP server"""
    server = ReviewValidatorMCPServer()
    server.run_server()


if __name__ == "__main__":
    main()
