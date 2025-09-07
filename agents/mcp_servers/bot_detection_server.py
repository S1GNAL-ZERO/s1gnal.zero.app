#!/usr/bin/env python3
"""
Bot Detection FastMCP Server
Exposes BotDetectionAgent capabilities as MCP tools for SAM integration
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
from bot_detection_agent import BotDetectionAgent


class BotDetectionMCPServer:
    """FastMCP server wrapping BotDetectionAgent functionality"""
    
    def __init__(self):
        self.agent = BotDetectionAgent()
        self.mcp = FastMCP("BotDetectionServer")
        self._setup_tools()
        
    def _setup_tools(self):
        """Register MCP tools for bot detection functionality"""
        
        @self.mcp.tool()
        def analyze_bot_patterns(
            analysis_id: str,
            query: str,
            platform: str = "all",
            user_id: str = None
        ) -> Dict[str, Any]:
            """
            Analyze content for bot-like behavior patterns and automated activity.
            
            Args:
                analysis_id: Unique identifier for this analysis request
                query: Content to analyze for bot patterns
                platform: Platform to analyze (twitter, reddit, instagram, all)
                user_id: Optional user ID for the analysis
                
            Returns:
                Dictionary containing bot analysis results with authenticity scores
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
                    'agent_type': 'bot-detector',
                    'result': result
                }
                
            except Exception as e:
                return {
                    'success': False,
                    'analysis_id': analysis_id,
                    'agent_type': 'bot-detector',
                    'error': str(e)
                }
        
        @self.mcp.tool()
        def get_bot_analysis_config() -> Dict[str, Any]:
            """
            Get configuration information for the bot detection agent.
            
            Returns:
                Dictionary containing agent configuration and capabilities
            """
            return {
                'agent_type': 'bot-detector',
                'capabilities': [
                    'account_age_analysis',
                    'profile_completeness_check', 
                    'username_pattern_analysis',
                    'posting_behavior_analysis',
                    'network_cluster_detection'
                ],
                'supported_platforms': ['twitter', 'reddit', 'instagram', 'all'],
                'min_confidence': self.agent.min_confidence,
                'sample_size': self.agent.sample_size,
                'status': 'running' if self.agent.is_running else 'stopped'
            }
            
        @self.mcp.tool()
        def health_check() -> Dict[str, Any]:
            """
            Perform a health check on the bot detection agent.
            
            Returns:
                Dictionary containing health status information
            """
            return {
                'agent_type': 'bot-detector',
                'status': 'healthy' if self.agent.is_running else 'stopped',
                'last_analysis': getattr(self.agent, 'last_analysis_time', None),
                'total_analyses': getattr(self.agent, 'total_analyses_processed', 0)
            }
            
    def run_server(self):
        """Start the FastMCP server"""
        print("🚀 Starting Bot Detection MCP Server")
        print("Available tools:")
        print("  - analyze_bot_patterns: Main bot detection analysis")  
        print("  - get_bot_analysis_config: Get agent configuration")
        print("  - health_check: Check agent health status")
        print("📡 Running in stdin/stdout mode")
        
        try:
            # Start the agent
            self.agent.start()
            print("✅ Bot Detection Agent started successfully")
            
            # Start the MCP server (uses stdin/stdout)
            self.mcp.run()
            
        except Exception as e:
            print(f"❌ Failed to start server: {e}")
            raise
        finally:
            if self.agent.is_running:
                self.agent.stop()
                print("🛑 Bot Detection Agent stopped")


def main():
    """Main entry point for the FastMCP server"""
    server = BotDetectionMCPServer()
    server.run_server()


if __name__ == "__main__":
    main()
