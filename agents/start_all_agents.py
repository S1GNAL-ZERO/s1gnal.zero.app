"""
Agent Orchestration Script for S1GNAL.ZERO
Starts all 5 agents in parallel for the multi-agent analysis system.
"""

import os
import sys
import time
import signal
import logging
import threading
from typing import List, Dict, Any
from concurrent.futures import ThreadPoolExecutor, as_completed

# Add the agents directory to Python path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Import all agents
from bot_detection_agent import BotDetectionAgent
from trend_analysis_agent import TrendAnalysisAgent
from review_validator_agent import ReviewValidatorAgent
from paid_promotion_agent import PaidPromotionAgent
from score_aggregator_agent import ScoreAggregatorAgent


class AgentOrchestrator:
    """
    Orchestrates the startup and management of all S1GNAL.ZERO agents
    """
    
    def __init__(self):
        self.agents = []
        self.agent_threads = []
        self.shutdown_event = threading.Event()
        self.logger = self._setup_logging()
        
        # Agent startup order (Score Aggregator last to receive results)
        self.agent_classes = [
            BotDetectionAgent,
            TrendAnalysisAgent,
            ReviewValidatorAgent,
            PaidPromotionAgent,
            ScoreAggregatorAgent  # Last to aggregate results from others
        ]
        
    def _setup_logging(self) -> logging.Logger:
        """Setup logging for the orchestrator"""
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
            handlers=[
                logging.StreamHandler(),
                logging.FileHandler('agents_orchestrator.log')
            ]
        )
        return logging.getLogger('AgentOrchestrator')
        
    def start_all_agents(self) -> bool:
        """Start all agents in parallel"""
        self.logger.info("🚀 Starting S1GNAL.ZERO Multi-Agent System...")
        self.logger.info("=" * 60)
        
        try:
            # Initialize all agents
            for agent_class in self.agent_classes:
                try:
                    agent = agent_class()
                    self.agents.append(agent)
                    self.logger.info(f"✅ Initialized {agent.agent_type} agent")
                except Exception as e:
                    self.logger.error(f"❌ Failed to initialize {agent_class.__name__}: {e}")
                    return False
                    
            # Start agents with staggered startup
            self.logger.info("\n🔄 Starting agents with staggered startup...")
            
            for i, agent in enumerate(self.agents):
                try:
                    # Start agent in separate thread
                    thread = threading.Thread(
                        target=self._run_agent,
                        args=(agent,),
                        name=f"{agent.agent_type}-thread",
                        daemon=True
                    )
                    thread.start()
                    self.agent_threads.append(thread)
                    
                    self.logger.info(f"🟢 Started {agent.agent_type} agent")
                    
                    # Staggered startup delay (except for last agent)
                    if i < len(self.agents) - 1:
                        time.sleep(2)  # 2 second delay between agent starts
                        
                except Exception as e:
                    self.logger.error(f"❌ Failed to start {agent.agent_type}: {e}")
                    return False
                    
            # Wait for all agents to be ready
            self.logger.info("\n⏳ Waiting for all agents to be ready...")
            self._wait_for_agents_ready()
            
            self.logger.info("\n" + "=" * 60)
            self.logger.info("🎉 ALL AGENTS STARTED SUCCESSFULLY!")
            self.logger.info("=" * 60)
            self._print_agent_status()
            
            return True
            
        except Exception as e:
            self.logger.error(f"💥 Critical error during agent startup: {e}")
            self.stop_all_agents()
            return False
            
    def _run_agent(self, agent) -> None:
        """Run an individual agent"""
        try:
            agent.start()
            self.logger.info(f"🔄 {agent.agent_type} agent is running...")
            
            # Keep agent running until shutdown
            while not self.shutdown_event.is_set() and agent.is_running:
                time.sleep(1)
                
        except Exception as e:
            self.logger.error(f"💥 {agent.agent_type} agent crashed: {e}")
        finally:
            try:
                agent.stop()
                self.logger.info(f"🛑 {agent.agent_type} agent stopped")
            except Exception as e:
                self.logger.error(f"⚠️ Error stopping {agent.agent_type}: {e}")
                
    def _wait_for_agents_ready(self, timeout: int = 30) -> bool:
        """Wait for all agents to be ready"""
        start_time = time.time()
        
        while time.time() - start_time < timeout:
            ready_count = 0
            
            for agent in self.agents:
                if hasattr(agent, 'is_running') and agent.is_running:
                    ready_count += 1
                    
            if ready_count == len(self.agents):
                self.logger.info(f"✅ All {len(self.agents)} agents are ready!")
                return True
                
            self.logger.info(f"⏳ {ready_count}/{len(self.agents)} agents ready...")
            time.sleep(2)
            
        self.logger.warning(f"⚠️ Timeout waiting for agents to be ready")
        return False
        
    def _print_agent_status(self) -> None:
        """Print status of all agents"""
        self.logger.info("\n📊 AGENT STATUS:")
        self.logger.info("-" * 40)
        
        for agent in self.agents:
            status = "🟢 RUNNING" if (hasattr(agent, 'is_running') and agent.is_running) else "🔴 STOPPED"
            self.logger.info(f"{agent.agent_type:20} | {status}")
            
        self.logger.info("-" * 40)
        
    def stop_all_agents(self) -> None:
        """Stop all agents gracefully"""
        self.logger.info("\n🛑 Stopping all agents...")
        
        # Signal shutdown
        self.shutdown_event.set()
        
        # Stop all agents
        for agent in self.agents:
            try:
                if hasattr(agent, 'stop'):
                    agent.stop()
                    self.logger.info(f"✅ Stopped {agent.agent_type} agent")
            except Exception as e:
                self.logger.error(f"⚠️ Error stopping {agent.agent_type}: {e}")
                
        # Wait for threads to finish
        for thread in self.agent_threads:
            try:
                thread.join(timeout=5)
                if thread.is_alive():
                    self.logger.warning(f"⚠️ Thread {thread.name} did not stop gracefully")
            except Exception as e:
                self.logger.error(f"⚠️ Error joining thread {thread.name}: {e}")
                
        self.logger.info("🏁 All agents stopped")
        
    def get_agent_health(self) -> Dict[str, Any]:
        """Get health status of all agents"""
        health_status = {
            'total_agents': len(self.agents),
            'running_agents': 0,
            'agent_details': []
        }
        
        for agent in self.agents:
            is_running = hasattr(agent, 'is_running') and agent.is_running
            if is_running:
                health_status['running_agents'] += 1
                
            health_status['agent_details'].append({
                'agent_type': agent.agent_type,
                'status': 'running' if is_running else 'stopped',
                'uptime': getattr(agent, 'uptime', 0)
            })
            
        health_status['health_percentage'] = (
            health_status['running_agents'] / health_status['total_agents'] * 100
            if health_status['total_agents'] > 0 else 0
        )
        
        return health_status
        
    def restart_failed_agents(self) -> None:
        """Restart any failed agents"""
        self.logger.info("🔄 Checking for failed agents...")
        
        for i, agent in enumerate(self.agents):
            if not (hasattr(agent, 'is_running') and agent.is_running):
                self.logger.warning(f"⚠️ {agent.agent_type} agent is not running, restarting...")
                
                try:
                    # Stop the old agent
                    agent.stop()
                    
                    # Create new agent instance
                    new_agent = self.agent_classes[i]()
                    self.agents[i] = new_agent
                    
                    # Start new agent
                    thread = threading.Thread(
                        target=self._run_agent,
                        args=(new_agent,),
                        name=f"{new_agent.agent_type}-thread-restart",
                        daemon=True
                    )
                    thread.start()
                    
                    self.logger.info(f"✅ Restarted {new_agent.agent_type} agent")
                    
                except Exception as e:
                    self.logger.error(f"❌ Failed to restart {agent.agent_type}: {e}")


def signal_handler(signum, frame):
    """Handle shutdown signals"""
    print(f"\n🛑 Received signal {signum}, shutting down agents...")
    if 'orchestrator' in globals():
        orchestrator.stop_all_agents()
    sys.exit(0)


def main():
    """Main function to start the agent orchestrator"""
    global orchestrator
    
    # Setup signal handlers
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    print("🚀 S1GNAL.ZERO Multi-Agent System")
    print("=" * 50)
    print("Starting all 5 agents for real-time analysis...")
    print("Press Ctrl+C to stop all agents")
    print("=" * 50)
    
    # Create and start orchestrator
    orchestrator = AgentOrchestrator()
    
    try:
        # Start all agents
        if orchestrator.start_all_agents():
            print("\n✅ All agents started successfully!")
            print("🔍 System ready for analysis requests...")
            
            # Keep running and monitor agents
            while True:
                time.sleep(30)  # Check every 30 seconds
                
                # Check agent health
                health = orchestrator.get_agent_health()
                if health['running_agents'] < health['total_agents']:
                    print(f"⚠️ Only {health['running_agents']}/{health['total_agents']} agents running")
                    orchestrator.restart_failed_agents()
                    
        else:
            print("❌ Failed to start agents")
            sys.exit(1)
            
    except KeyboardInterrupt:
        print("\n🛑 Shutdown requested by user")
    except Exception as e:
        print(f"💥 Critical error: {e}")
    finally:
        orchestrator.stop_all_agents()
        print("👋 Goodbye!")


if __name__ == "__main__":
    main()
