#!/usr/bin/env python3
"""
Start All FastMCP Servers
Orchestrates the startup of all S1GNAL.ZERO agent MCP servers
"""

import os
import sys
import time
import signal
import logging
import threading
import subprocess
from pathlib import Path
from typing import List, Dict, Any

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('mcp_servers.log')
    ]
)
logger = logging.getLogger('MCPServerOrchestrator')


class MCPServerOrchestrator:
    """Orchestrates all FastMCP servers for S1GNAL.ZERO agents"""
    
    def __init__(self):
        self.servers = []
        self.processes = []
        self.shutdown_event = threading.Event()
        self.server_configs = [
            {
                'name': 'BotDetectionServer',
                'script': 'bot_detection_server.py',
                'port': 8001,
                'description': 'Bot Detection Analysis'
            },
            {
                'name': 'TrendAnalysisServer',
                'script': 'trend_analysis_server.py', 
                'port': 8002,
                'description': 'Trend Pattern Analysis'
            },
            {
                'name': 'ReviewValidatorServer',
                'script': 'review_validator_server.py',
                'port': 8003,
                'description': 'Review Authenticity Validation'
            },
            {
                'name': 'PaidPromotionServer',
                'script': 'paid_promotion_server.py',
                'port': 8004,
                'description': 'Paid Promotion Detection'
            },
            {
                'name': 'ScoreAggregatorServer',
                'script': 'score_aggregator_server.py',
                'port': 8005,
                'description': 'Final Score Aggregation'
            }
        ]
        
    def start_all_servers(self) -> bool:
        """Start all MCP servers"""
        logger.info("🚀 Starting S1GNAL.ZERO FastMCP Server Orchestration")
        logger.info("=" * 70)
        
        try:
            # Get the directory where this script is located
            script_dir = Path(__file__).parent
            
            for config in self.server_configs:
                try:
                    script_path = script_dir / config['script']
                    if not script_path.exists():
                        logger.error(f"❌ Script not found: {script_path}")
                        return False
                    
                    # Start the server process
                    # Need to run from agents directory for imports to work
                    agents_dir = script_dir.parent
                    
                    # Set up environment to inherit current Python environment
                    env = os.environ.copy()
                    
                    cmd = [
                        sys.executable,
                        str(script_path.relative_to(agents_dir))
                    ]
                    
                    logger.info(f"🔄 Starting {config['name']} on port {config['port']}")
                    process = subprocess.Popen(
                        cmd,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE,
                        text=True,
                        bufsize=1,
                        universal_newlines=True,
                        cwd=str(agents_dir),  # Run from agents directory
                        env=env  # Inherit environment
                    )
                    
                    self.processes.append({
                        'process': process,
                        'config': config,
                        'started_at': time.time()
                    })
                    
                    logger.info(f"✅ Started {config['name']} (PID: {process.pid})")
                    
                    # Brief delay between server starts
                    time.sleep(2)
                    
                except Exception as e:
                    logger.error(f"❌ Failed to start {config['name']}: {e}")
                    return False
            
            # Wait a bit for all servers to initialize
            logger.info("\n⏳ Waiting for servers to initialize...")
            time.sleep(5)
            
            # Check if all servers are still running
            running_count = 0
            for proc_info in self.processes:
                if proc_info['process'].poll() is None:
                    running_count += 1
                else:
                    logger.error(f"❌ {proc_info['config']['name']} failed to start")
            
            if running_count == len(self.server_configs):
                logger.info("\n" + "=" * 70)
                logger.info("🎉 ALL FASTMCP SERVERS STARTED SUCCESSFULLY!")
                logger.info("=" * 70)
                self._print_server_status()
                return True
            else:
                logger.error(f"❌ Only {running_count}/{len(self.server_configs)} servers running")
                return False
                
        except Exception as e:
            logger.error(f"💥 Critical error during server startup: {e}")
            self.stop_all_servers()
            return False
    
    def _print_server_status(self):
        """Print status of all servers"""
        logger.info("\n📊 MCP SERVER STATUS:")
        logger.info("-" * 50)
        
        for proc_info in self.processes:
            config = proc_info['config']
            process = proc_info['process']
            status = "🟢 RUNNING" if process.poll() is None else "🔴 STOPPED"
            uptime = int(time.time() - proc_info['started_at'])
            
            logger.info(f"{config['name']:20} | Port {config['port']} | {status} | {uptime}s uptime")
            logger.info(f"{'':20} | {config['description']}")
            
        logger.info("-" * 50)
        logger.info("\n🔗 MCP SERVER ENDPOINTS:")
        logger.info("-" * 30)
        for config in self.server_configs:
            logger.info(f"  http://localhost:{config['port']} - {config['name']}")
        logger.info("-" * 30)
    
    def stop_all_servers(self):
        """Stop all MCP servers gracefully"""
        logger.info("\n🛑 Stopping all MCP servers...")
        
        for proc_info in self.processes:
            try:
                process = proc_info['process']
                config = proc_info['config']
                
                if process.poll() is None:  # Process is still running
                    logger.info(f"🔄 Stopping {config['name']}...")
                    process.terminate()
                    
                    # Wait up to 10 seconds for graceful shutdown
                    try:
                        process.wait(timeout=10)
                        logger.info(f"✅ Stopped {config['name']}")
                    except subprocess.TimeoutExpired:
                        logger.warning(f"⚠️ Force killing {config['name']}")
                        process.kill()
                        process.wait()
                        
            except Exception as e:
                logger.error(f"⚠️ Error stopping {proc_info['config']['name']}: {e}")
        
        logger.info("🏁 All MCP servers stopped")
    
    def monitor_servers(self):
        """Monitor server health and restart if needed"""
        logger.info("🔍 Starting server monitoring...")
        
        while not self.shutdown_event.is_set():
            failed_servers = []
            
            for i, proc_info in enumerate(self.processes):
                process = proc_info['process']
                config = proc_info['config']
                
                if process.poll() is not None:  # Process has died
                    logger.error(f"💀 {config['name']} has died (exit code: {process.poll()})")
                    failed_servers.append(i)
            
            # Restart failed servers
            for i in failed_servers:
                proc_info = self.processes[i]
                config = proc_info['config']
                
                try:
                    logger.info(f"🔄 Restarting {config['name']}...")
                    script_path = Path(__file__).parent / config['script']
                    cmd = [
                        sys.executable,
                        str(script_path)
                    ]
                    
                    new_process = subprocess.Popen(
                        cmd,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE,
                        text=True
                    )
                    
                    self.processes[i] = {
                        'process': new_process,
                        'config': config,
                        'started_at': time.time()
                    }
                    
                    logger.info(f"✅ Restarted {config['name']} (PID: {new_process.pid})")
                    
                except Exception as e:
                    logger.error(f"❌ Failed to restart {config['name']}: {e}")
            
            # Check every 30 seconds
            time.sleep(30)
    
    def get_server_health(self) -> Dict[str, Any]:
        """Get health status of all servers"""
        health = {
            'total_servers': len(self.server_configs),
            'running_servers': 0,
            'server_details': []
        }
        
        for proc_info in self.processes:
            config = proc_info['config']
            process = proc_info['process']
            is_running = process.poll() is None
            
            if is_running:
                health['running_servers'] += 1
            
            health['server_details'].append({
                'name': config['name'],
                'port': config['port'],
                'status': 'running' if is_running else 'stopped',
                'uptime': int(time.time() - proc_info['started_at']) if is_running else 0,
                'pid': process.pid if is_running else None
            })
        
        health['health_percentage'] = (health['running_servers'] / health['total_servers'] * 100)
        return health


def signal_handler(signum, frame):
    """Handle shutdown signals"""
    logger.info(f"\n🛑 Received signal {signum}, shutting down all MCP servers...")
    if 'orchestrator' in globals():
        orchestrator.stop_all_servers()
    sys.exit(0)


def main():
    """Main function"""
    global orchestrator
    
    # Setup signal handlers
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    print("🚀 S1GNAL.ZERO FastMCP Server Orchestrator")
    print("=" * 50)
    print("Starting all 5 agent MCP servers...")
    print("Press Ctrl+C to stop all servers")
    print("=" * 50)
    
    orchestrator = MCPServerOrchestrator()
    
    try:
        # Start all servers
        if orchestrator.start_all_servers():
            logger.info("\n✅ All servers started successfully!")
            logger.info("🔍 System ready for SAM integration...")
            
            # Start monitoring in a separate thread
            monitor_thread = threading.Thread(
                target=orchestrator.monitor_servers,
                daemon=True
            )
            monitor_thread.start()
            
            # Keep the main thread alive
            while True:
                time.sleep(60)  # Print status every minute
                health = orchestrator.get_server_health()
                if health['running_servers'] < health['total_servers']:
                    logger.warning(
                        f"⚠️ Health Check: {health['running_servers']}/{health['total_servers']} servers running"
                    )
        else:
            logger.error("❌ Failed to start all servers")
            sys.exit(1)
            
    except KeyboardInterrupt:
        logger.info("\n🛑 Shutdown requested by user")
    except Exception as e:
        logger.error(f"💥 Critical error: {e}")
    finally:
        orchestrator.stop_all_servers()
        logger.info("👋 Goodbye!")


if __name__ == "__main__":
    main()
