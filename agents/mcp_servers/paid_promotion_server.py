#!/usr/bin/env python3
"""
Paid Promotion FastMCP Server
Exposes PaidPromotionAgent capabilities as MCP tools for SAM integration
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
from paid_promotion_agent import PaidPromotionAgent


class PaidPromotionMCPServer:
    """FastMCP server wrapping PaidPromotionAgent functionality"""
    
    def __init__(self):
        self.agent = PaidPromotionAgent()
        self.mcp = FastMCP("PaidPromotionServer")
        self._setup_tools()
        
    def _setup_tools(self):
        """Register MCP tools for paid promotion detection functionality"""
        
        @self.mcp.tool()
        def detect_paid_promotion(
            analysis_id: str,
            query: str,
            platform: str = "all",
            user_id: str = None
        ) -> Dict[str, Any]:
            """
            Analyze content for paid promotion indicators, sponsored content, and affiliate marketing.
            
            Args:
                analysis_id: Unique identifier for this analysis request
                query: Content to analyze for paid promotion patterns
                platform: Platform to analyze (instagram, youtube, twitter, all)
                user_id: Optional user ID for the analysis
                
            Returns:
                Dictionary containing paid promotion analysis results with promotion scores
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
                    'agent_type': 'paid-promotion',
                    'result': result
                }
                
            except Exception as e:
                return {
                    'success': False,
                    'analysis_id': analysis_id,
                    'agent_type': 'paid-promotion',
                    'error': str(e)
                }
        
        @self.mcp.tool()
        def get_paid_promotion_config() -> Dict[str, Any]:
            """
            Get configuration information for the paid promotion agent.
            
            Returns:
                Dictionary containing agent configuration and capabilities
            """
            return {
                'agent_type': 'paid-promotion',
                'capabilities': [
                    'sponsored_content_detection',
                    'affiliate_link_analysis',
                    'disclosure_compliance_check',
                    'brand_partnership_identification',
                    'promotional_language_detection'
                ],
                'supported_platforms': ['instagram', 'youtube', 'twitter', 'tiktok', 'all'],
                'disclosure_keywords': getattr(self.agent.config, 'PAID_PROMOTION_DISCLOSURE_KEYWORDS', []),
                'min_confidence_threshold': getattr(self.agent.config, 'PAID_PROMOTION_MIN_CONFIDENCE', 0.75),
                'status': 'running' if self.agent.is_running else 'stopped'
            }
            
        @self.mcp.tool()
        def health_check() -> Dict[str, Any]:
            """
            Perform a health check on the paid promotion agent.
            
            Returns:
                Dictionary containing health status information
            """
            return {
                'agent_type': 'paid-promotion',
                'status': 'healthy' if self.agent.is_running else 'stopped',
                'last_analysis': getattr(self.agent, 'last_analysis_time', None),
                'total_analyses': getattr(self.agent, 'total_analyses_processed', 0)
            }
            
    def run_server(self):
        """Start the FastMCP server"""
        print("🚀 Starting Paid Promotion MCP Server")
        print("Available tools:")
        print("  - detect_paid_promotion: Main paid promotion analysis")  
        print("  - get_paid_promotion_config: Get agent configuration")
        print("  - health_check: Check agent health status")
        print("📡 Running in stdin/stdout mode")
        
        try:
            # Start the agent
            self.agent.start()
            print("✅ Paid Promotion Agent started successfully")
            
            # Start the MCP server (uses stdin/stdout)
            self.mcp.run()
            
        except Exception as e:
            print(f"❌ Failed to start server: {e}")
            raise
        finally:
            if self.agent.is_running:
                self.agent.stop()
                print("🛑 Paid Promotion Agent stopped")


def main():
    """Main entry point for the FastMCP server"""
    server = PaidPromotionMCPServer()
    server.run_server()


if __name__ == "__main__":
    main()
