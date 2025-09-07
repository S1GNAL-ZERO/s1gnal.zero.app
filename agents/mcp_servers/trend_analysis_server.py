#!/usr/bin/env python3
"""
Trend Analysis FastMCP Server
Exposes TrendAnalysisAgent capabilities as MCP tools for SAM integration
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
from trend_analysis_agent import TrendAnalysisAgent


class TrendAnalysisMCPServer:
    """FastMCP server wrapping TrendAnalysisAgent functionality"""
    
    def __init__(self):
        self.agent = TrendAnalysisAgent()
        self.mcp = FastMCP("TrendAnalysisServer")
        self._setup_tools()
        
    def _setup_tools(self):
        """Register MCP tools for trend analysis functionality"""
        
        @self.mcp.tool()
        def analyze_trend_patterns(
            analysis_id: str,
            query: str,
            platform: str = "all",
            user_id: str = None
        ) -> Dict[str, Any]:
            """
            Analyze content for trending patterns, viral mechanics, and artificial amplification.
            
            Args:
                analysis_id: Unique identifier for this analysis request
                query: Content to analyze for trend patterns
                platform: Platform to analyze (twitter, reddit, instagram, all)
                user_id: Optional user ID for the analysis
                
            Returns:
                Dictionary containing trend analysis results with manipulation scores
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
                    'agent_type': 'trend-analyzer',
                    'result': result
                }
                
            except Exception as e:
                return {
                    'success': False,
                    'analysis_id': analysis_id,
                    'agent_type': 'trend-analyzer',
                    'error': str(e)
                }
        
        @self.mcp.tool()
        def get_trend_analysis_config() -> Dict[str, Any]:
            """
            Get configuration information for the trend analysis agent.
            
            Returns:
                Dictionary containing agent configuration and capabilities
            """
            return {
                'agent_type': 'trend-analyzer',
                'capabilities': [
                    'viral_pattern_detection',
                    'engagement_velocity_analysis',
                    'hashtag_trending_analysis',
                    'artificial_amplification_detection',
                    'organic_vs_paid_classification'
                ],
                'supported_platforms': ['twitter', 'reddit', 'instagram', 'tiktok', 'all'],
                'analysis_window_hours': getattr(self.agent.config, 'TREND_ANALYZER_WINDOW_HOURS', 24),
                'min_engagement_threshold': getattr(self.agent.config, 'TREND_ANALYZER_MIN_ENGAGEMENT', 100),
                'status': 'running' if self.agent.is_running else 'stopped'
            }
            
        @self.mcp.tool()
        def health_check() -> Dict[str, Any]:
            """
            Perform a health check on the trend analysis agent.
            
            Returns:
                Dictionary containing health status information
            """
            return {
                'agent_type': 'trend-analyzer',
                'status': 'healthy' if self.agent.is_running else 'stopped',
                'last_analysis': getattr(self.agent, 'last_analysis_time', None),
                'total_analyses': getattr(self.agent, 'total_analyses_processed', 0)
            }
            
    def run_server(self):
        """Start the FastMCP server"""
        print("🚀 Starting Trend Analysis MCP Server")
        print("Available tools:")
        print("  - analyze_trend_patterns: Main trend analysis")  
        print("  - get_trend_analysis_config: Get agent configuration")
        print("  - health_check: Check agent health status")
        print("📡 Running in stdin/stdout mode")
        
        try:
            # Start the agent
            self.agent.start()
            print("✅ Trend Analysis Agent started successfully")
            
            # Start the MCP server (uses stdin/stdout)
            self.mcp.run()
            
        except Exception as e:
            print(f"❌ Failed to start server: {e}")
            raise
        finally:
            if self.agent.is_running:
                self.agent.stop()
                print("🛑 Trend Analysis Agent stopped")


def main():
    """Main entry point for the FastMCP server"""
    server = TrendAnalysisMCPServer()
    server.run_server()


if __name__ == "__main__":
    main()
