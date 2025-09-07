# S1GNAL.ZERO 🛡️

<div align="center">

**AI-Powered Authenticity Verification System**

**AGI Ventures Canada Hackathon 3.0** | **Powered by Solace PubSub+** | **[Live Demo](https://s1gnal-zero.github.io)**

</div>

---

## 🚨 IMPORTANT: Development Documentation

This project includes comprehensive documentation for development:

- **[DETAILED_DESIGN.md](DETAILED_DESIGN.md)** - Complete technical specification with 21 sections
- **[signalzero-timeline-claude.md](signalzero-timeline-claude.md)** - Hour-by-hour build timeline 
- **[CLAUDE.md](CLAUDE.md)** - Critical instructions for production-ready code (NO PLACEHOLDERS)

**ALL CODE MUST BE PRODUCTION READY - NO TODO COMMENTS, NO MOCK IMPLEMENTATIONS**

---

## 🎯 Mission

**S1GNAL.ZERO** cuts through manufactured viral trends to reveal the truth behind digital hype. Our multi-agent AI system instantly detects bots, fake reviews, and coordinated manipulation campaigns, protecting consumers and businesses from FOMO-driven deception.

## 🚀 Built at AGI Ventures Canada Hackathon 3.0

* **Event**: [AGI Ventures Canada Hackathon 3.0](https://lu.ma/ai-tinkerers-ottawa) (formerly AI Tinkerers Ottawa)
* **Date**: September 6-7, 2025
* **Location**: Ottawa, Ontario
* **Sponsor**: [Solace PubSub+](https://solace.com)

## ✨ Features

### 🤖 Multi-Agent Intelligence System

Our system uses **5 specialized Python agents** communicating via Solace Agent Mesh:

1. **Bot Detection Agent**: Identifies automated accounts (62% for Stanley Cup demo)
2. **Trend Analysis Agent**: Detects abnormal growth patterns and viral velocity
3. **Review Validator Agent**: Cross-references reviews with purchase patterns
4. **Promotion Detector Agent**: Finds undisclosed sponsorships and paid promotions
5. **Score Aggregator Agent**: Combines all signals into Reality Score™

### ⚡ Event-Driven Architecture

* All analysis requests flow through Solace PubSub+ topics
* JCSMP client for guaranteed message delivery
* Real-time WebSocket updates via Vaadin Push
* Parallel agent processing < 3 seconds total

### 📊 Reality Score™ Calculation

Our proprietary weighted algorithm:

```
Reality Score = (Bot Score × 0.4) + (Trend Score × 0.3) + 
                (Review Score × 0.2) + (Promotion Score × 0.1)
```

**Score Ranges:**
* **0-33%**: Heavily Manipulated (Red Zone)
* **34-66%**: Mixed Signals (Yellow Zone)  
* **67-100%**: Authentic Engagement (Green Zone)

### 🎯 Hardcoded Demo Values

For consistent hackathon demonstrations:

| Query | Bot % | Reality Score |
|-------|-------|---------------|
| Stanley Cup | 62% | 34% |
| $BUZZ | 87% | 12% |
| Prime Energy | 71% | 29% |

## 🔍 Use Cases

1. **Consumer Protection**: Avoid FOMO-driven purchases on manufactured viral products
2. **Brand Protection**: Identify competitor sabotage and fake review attacks
3. **Marketing Due Diligence**: Verify influencer authenticity before partnerships
4. **E-commerce Intelligence**: Detect dropshipping scams and bot-driven trends
5. **Investment Analysis**: Identify pump-and-dump schemes in viral stocks

## 🛠️ Technology Stack

* **Event Broker**: Solace PubSub+ for real-time messaging
* **Backend**: Spring Boot 3.0+ (Java 17) with Solace JCSMP
* **AI Agents**: Python 3.10+ with Solace Python API
* **Frontend**: Vaadin Flow 24.2 (server-side Java UI) with @Push for real-time updates
* **Database**: PostgreSQL 14+ with UUID primary keys
* **Deployment**: Docker containers for all services

## 📈 Performance Metrics

* **< 3 seconds** End-to-end analysis time
* **5 parallel** Agent processing
* **99.99%** Message delivery guarantee (Solace)
* **Real-time** UI updates via WebSocket

## 🏗️ Architecture

### FastMCP + SAM Agent Mesh Integration

S1GNAL.ZERO uses a sophisticated **FastMCP + Solace Agent Mesh (SAM)** architecture that preserves custom Python business logic while enabling enterprise-grade orchestration:

```
┌─────────────────────────────────────────┐
│        Vaadin Web UI (@Push)            │
│  • Dashboard • Analysis • Wall of Shame │
└──────────────┬──────────────────────────┘
               │ WebSocket
┌──────────────▼──────────────────────────┐
│       Spring Boot Application           │
│  • REST Controllers • Services • JPA    │
└──────────────┬──────────────────────────┘
               │ JCSMP
         ┌─────▼─────┐
         │  Solace   │ ← Event Broker
         │ PubSub+   │
         └─────┬─────┘
               │ Topics
┌──────────────▼──────────────────────────┐
│         SAM Agent Mesh                   │
│    (Solace Agent Mesh Gateway)          │
├──────────────────────────────────────────┤
│ SAM MCP Agents (5 configured):          │
│ • bot_detection_mcp_agent.yaml          │
│ • trend_analysis_mcp_agent.yaml         │
│ • review_validator_mcp_agent.yaml       │
│ • paid_promotion_mcp_agent.yaml         │
│ • score_aggregator_mcp_agent.yaml       │
└──────────────┬──────────────────────────┘
               │ MCP Protocol (stdin/stdout)
┌──────────────▼──────────────────────────┐
│        FastMCP Servers (5 servers)      │
├──────────────────────────────────────────┤
│ • bot_detection_server.py               │
│ • trend_analysis_server.py              │
│ • review_validator_server.py            │
│ • paid_promotion_server.py              │
│ • score_aggregator_server.py            │
└──────────────┬──────────────────────────┘
               │ Direct Python imports
┌──────────────▼──────────────────────────┐
│       Original Python Agents            │
├──────────────────────────────────────────┤
│ • BotDetectionAgent                     │
│ • TrendAnalysisAgent                    │
│ • ReviewValidatorAgent                  │
│ • PaidPromotionAgent                    │
│ • ScoreAggregatorAgent                  │
└──────────────┬──────────────────────────┘
               │
         ┌─────▼─────┐
         │PostgreSQL │
         └───────────┘
```

### 🔄 Message Flow Architecture

**Data Flow**: `Java Backend → SAM Gateway → MCP Agents → FastMCP Servers → Python Agents`

1. **Analysis Request** submitted via Vaadin UI
2. **Spring Boot** publishes to Solace topic `signalzero/analysis/request/{userId}/{analysisId}`
3. **SAM Gateway** routes to configured MCP agents based on YAML configurations
4. **SAM MCP Agents** invoke FastMCP servers using MCP protocol over stdin/stdout
5. **FastMCP Servers** call original Python agent business logic
6. **Results** flow back through the same chain with guaranteed message delivery
7. **WebSocket Push** delivers real-time updates to UI (< 3 seconds total)

### 🏛️ Component Architecture

#### SAM MCP Agents (YAML Configuration Layer)
Located in `configs/agents/`, these YAML files define how SAM connects to FastMCP servers:

**Complete Agent Configuration Files:**

| Agent | Config File | MCP Port | Primary Tools |
|-------|-------------|----------|---------------|
| **Bot Detection** | `bot_detection_mcp_agent.yaml` | 8001 | `analyze_bot_patterns`, `get_bot_analysis_config`, `health_check` |
| **Trend Analysis** | `trend_analysis_mcp_agent.yaml` | 8002 | `analyze_trend_patterns`, `get_trend_analysis_config`, `health_check` |
| **Review Validator** | `review_validator_mcp_agent.yaml` | 8003 | `validate_review_authenticity`, `get_review_validator_config`, `health_check` |
| **Paid Promotion** | `paid_promotion_mcp_agent.yaml` | 8004 | `detect_paid_promotion`, `get_paid_promotion_config`, `health_check` |
| **Score Aggregator** | `score_aggregator_mcp_agent.yaml` | 8005 | `aggregate_reality_scores`, `calculate_reality_score`, `health_check` |

**Example Configuration Structure:**
```yaml
# configs/agents/bot_detection_mcp_agent.yaml
log:
  stdout_log_level: INFO
  log_file_level: INFO
  log_file: bot-detection-mcp-agent.log

!include ../shared_config.yaml

apps:
  - name: bot-detection-mcp-agent_app
    app_base_path: .
    app_module: solace_agent_mesh.agent.sac.app
    broker:
      <<: *broker_connection

    app_config:
      namespace: ${NAMESPACE} 
      supports_streaming: true
      agent_name: "BotDetectionMCPAgent"
      display_name: "Bot Detection MCP Agent"
      model: *analysis_model 

      tools: 
        - group_name: artifact_management
          tool_type: builtin-group
        - group_name: bot_detection_mcp
          tool_type: mcp
          connection_params:
            url: "http://localhost:8001"

      agent_card:
        description: "MCP-powered agent for detecting automated behavior"
        skills:
          - id: "bot_pattern_analysis"
            name: "Bot Pattern Analysis"
            description: "Analyze content for bot-like behavior patterns"
```

#### FastMCP Servers (Protocol Bridge Layer)
Located in `agents/mcp_servers/`, these Python servers bridge MCP protocol to original agents:

**FastMCP Server Startup:**
```bash
# Start all 5 MCP servers with orchestrator
cd agents/mcp_servers
python start_all_mcp_servers.py

# Or start individual servers
python bot_detection_server.py     # Port 8001
python trend_analysis_server.py    # Port 8002  
python review_validator_server.py  # Port 8003
python paid_promotion_server.py    # Port 8004
python score_aggregator_server.py  # Port 8005
```

**FastMCP Server Architecture:**
```python
# Example: agents/mcp_servers/bot_detection_server.py
from fastmcp import FastMCP
from bot_detection_agent import BotDetectionAgent

class BotDetectionMCPServer:
    def __init__(self):
        self.agent = BotDetectionAgent()
        self.mcp = FastMCP("BotDetectionServer")
        self._setup_tools()
    
    def _setup_tools(self):
        @self.mcp.tool()
        def analyze_bot_patterns(
            analysis_id: str,
            query: str,
            platform: str = "all",
            user_id: str = None
        ) -> dict:
            """Main bot detection analysis using MCP protocol"""
            request_data = {
                'analysisId': analysis_id,
                'query': query,
                'platform': platform,
                'userId': user_id
            }
            result = self.agent.process_analysis_request(request_data)
            return {
                'success': True,
                'analysis_id': analysis_id,
                'agent_type': 'bot-detection',
                'result': result
            }
    
    def run_server(self):
        self.agent.start()
        self.mcp.run()  # stdin/stdout MCP protocol
```

**MCP Server Health Monitoring:**
The orchestrator (`start_all_mcp_servers.py`) provides:
- ✅ Automatic server startup and health monitoring
- ✅ Process isolation and restart capability
- ✅ Centralized logging and status reporting
- ✅ Graceful shutdown handling

### 🖥️ Complete MCP Agent Reference

#### **All MCP Agents Overview**

| Agent Name | Script File | Port | Status Endpoint | Log File | Primary Function |
|------------|-------------|------|----------------|----------|------------------|
| **BotDetectionServer** | `bot_detection_server.py` | 8001 | `http://localhost:8001/health` | `bot-detection-mcp-agent.log` | Detects automated accounts and bot behavior |
| **TrendAnalysisServer** | `trend_analysis_server.py` | 8002 | `http://localhost:8002/health` | `trend-analysis-mcp-agent.log` | Analyzes viral patterns and growth velocity |
| **ReviewValidatorServer** | `review_validator_server.py` | 8003 | `http://localhost:8003/health` | `review-validator-mcp-agent.log` | Validates review authenticity |
| **PaidPromotionServer** | `paid_promotion_server.py` | 8004 | `http://localhost:8004/health` | `paid-promotion-mcp-agent.log` | Detects sponsored content and paid promotions |
| **ScoreAggregatorServer** | `score_aggregator_server.py` | 8005 | `http://localhost:8005/health` | `score-aggregator-mcp-agent.log` | Aggregates results into Reality Score™ |

#### **Starting MCP Agents**

**Option 1: Start All Agents with Orchestrator (Recommended)**
```bash
# Navigate to MCP servers directory
cd agents/mcp_servers

# Activate Python virtual environment
source ../.venv/bin/activate  # Linux/Mac
# OR: ..\\.venv\\Scripts\\activate  # Windows

# Start all 5 agents with orchestrator
python start_all_mcp_servers.py

# Expected output:
# 🚀 Starting S1GNAL.ZERO FastMCP Server Orchestration
# ======================================================================
# 🔄 Starting BotDetectionServer on port 8001
# ✅ Started BotDetectionServer (PID: 12345)
# 🔄 Starting TrendAnalysisServer on port 8002
# ✅ Started TrendAnalysisServer (PID: 12346)
# 🔄 Starting ReviewValidatorServer on port 8003
# ✅ Started ReviewValidatorServer (PID: 12347)
# 🔄 Starting PaidPromotionServer on port 8004
# ✅ Started PaidPromotionServer (PID: 12348)
# 🔄 Starting ScoreAggregatorServer on port 8005
# ✅ Started ScoreAggregatorServer (PID: 12349)
# 🎉 ALL FASTMCP SERVERS STARTED SUCCESSFULLY!
```

**Option 2: Start Individual Agents (For Development/Debugging)**
```bash
# Navigate to MCP servers directory
cd agents/mcp_servers
source ../.venv/bin/activate

# Start individual agents in separate terminals:

# Terminal 1 - Bot Detection Agent
python bot_detection_server.py
# Expected: 🚀 Starting Bot Detection MCP Server
#           📡 Running in stdin/stdout mode
#           ✅ Bot Detection Agent started successfully

# Terminal 2 - Trend Analysis Agent  
python trend_analysis_server.py
# Expected: 🚀 Starting Trend Analysis MCP Server
#           📡 Running in stdin/stdout mode
#           ✅ Trend Analysis Agent started successfully

# Terminal 3 - Review Validator Agent
python review_validator_server.py
# Expected: 🚀 Starting Review Validator MCP Server
#           📡 Running in stdin/stdout mode
#           ✅ Review Validator Agent started successfully

# Terminal 4 - Paid Promotion Agent
python paid_promotion_server.py
# Expected: 🚀 Starting Paid Promotion MCP Server
#           📡 Running in stdin/stdout mode
#           ✅ Paid Promotion Agent started successfully

# Terminal 5 - Score Aggregator Agent
python score_aggregator_server.py
# Expected: 🚀 Starting Score Aggregator MCP Server
#           📡 Running in stdin/stdout mode
#           ✅ Score Aggregator Agent started successfully
```

#### **MCP Agent Health Checks**

**Check All Agent Status:**
```bash
# Check if all agents are running
ps aux | grep "_server.py"

# Check specific ports are listening
netstat -an | grep -E ':(8001|8002|8003|8004|8005)'

# Test individual agent health (if health endpoints are implemented)
curl http://localhost:8001/health  # Bot Detection
curl http://localhost:8002/health  # Trend Analysis
curl http://localhost:8003/health  # Review Validator
curl http://localhost:8004/health  # Paid Promotion
curl http://localhost:8005/health  # Score Aggregator
```

**View Agent Logs:**
```bash
# View orchestrator logs
tail -f agents/mcp_servers/mcp_servers.log

# View individual agent logs (if SAM is configured)
tail -f bot-detection-mcp-agent.log
tail -f trend-analysis-mcp-agent.log
tail -f review-validator-mcp-agent.log
tail -f paid-promotion-mcp-agent.log
tail -f score-aggregator-mcp-agent.log
```

#### **Stopping MCP Agents**

**Stop All Agents (Orchestrator):**
```bash
# Send SIGTERM to orchestrator (Ctrl+C in terminal)
# OR find and kill the orchestrator process
pkill -f "start_all_mcp_servers.py"
```

**Stop Individual Agents:**
```bash
# Kill specific agents by name
pkill -f "bot_detection_server.py"
pkill -f "trend_analysis_server.py"
pkill -f "review_validator_server.py"
pkill -f "paid_promotion_server.py"
pkill -f "score_aggregator_server.py"

# OR kill by port (find PID first)
lsof -ti:8001 | xargs kill  # Bot Detection
lsof -ti:8002 | xargs kill  # Trend Analysis
lsof -ti:8003 | xargs kill  # Review Validator
lsof -ti:8004 | xargs kill  # Paid Promotion
lsof -ti:8005 | xargs kill  # Score Aggregator
```

#### **Troubleshooting MCP Agents**

**Common Issues & Solutions:**

1. **Agent Won't Start:**
   ```bash
   # Check if port is already in use
   lsof -i :8001
   
   # Check Python environment
   which python
   pip list | grep fastmcp
   
   # Check agent dependencies
   cd agents && pip install -r requirements.txt
   ```

2. **Agent Starts But No Response:**
   ```bash
   # Verify agent is importing correctly
   python -c "from bot_detection_agent import BotDetectionAgent; print('Import OK')"
   
   # Check for Python errors in logs
   python bot_detection_server.py 2>&1 | tee debug.log
   ```

3. **SAM Can't Connect to MCP Server:**
   ```bash
   # Verify MCP server is listening on correct port
   netstat -tulpn | grep 8001
   
   # Test MCP protocol communication
   # (Note: MCP uses stdin/stdout, not HTTP)
   echo '{"jsonrpc": "2.0", "id": 1, "method": "tools/list", "params": {}}' | python bot_detection_server.py
   ```

4. **Performance Issues:**
   ```bash
   # Monitor agent resource usage
   top -p $(pgrep -f "_server.py")
   
   # Check agent processing times in logs
   tail -f mcp_servers.log | grep "execution_time"
   ```

**Development Mode:**
```bash
# Run agents with debug logging
export DEBUG_MODE=true
export LOG_LEVEL=DEBUG
python start_all_mcp_servers.py

# Run single agent with verbose output
export PYTHONPATH="${PYTHONPATH}:$(pwd)/.."
python -v bot_detection_server.py
```

#### Original Python Agents (Business Logic Layer)
Located in `agents/`, these contain all the custom analysis algorithms and remain unchanged:

- **BotDetectionAgent**: Detects automated accounts and bot activity patterns
- **TrendAnalysisAgent**: Analyzes viral growth patterns and organic vs manufactured trends
- **ReviewValidatorAgent**: Cross-references reviews with purchase patterns and sentiment
- **PaidPromotionAgent**: Identifies undisclosed sponsorships and promotional content
- **ScoreAggregatorAgent**: Combines all agent signals into Reality Score™

### 🚀 Why FastMCP + SAM Architecture?

**Benefits of this hybrid approach:**

1. **✅ Preserves Custom Logic**: All original Python agent business logic remains intact
2. **✅ Enterprise Orchestration**: SAM provides enterprise-grade routing, monitoring, and scaling
3. **✅ Protocol Compliance**: MCP protocol enables standardized tool integration
4. **✅ Process Isolation**: Each FastMCP server runs in its own process with health monitoring
5. **✅ Easy Debugging**: Clear separation between orchestration (SAM) and logic (Python)
6. **✅ Scalability**: SAM can scale agents independently based on load

**vs. Direct Solace Integration:**
- More complex setup, but provides enterprise orchestration and monitoring
- Better for production deployments requiring high availability and scaling
- Cleaner separation of concerns between message routing and business logic

## 🚦 Getting Started

### Prerequisites

* Docker Desktop running
* Java 17+ 
* Python 3.10.16+
* **pip** (usually included with Python) or **uv** (install [uv](https://docs.astral.sh/uv/getting-started/installation/))
* PostgreSQL 14+
* Maven 3.8+
* **Operating System**: macOS, Linux, or Windows (via [WSL](https://learn.microsoft.com/en-us/windows/wsl/))
* **LLM API key** from any major provider or your own custom endpoint

### SAM (Solace Agent Mesh) Installation

#### Installing Solace Agent Mesh

**Important**: We recommend installing SAM in a virtual environment to avoid conflicts with other Python packages.

**Creating a Virtual Environment**

**Using PIP:**
```bash
# 1. Create a virtual environment
python3 -m venv .venv

# 2. Activate the environment
# On Linux or Unix platforms:
source .venv/bin/activate

# On Windows:
# .venv\Scripts\activate
```

**Using UV:**
```bash
# 1. Create a virtual environment
uv venv .venv

# 2. Activate the environment
# On Linux or Unix platforms:
source .venv/bin/activate

# On Windows:
# .venv\Scripts\activate

# 3. Expose the following environment variables:
# On Linux or Unix platforms:
export SAM_PLUGIN_INSTALL_COMMAND="uv pip install {package}"

# On Windows:
# set SAM_PLUGIN_INSTALL_COMMAND="uv pip install {package}"
```

**Install Solace Agent Mesh:**

**Using PIP:**
```bash
pip install solace-agent-mesh
```

**Using UV:**
```bash
uv pip install solace-agent-mesh
```

**Docker Alternative:**
You can also use our pre-built Docker image to run SAM CLI commands without a local Python installation:

```bash
# Verify installation using Docker
docker run --rm solace/solace-agent-mesh:latest --version

# Note: If your OS architecture is not linux/amd64, add --platform linux/amd64
docker run --rm --platform linux/amd64 solace/solace-agent-mesh:latest --version
```

**Browser Requirement:**
The `Mermaid` agent requires a browser with headless mode support. Install browser dependencies:

```bash
playwright install
```

**Verify Installation:**
```bash
# Test SAM CLI installation
solace-agent-mesh --version

# Or use the shorter alias:
sam --version

# Get help:
solace-agent-mesh --help
```

### Environment Configuration

**Important**: Set up your environment variables before running the application.

#### 1. Environment Files Overview

| File | Purpose | Committed to Git |
|------|---------|------------------|
| `.env` | Your actual working environment (local) | ❌ No (gitignored) |
| `.env.dev` | Development template | ✅ Yes (template) |
| `.env.prod` | Production template | ✅ Yes (template) |
| `agents/.env` | Python agents configuration | ❌ No (gitignored) |

#### 2. Quick Setup for Development

```bash
# Copy development template to working file
cp .env.dev .env

# Copy agents template 
cp agents/.env.example agents/.env  # (if available)
# OR create agents/.env manually with Solace connection details
```

#### 3. Required Environment Variables

**Critical (Must Set):**
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/signalzero
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

# Solace PubSub+ (Spring Boot Backend)
SOLACE_HOST=tcp://localhost:55555
SOLACE_VPN=default
SOLACE_USERNAME=admin
SOLACE_PASSWORD=admin
SOLACE_CLIENT_NAME=signalzero-backend-dev

# SAM Agent Mesh Event Broker Configuration
SOLACE_BROKER_URL=ws://localhost:8008
SOLACE_BROKER_VPN=default
SOLACE_BROKER_USERNAME=default
SOLACE_BROKER_PASSWORD=default
SOLACE_DEV_MODE=false

# Data Plane Event Mesh (Separate from Control Plane)
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_URL=ws://localhost:8008
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_VPN=default
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_USERNAME=default
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_PASSWORD=default

# LLM Service Integration (Required for SAM)
LLM_SERVICE_ENDPOINT=https://api.anthropic.com
LLM_SERVICE_API_KEY=your_anthropic_api_key_here
LLM_SERVICE_PLANNING_MODEL_NAME=anthropic/claude-3-haiku-20240307
LLM_SERVICE_GENERAL_MODEL_NAME=anthropic/claude-3-haiku-20240307
LLM_SERVICE_ANALYSIS_MODEL_NAME=anthropic/claude-3-haiku-20240307

# SAM Namespace Configuration
NAMESPACE=default_namespace/

# Stripe (for payments)
STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable_key_here
STRIPE_SECRET_KEY=sk_test_your_secret_key_here
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
```

**Optional APIs (Add as available):**
```bash
# Reddit API (Free - https://www.reddit.com/prefs/apps)
REDDIT_CLIENT_ID=your_reddit_client_id
REDDIT_CLIENT_SECRET=your_reddit_client_secret

# YouTube API (Free quota - Google Cloud Console)
YOUTUBE_API_KEY=AIzaSy_your_youtube_api_key_here

# News API (Free tier - https://newsapi.org)
NEWSAPI_KEY=your_newsapi_key_here

# Twitter API ($100/month - https://developer.twitter.com)
TWITTER_BEARER_TOKEN=AAAAAAAAAA_your_twitter_bearer_token_here
```

#### 4. Hackathon Quick Start (Demo Mode)

For fastest setup during hackathon:

```bash
# Use development environment with demo mode enabled
cp .env.dev .env

# Edit .env and set:
DEMO_MODE=true
USE_MOCK_DATA=true
HACKATHON_MODE=true
AUTO_SEED_DATA=true

# These settings enable:
# - Hardcoded demo responses (Stanley Cup = 62% bots, 34% Reality Score)
# - Mock data fallbacks when APIs unavailable
# - Auto-populated Wall of Shame
# - Bypass payment verification for testing
```

### Quick Installation

Follow the hour-by-hour timeline in [signalzero-timeline-claude.md](signalzero-timeline-claude.md) for detailed build instructions.

```bash
# 1. Clone the repository
git clone https://github.com/S1GNAL-ZERO/s1gnal.zero.app.git
cd s1gnal.zero.app

# 2. Set up environment (IMPORTANT - do this first!)
cp .env.dev .env
# Edit .env with your database password and API keys

# 3. Start Solace PubSub+ Docker
docker run -d -p 55555:55555 -p 8080:8080 -p 1883:1883 -p 8008:8008 \
  --shm-size=2g --env username_admin_globalaccesslevel=admin \
  --env username_admin_password=admin --name=solace \
  solace/solace-pubsub-standard

# Port explanations:
# 55555 - Solace Message Format (SMF) port for Java/Spring Boot JCSMP client
# 8080  - PubSub+ Manager web console (http://localhost:8080)
# 1883  - MQTT port for Python agents 
# 8008  - Web messaging port for browser clients

# 4. Set up PostgreSQL database
createdb signalzero
psql signalzero < database/schema.sql
psql signalzero < database/demo_data.sql

# 5. Start Spring Boot backend (port 8081)
cd backend
mvn clean install
mvn spring-boot:run

# 6. Start FastMCP Servers (in new terminal)
cd agents
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
pip install fastmcp  # Install FastMCP protocol library
python mcp_servers/start_all_mcp_servers.py

# Verify all 5 servers started:
# ✅ BotDetectionServer (Port 8001)
# ✅ TrendAnalysisServer (Port 8002) 
# ✅ ReviewValidatorServer (Port 8003)
# ✅ PaidPromotionServer (Port 8004)
# ✅ ScoreAggregatorServer (Port 8005)

# 7. Start SAM Agent Mesh (in another terminal)
# Load all 5 agent configurations from configs/agents/:
# • bot_detection_mcp_agent.yaml
# • trend_analysis_mcp_agent.yaml
# • review_validator_mcp_agent.yaml  
# • paid_promotion_mcp_agent.yaml
# • score_aggregator_mcp_agent.yaml

# Example SAM startup (adjust per your SAM installation):
sam-gateway --config-dir configs/agents/

# 8. Access the application
open http://localhost:8081
```

### 🔐 Security Notes

- **Never commit actual `.env` files** - they contain sensitive API keys and passwords
- The `.env.dev` and `.env.prod` files are templates with placeholder values
- Actual environment files are properly gitignored for security
- For production deployment, use secure secret management (AWS Secrets Manager, etc.)

## 🛠️ Building Custom SAM Agents

### Creating Your Own Weather Agent Tutorial

This comprehensive example shows how to build a sophisticated weather agent using the Solace Agent Mesh framework, demonstrating external API integration, professional service layer architecture, and production-ready patterns.

#### Planning Your Custom Agent

When building custom agents for S1GNAL.ZERO, consider these capabilities:

1. **External API Integration**: Fetch data from third-party services (like OpenWeatherMap)
2. **Professional Service Layer**: Clean architecture with proper separation of concerns  
3. **Multiple Sophisticated Tools**: Provide various functions through MCP protocol
4. **Proper Lifecycle Management**: Initialize and cleanup resources correctly
5. **Error Handling and Validation**: Robust error handling for production use
6. **Artifact Creation**: Save results as files for future reference

#### Step 1: Create Agent Structure

Create a new custom agent using SAM CLI:

```bash
# Create agent using GUI wizard
sam add agent --gui

# OR create as plugin (alternative approach)
sam plugin create my-custom-agent --type agent
```

**Project Structure for Custom Agent:**
```
sam-project/
├── src/
│   └── custom_agent/
│       ├── __init__.py
│       ├── tools.py           # MCP tool functions
│       ├── lifecycle.py       # Init/cleanup functions
│       └── services/
│           ├── __init__.py
│           └── custom_service.py
├── configs/
│   └── agents/
│       └── custom_agent.yaml  # Agent configuration
└── requirements.txt
```

#### Step 2: Service Implementation

**Example Service Class (`src/custom_agent/services/custom_service.py`):**

```python
"""Custom service for handling external API calls."""
import aiohttp
from typing import Dict, Any, Optional
from datetime import datetime
from solace_ai_connector.common.log import log

class CustomService:
    """Service for fetching data from external APIs."""
    
    def __init__(self, api_key: str, base_url: str):
        self.api_key = api_key
        self.base_url = base_url
        self.session: Optional[aiohttp.ClientSession] = None
        self.log_identifier = "[CustomService]"
    
    async def _get_session(self) -> aiohttp.ClientSession:
        """Get or create HTTP session."""
        if self.session is None or self.session.closed:
            self.session = aiohttp.ClientSession()
        return self.session
    
    async def close(self):
        """Close HTTP session."""
        if self.session and not self.session.closed:
            await self.session.close()
            log.info(f"{self.log_identifier} HTTP session closed")
    
    async def fetch_data(self, query: str, options: Dict[str, Any] = None) -> Dict[str, Any]:
        """Fetch data from external API."""
        log.info(f"{self.log_identifier} Fetching data for: {query}")
        
        session = await self._get_session()
        params = {"q": query, "key": self.api_key}
        if options:
            params.update(options)
        
        try:
            async with session.get(f"{self.base_url}/data", params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return self._format_response(data)
                else:
                    error_data = await response.json()
                    raise Exception(f"API error: {error_data.get('message', 'Unknown error')}")
        except aiohttp.ClientError as e:
            log.error(f"{self.log_identifier} Network error: {e}")
            raise Exception(f"Network error: {str(e)}")
    
    def _format_response(self, data: Dict) -> Dict[str, Any]:
        """Format API response for consistent output."""
        return {
            "timestamp": datetime.now().isoformat(),
            "data": data,
            "processed": True
        }
```

#### Step 3: Tool Implementation

**MCP Tools (`src/custom_agent/tools.py`):**

```python
"""Custom agent tools using MCP protocol."""
from typing import Any, Dict, Optional
from datetime import datetime, timezone
from google.adk.tools import ToolContext
from solace_ai_connector.common.log import log
from solace_agent_mesh.agent.utils.artifact_helpers import save_artifact_with_metadata

async def process_custom_request(
    query: str,
    options: Dict[str, Any] = None,
    save_to_file: bool = False,
    tool_context: Optional[ToolContext] = None,
    tool_config: Optional[Dict[str, Any]] = None
) -> Dict[str, Any]:
    """
    Process custom data request.
    
    Args:
        query: Search query or data identifier
        options: Additional processing options
        save_to_file: Whether to save results as artifact
    
    Returns:
        Dictionary containing processed results
    """
    log_identifier = "[ProcessCustomRequest]"
    log.info(f"{log_identifier} Processing request: {query}")
    
    if not tool_context:
        return {"status": "error", "message": "Tool context required"}
    
    try:
        # Get service from agent state
        host_component = getattr(tool_context._invocation_context, "agent", None)
        if host_component:
            host_component = getattr(host_component, "host_component", None)
        
        if not host_component:
            return {"status": "error", "message": "Could not access agent"}
        
        custom_service = host_component.get_agent_specific_state("custom_service")
        if not custom_service:
            return {"status": "error", "message": "Custom service not initialized"}
        
        # Process request
        result_data = await custom_service.fetch_data(query, options or {})
        
        # Create summary
        summary = f"Processed query '{query}' successfully at {result_data['timestamp']}"
        
        result = {
            "status": "success",
            "query": query,
            "summary": summary,
            "data": result_data
        }
        
        # Save artifact if requested
        if save_to_file:
            artifact_result = await _save_custom_artifact(
                result_data, f"custom_data_{query}", tool_context
            )
            result["artifact"] = artifact_result
        
        log.info(f"{log_identifier} Successfully processed: {query}")
        return result
        
    except Exception as e:
        log.error(f"{log_identifier} Error processing request: {e}")
        return {"status": "error", "message": f"Processing error: {str(e)}"}

async def _save_custom_artifact(
    data: Dict[str, Any],
    filename_base: str,
    tool_context: ToolContext
) -> Dict[str, Any]:
    """Save custom data as artifact."""
    try:
        import json
        content = json.dumps(data, indent=2, default=str)
        timestamp = datetime.now(timezone.utc)
        filename = f"{filename_base}_{timestamp.strftime('%Y%m%d_%H%M%S')}.json"
        
        artifact_service = tool_context._invocation_context.artifact_service
        result = await save_artifact_with_metadata(
            artifact_service=artifact_service,
            app_name=tool_context._invocation_context.app_name,
            user_id=tool_context._invocation_context.user_id,
            session_id=tool_context._invocation_context.session.id,
            filename=filename,
            content_bytes=content.encode('utf-8'),
            mime_type="application/json",
            metadata_dict={
                "description": "Custom agent data report",
                "source": "Custom Agent"
            },
            timestamp=timestamp
        )
        
        return {"filename": filename, "status": result.get("status", "success")}
    except Exception as e:
        log.error(f"[CustomArtifact] Error saving: {e}")
        return {"status": "error", "message": f"Failed to save: {str(e)}"}
```

#### Step 4: Agent Lifecycle

**Lifecycle Management (`src/custom_agent/lifecycle.py`):**

```python
"""Lifecycle functions for Custom Agent."""
from typing import Any
import asyncio
from pydantic import BaseModel, Field, SecretStr
from solace_ai_connector.common.log import log
from .services.custom_service import CustomService

class CustomAgentInitConfig(BaseModel):
    """Configuration model for Custom Agent initialization."""
    api_key: SecretStr = Field(description="External API key")
    base_url: str = Field(default="https://api.example.com", description="API base URL")
    startup_message: str = Field(
        default="Custom Agent is ready!",
        description="Startup message"
    )

def initialize_custom_agent(host_component: Any, init_config: CustomAgentInitConfig):
    """Initialize the Custom Agent."""
    log_identifier = f"[{host_component.agent_name}:init]"
    log.info(f"{log_identifier} Starting initialization...")
    
    try:
        # Initialize custom service
        custom_service = CustomService(
            api_key=init_config.api_key.get_secret_value(),
            base_url=init_config.base_url
        )
        
        # Store service in agent state
        host_component.set_agent_specific_state("custom_service", custom_service)
        host_component.set_agent_specific_state("initialized_at", "2024-01-01T00:00:00Z")
        
        log.info(f"{log_identifier} {init_config.startup_message}")
        log.info(f"{log_identifier} Initialization completed successfully")
        
    except Exception as e:
        log.error(f"{log_identifier} Failed to initialize: {e}")
        raise

def cleanup_custom_agent(host_component: Any):
    """Clean up Custom Agent resources."""
    log_identifier = f"[{host_component.agent_name}:cleanup]"
    log.info(f"{log_identifier} Starting cleanup...")
    
    async def cleanup_async(host_component: Any):
        try:
            custom_service = host_component.get_agent_specific_state("custom_service")
            if custom_service:
                await custom_service.close()
                log.info(f"{log_identifier} Service closed successfully")
            
            log.info(f"{log_identifier} Cleanup completed")
        except Exception as e:
            log.error(f"{log_identifier} Error during cleanup: {e}")
    
    loop = asyncio.get_event_loop()
    loop.run_until_complete(cleanup_async(host_component))
```

#### Step 5: Agent Configuration

**YAML Configuration (`configs/agents/custom_agent.yaml`):**

```yaml
# Custom Agent Configuration
log:
  stdout_log_level: INFO
  log_file_level: DEBUG
  log_file: custom-agent.log

!include ../shared_config.yaml

apps:
  - name: custom-agent
    app_module: solace_agent_mesh.agent.sac.app
    broker:
      <<: *broker_connection
    
    app_config:
      namespace: "${NAMESPACE}"
      agent_name: "CustomAgent"
      display_name: "Custom Data Processing Agent"
      supports_streaming: true
      
      # LLM model configuration
      model: *general_model
      
      # Agent instructions
      instruction: |
        You are a professional custom agent that processes data requests efficiently.
        
        Your capabilities include:
        1. Processing custom data requests from external APIs
        2. Formatting and validating response data
        3. Saving detailed reports as files when requested
        
        Guidelines:
        - Always validate input parameters before processing
        - Provide clear, structured responses
        - Handle errors gracefully with helpful messages
        - Offer to save detailed reports when appropriate
      
      # Lifecycle functions
      agent_init_function:
        module: "src.custom_agent.lifecycle"
        name: "initialize_custom_agent"
        base_path: .
        config:
          api_key: ${CUSTOM_API_KEY}
          base_url: "https://api.example.com"
          startup_message: "Custom Agent is ready to process requests!"
      
      agent_cleanup_function:
        module: "src.custom_agent.lifecycle"
        base_path: .
        name: "cleanup_custom_agent"
      
      # Tools configuration
      tools:
        - tool_type: python
          component_module: "src.custom_agent.tools"
          component_base_path: .
          function_name: "process_custom_request"
          tool_description: "Process custom data requests from external sources"
        
        # Built-in artifact tools
        - tool_type: builtin-group
          group_name: "artifact_management"
          session_service: *default_session_service
      
      artifact_service: *default_artifact_service
      artifact_handling_mode: "reference"
      enable_embed_resolution: true
      
      # Agent card for discovery
      agent_card:
        description: "Professional custom agent for data processing and external API integration"
        defaultInputModes: ["text"]
        defaultOutputModes: ["text", "file"]
        skills:
          - id: "custom_processing"
            name: "Custom Data Processing"
            description: "Process requests using external API integration"
      
      agent_card_publishing:
        interval_seconds: 30
      agent_discovery:
        enabled: false
      inter_agent_communication:
        allow_list: []
        request_timeout_seconds: 30
```

#### Step 6: Running Your Custom Agent

**Environment Setup:**
```bash
# Set required environment variables
export CUSTOM_API_KEY="your_api_key_here"
export NAMESPACE="custom_namespace/"

# Other SAM environment variables as needed
```

**Start the Agent:**
```bash
# For development testing
sam run configs/agents/custom_agent.yaml

# Or build as plugin for production
sam plugin build
sam plugin install your-custom-agent-package
```

#### Key Features Demonstrated

1. **External API Integration**: HTTP session management and API calls
2. **Resource Management**: Proper lifecycle with initialization and cleanup  
3. **Configuration Management**: Type-safe config with Pydantic models
4. **Error Handling**: Comprehensive exception handling and logging
5. **Artifact Management**: Save structured data as downloadable files
6. **MCP Protocol Integration**: Tools accessible through standardized protocol

### 🏗️ SAM Event Broker Architecture

S1GNAL.ZERO uses a **dual-broker architecture** separating control plane and data plane messaging:

#### **Event Mesh Gateway Integration**

S1GNAL.ZERO leverages SAM's **Event Mesh Gateway** to seamlessly integrate with existing event mesh infrastructure. This allows the system to:

- **Seamless Communication**: Subscribe to and publish events across the entire event mesh
- **Event-Driven Automation**: Intelligent event processing based on patterns and AI-driven insights  
- **Scalability**: Dynamically participate in large-scale event-driven systems

The Event Mesh Gateway connects SAM to our Solace PubSub+ event mesh, enabling asynchronous interfaces where applications can seamlessly access and utilize Solace Agent Mesh capabilities.

#### **S1GNAL.ZERO Event Mesh Gateway Configuration**

Our system uses a custom Event Mesh Gateway that automatically processes analysis requests and routes them to appropriate agents. Here's how it integrates with our existing Jira-style workflow:

**Gateway Configuration (`configs/gateways/signalzero-event-mesh.yaml`):**

```yaml
log:
  stdout_log_level: INFO
  log_file_level: DEBUG
  log_file: signalzero-event-mesh.log

!include ../shared_config.yaml

apps:
  - name: signalzero-event-mesh-app
    app_module: sam_event_mesh_gateway.app
    broker:
      <<: *broker_connection
    
    app_config:
      namespace: ${NAMESPACE}
      gateway_id: "signalzero-event-mesh-gw-01"
      artifact_service: *default_artifact_service
      authorization_service:
        type: "none" # Or "default_rbac" for production
      default_user_identity: "anonymous_analysis_user"
      
      # Data plane connection (separate from control plane)
      event_mesh_broker_config:
        broker_url: ${SIGNALZERO_EVENT_MESH_SOLACE_BROKER_URL}
        broker_vpn: ${SIGNALZERO_EVENT_MESH_SOLACE_BROKER_VPN}
        broker_username: ${SIGNALZERO_EVENT_MESH_SOLACE_BROKER_USERNAME}
        broker_password: ${SIGNALZERO_EVENT_MESH_SOLACE_BROKER_PASSWORD}
      
      # Event handlers for analysis requests
      event_handlers:
        - name: "analysis_request_handler"
          subscriptions:
            - topic: "signalzero/analysis/request/>"
              qos: 1
          input_expression: "template:Analyze the following query for authenticity: Query: {{text://input.payload:query}}, Platform: {{text://input.payload:platform}}, User: {{text://input.payload:userId}}. Provide Reality Score and detailed breakdown."
          payload_encoding: "utf-8"
          payload_format: "json"
          target_agent_name: "OrchestratorAgent"
          on_success: "analysis_response_handler"
          on_error: "analysis_error_handler"
          forward_context:
            analysis_id: "input.payload:analysisId"
            user_id: "input.payload:userId"
            correlation_id: "input.user_properties:correlation_id"
        
        - name: "bot_detection_handler"
          subscriptions:
            - topic: "signalzero/agent/bot-detector/request"
              qos: 1
          input_expression: "template:Detect bot activity for: {{text://input.payload:query}}. Return bot percentage and confidence score."
          payload_encoding: "utf-8"
          payload_format: "json"
          target_agent_name: "BotDetectionMCPAgent"
          on_success: "bot_detection_response_handler"
          on_error: "analysis_error_handler"
      
      # Output handlers for responses
      output_handlers:
        - name: "analysis_response_handler"
          topic_expression: "template:signalzero/analysis/response/{{text://user_data.forward_context:user_id}}/{{text://user_data.forward_context:analysis_id}}"
          payload_expression: "task_response:text"
          payload_encoding: "utf-8"
          payload_format: "json"
        
        - name: "bot_detection_response_handler"
          topic_expression: "static:signalzero/agent/bot-detector/response"
          payload_expression: "task_response:text"
          payload_encoding: "utf-8"
          payload_format: "json"
        
        - name: "analysis_error_handler"
          topic_expression: "template:signalzero/analysis/error/{{text://user_data.forward_context:analysis_id}}"
          payload_expression: "task_response:a2a_task_response.error"
          payload_encoding: "utf-8"
          payload_format: "json"
```

**Environment Variables for Gateway:**
```bash
# Data plane event mesh connection (separate from control plane)
export SIGNALZERO_EVENT_MESH_SOLACE_BROKER_URL="ws://localhost:8008"
export SIGNALZERO_EVENT_MESH_SOLACE_BROKER_VPN="default"
export SIGNALZERO_EVENT_MESH_SOLACE_BROKER_USERNAME="default"
export SIGNALZERO_EVENT_MESH_SOLACE_BROKER_PASSWORD="default"
```

**Running the Event Mesh Gateway:**
```bash
# Start the S1GNAL.ZERO event mesh gateway
sam run configs/gateways/signalzero-event-mesh.yaml
```

The gateway performs these functions:

1. **Connects to dual broker architecture** (control plane + data plane)
2. **Subscribes to analysis topics** on the data plane event mesh
3. **Routes requests to appropriate SAM agents** (bot detection, trend analysis, etc.)
4. **Publishes responses** back to the event mesh for consumption by Spring Boot backend
5. **Handles errors gracefully** with separate error topic routing

#### **Advanced Gateway Features**

**Dynamic Agent Routing:**
```yaml
# Route to different agents based on analysis type
target_agent_name_expression: "template:{{text://input.payload:analysisType}}MCPAgent"
```

**Context Forwarding:**
```yaml
forward_context:
  analysis_id: "input.payload:analysisId"
  user_id: "input.payload:userId"
  platform: "input.payload:platform"
  timestamp: "input.timestamp"
```

**Artifact Processing:**
```yaml
# Automatically create artifacts from analysis results
create_artifact_from_payload: true
artifact_metadata:
  description: "Analysis result data"
  source: "S1GNAL.ZERO Analysis"
```

#### **Testing the Gateway**

**Using Solace PubSub+ Broker Manager:**

1. Open **Try Me!** tab in PubSub+ Manager (http://localhost:8080)
2. **Subscriber Panel**: Subscribe to `signalzero/analysis/response/>`
3. **Publisher Panel**: Publish to `signalzero/analysis/request/user123/analysis456`

**Example Test Message:**
```json
{
  "analysisId": "analysis456",
  "userId": "user123",
  "query": "Stanley Cup",
  "platform": "all",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**Expected Response:**
```json
{
  "analysisId": "analysis456",
  "realityScore": 34,
  "botPercentage": 62,
  "status": "completed",
  "timestamp": "2024-01-01T00:00:05Z"
}
```

#### **Control Plane (Spring Boot ↔ Solace)**
```bash
# Java JCSMP Connection (Port 55555 - SMF Protocol)
SOLACE_HOST=tcp://localhost:55555
SOLACE_USERNAME=admin
SOLACE_PASSWORD=admin
```
- **Purpose**: Analysis request/response messaging between Java backend and agents
- **Protocol**: Solace Message Format (SMF) for high-performance guaranteed delivery
- **Topics**: `signalzero/analysis/request/*`, `signalzero/agent/*/response`

#### **Data Plane (SAM Agent Mesh ↔ Solace)**
```bash
# WebSocket Connection (Port 8008 - Web Messaging)
SOLACE_BROKER_URL=ws://localhost:8008
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_URL=ws://localhost:8008
```
- **Purpose**: Inter-agent communication and SAM orchestration
- **Protocol**: WebSocket for browser/agent compatibility
- **Topics**: SAM internal routing and MCP tool invocations

#### **Complete SAM Configuration Template**
```bash
# =============================================================================
# SAM AGENT MESH CONFIGURATION
# =============================================================================

# Control Plane - Spring Boot to Solace
SOLACE_HOST=tcp://localhost:55555
SOLACE_VPN=default
SOLACE_USERNAME=admin
SOLACE_PASSWORD=admin
SOLACE_CLIENT_NAME=signalzero-backend-dev

# Data Plane - SAM Gateway to Solace
SOLACE_BROKER_URL=ws://localhost:8008
SOLACE_BROKER_VPN=default
SOLACE_BROKER_USERNAME=default
SOLACE_BROKER_PASSWORD=default
SOLACE_DEV_MODE=false

# Event Mesh Data Plane (Redundant Configuration)
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_URL=ws://localhost:8008
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_VPN=default
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_USERNAME=default
SIGNALZERO_EVENT_MESH_SOLACE_BROKER_PASSWORD=default

# LLM Service Integration
LLM_SERVICE_ENDPOINT=https://api.anthropic.com
LLM_SERVICE_API_KEY=your_anthropic_api_key_here
LLM_SERVICE_PLANNING_MODEL_NAME=anthropic/claude-3-haiku-20240307
LLM_SERVICE_GENERAL_MODEL_NAME=anthropic/claude-3-haiku-20240307
LLM_SERVICE_ANALYSIS_MODEL_NAME=anthropic/claude-3-haiku-20240307

# SAM Namespace & Session Management
NAMESPACE=default_namespace/
SESSION_SECRET_KEY=signalzero
ENABLE_EMBED_RESOLUTION=True

# FastMCP Server Configuration
FASTAPI_HOST=127.0.0.1
FASTAPI_PORT=8082
FASTAPI_HTTPS_PORT=8443

# Logging & Monitoring
LOGGING_CONFIG_PATH=configs/logging_config.ini
ENABLE_AGENT_LOGGING=true
```

#### **Broker Connection Flow**
```
┌─────────────────┐    TCP:55555 (SMF)     ┌─────────────────┐
│  Spring Boot    │◄──────────────────────►│   Solace        │
│  Backend        │    Guaranteed Delivery  │   PubSub+       │
└─────────────────┘                         │   Event Broker  │
                                            │                 │
┌─────────────────┐    WS:8008 (Web Msg)   │                 │
│  SAM Gateway    │◄──────────────────────►│                 │
│  Agent Mesh     │    Agent Orchestration  └─────────────────┘
└─────────────────┘                         
        ▲
        │ MCP Protocol (stdin/stdout)
        ▼
┌─────────────────┐
│ FastMCP Servers │
│ (Ports 8001-8005) │
└─────────────────┘
```

## 📡 Solace Topic Structure

All topics follow the pattern defined in DETAILED_DESIGN.md Section 3.3:

```
signalzero/analysis/request/{userId}/{analysisId}
signalzero/agent/bot-detector/request
signalzero/agent/bot-detector/response
signalzero/agent/trend-analyzer/request
signalzero/agent/trend-analyzer/response
signalzero/agent/review-validator/request
signalzero/agent/review-validator/response
signalzero/agent/promotion-detector/request
signalzero/agent/promotion-detector/response
signalzero/agent/score-aggregator/request
signalzero/agent/score-aggregator/response
signalzero/updates/score/{analysisId}
signalzero/updates/status/{analysisId}
signalzero/usage/analysis/{userId}
signalzero/usage/limit-reached/{userId}
```

## 💰 Monetization Model

### Usage-Based Freemium Tiers

| Tier | Price | Monthly Analyses | Features |
|------|-------|-----------------|----------|
| **PUBLIC** | Free | 0 (View Only) | Live dashboard, Wall of Shame |
| **FREE** | $0 | 3 | Basic Reality Score |
| **PRO** | $99/mo | 100 | Detailed reports, API, history |
| **BUSINESS** | $499/mo | 1,000 | Team seats, priority queue |
| **ENTERPRISE** | Custom | Unlimited | Dedicated queue, SLA, white-label |

Usage tracked in real-time via Solace events - automatic enforcement at service level.

## 🗄️ Database Schema

The system uses 8 PostgreSQL tables (see DETAILED_DESIGN.md Section 6):
- `users` - User accounts with subscription tiers
- `analyses` - Analysis requests and results  
- `agent_results` - Individual agent responses
- `wall_of_shame` - High manipulation products
- `waitlist` - Marketing signups
- `payments` - Transaction records
- `automation_events` - Marketing automation tracking
- `api_keys` - External service credentials

## ✅ Demo Requirements

**These MUST work for the hackathon demo:**

1. ✅ Submit "Stanley Cup" → Returns 34% Reality Score
2. ✅ All 5 agents process in parallel
3. ✅ Real-time UI updates via WebSocket
4. ✅ Processing completes in < 3 seconds
5. ✅ Wall of Shame shows items > 60% bots
6. ✅ Usage limits enforced after 3 analyses
7. ✅ Fallback to hardcoded data if agents timeout

## 🚨 Production Code Standards

**NO PLACEHOLDERS, NO TODO COMMENTS, NO MOCK IMPLEMENTATIONS**

All code must be production-ready:
- Complete error handling with fallbacks
- Proper logging (no System.out.println)
- Real Solace connections
- Working database transactions
- Functional UI components

See [CLAUDE.md](CLAUDE.md) for detailed coding standards.

## 📂 Project Structure

Total: **100+ files** organized as:

```
s1gnal.zero.app/
├── .env.dev           (Development environment template)
├── .env.prod          (Production environment template)  
├── .gitignore         (Git ignore rules)
├── README.md          (This file)
├── LICENSE            (Proprietary license)
├── CLAUDE.md          (Development standards)
├── DETAILED_DESIGN.md (Complete technical specification)
├── signalzero-timeline-claude.md (Build timeline)
├── checkstyle.xml     (Java code style rules)
├── requirements.txt   (Python dependencies)
├── sam.log.1          (SAM execution logs)
│
├── backend/           (Spring Boot Java application)
│   ├── pom.xml        (Maven dependencies)
│   ├── package.json   (Frontend build config)
│   └── src/main/
│       ├── java/io/signalzero/
│       │   ├── SignalZeroApplication.java (Main app)
│       │   ├── config/    (Solace, Security, SAM configs)
│       │   ├── controller/(REST endpoints)
│       │   ├── messaging/ (Solace pub/sub, JCSMP)
│       │   ├── model/     (JPA entities, 8 tables)
│       │   ├── repository/(Database access layer)
│       │   ├── service/   (Business logic)
│       │   └── ui/        (Vaadin Flow views)
│       ├── resources/
│       │   ├── application.properties (Spring config)
│       │   └── META-INF/resources/ (Static assets)
│       └── frontend/  (Vaadin frontend resources)
│           └── themes/signalzero/ (Custom styling)
│
├── agents/            (Python AI agents + FastMCP servers)
│   ├── .env           (Python agents config - gitignored)
│   ├── requirements.txt (Python dependencies)
│   ├── start_all_agents.py (Old orchestrator)
│   ├── bot_detection_agent.py (Bot analysis logic)
│   ├── trend_analysis_agent.py (Viral pattern analysis)
│   ├── review_validator_agent.py (Review authenticity)
│   ├── paid_promotion_agent.py (Sponsored content detection)
│   ├── score_aggregator_agent.py (Reality Score calculation)
│   ├── base/          (Base agent classes)
│   │   └── base_agent.py
│   ├── config/        (Agent configuration)
│   │   └── config.py
│   ├── utils/         (Utilities and mock data)
│   │   ├── solace_client.py
│   │   └── mock_data_generator.py
│   └── mcp_servers/   (FastMCP protocol bridge)
│       ├── start_all_mcp_servers.py (MCP orchestrator)
│       ├── bot_detection_server.py (Port 8001)
│       ├── trend_analysis_server.py (Port 8002)
│       ├── review_validator_server.py (Port 8003)
│       ├── paid_promotion_server.py (Port 8004)
│       └── score_aggregator_server.py (Port 8005)
│
├── configs/           (SAM Agent Mesh configurations)
│   ├── shared_config.yaml (Common SAM settings)
│   ├── logging_config.ini (Logging configuration)
│   ├── agents/        (SAM agent YAML configs)
│   │   ├── main_orchestrator.yaml
│   │   ├── bot_detection_mcp_agent.yaml
│   │   ├── trend_analysis_mcp_agent.yaml
│   │   ├── review_validator_mcp_agent.yaml
│   │   ├── paid_promotion_mcp_agent.yaml
│   │   └── score_aggregator_mcp_agent.yaml
│   └── gateways/      (Event Mesh Gateway configs)
│       ├── signalzero-event-mesh.yaml
│       └── webui.yaml
│
├── database/          (PostgreSQL schema and data)
│   ├── 01_create_schema.sql (8 tables + views)
│   ├── 02_create_functions.sql (Database functions)
│   ├── 03_create_views.sql (Analytics views)
│   ├── 04_seed_demo_data.sql (Demo data)
│   └── reset_database.sh (Database reset script)
│
├── docs/              (Documentation and landing page)
│   ├── API_KEYS_GUIDE.md (API setup instructions)
│   ├── prd.html       (Product requirements)
│   ├── s1gnal-zero-tech-design.html (Technical design)
│   ├── ui_mockup.html (UI mockups)
│   └── fomo-killer-logos (4).html (Logo designs)
│
├── solace/            (Solace PubSub+ resources)
├── solace-samples-java/ (Solace Java examples)
├── solace-samples-python/ (Solace Python examples)
└── src/               (Additional source files)
    └── __init__.py
```

## 🤝 Team

Built with ❤️ by the S1GNAL.ZERO team at AGI Ventures Canada Hackathon 3.0

### Project Creator

**Shawn Jackson Dyck** - Leader behind the build
- Email: [shawn@samjdtechnologies.com](mailto:shawn@samjdtechnologies.com)
- GitHub: [@samjd-zz](https://github.com/samjd-zz)
- Known for: [Answer42](https://github.com/samjd-zz/answer42)

### Event Organizers

* Hai
* Sukhpal Saini
* Kaan UN
* Neilda Pacquing Gagné
* Thoufeek Baber
* Susan Habib

## 📄 License

This project is licensed under a **Proprietary License** - see the [LICENSE](LICENSE) file for complete terms.

### Key Points:
- ✅ **Open for Learning**: View and study the code for educational purposes
- ✅ **Non-Commercial Research**: Use for academic and personal research projects
- ❌ **Commercial Use Prohibited**: Contact us for commercial licensing
- ❌ **No Redistribution**: Cannot redistribute or create competing products
- 🤝 **Commercial Licensing Available**: Enterprise and custom licenses available

For commercial use, enterprise licensing, or custom implementations, contact:
- **Email**: s1gnal.zero.42@gmail.com
- **Subject**: "Commercial License Inquiry"

## 🙏 Acknowledgments

* **[Solace](https://solace.com)** for sponsoring and providing PubSub+ platform
* **[AGI Ventures Canada](https://lu.ma/ai-tinkerers-ottawa)** for organizing the hackathon
* The open source community for inspiration

## 📞 Contact

* **Website**: [https://s1gnal-zero.github.io](https://s1gnal-zero.github.io)
* **Email**: [s1gnal.zero.42@gmail.com](mailto:s1gnal.zero.42@gmail.com)
* **GitHub**: [@S1GNAL-ZERO](https://github.com/S1GNAL-ZERO)
* **Event**: [AGI Ventures Canada](https://lu.ma/ai-tinkerers-ottawa)

---

<div align="center">

**Detecting truth in digital noise, one signal at a time.**

*S1GNAL.ZERO - Because authenticity matters in the age of virality.*

**⚠️ REMINDER: ALL CODE MUST BE PRODUCTION READY - NO PLACEHOLDERS**

</div>
