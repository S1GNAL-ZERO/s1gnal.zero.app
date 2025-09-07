#!/bin/bash

# S1GNAL.ZERO Replit Startup Script - Hybrid SAM + Solace Docker Architecture
# AGI Ventures Canada Hackathon 3.0 (September 6-7, 2025)

set -e  # Exit on any error

echo "==================================================================="
echo "   🚀 S1GNAL.ZERO - Hybrid SAM + Solace Docker Setup"
echo "   AGI Ventures Canada Hackathon 3.0 | September 6-7, 2025"
echo "==================================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%H:%M:%S')] ERROR: $1${NC}"
}

# Function to check if a service is running
check_service() {
    local service_name=$1
    local port=$2
    
    if nc -z localhost $port 2>/dev/null; then
        log "$service_name is running on port $port"
        return 0
    else
        warn "$service_name is not running on port $port"
        return 1
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    log "Waiting for $service_name to be ready on port $port..."
    while [ $attempt -le $max_attempts ]; do
        if nc -z localhost $port 2>/dev/null; then
            log "$service_name is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    error "$service_name failed to start after $((max_attempts * 2)) seconds"
    return 1
}

# Set up environment variables for Replit hybrid setup
export JAVA_HOME="${JAVA_HOME:-/nix/store/*/lib/openjdk}"
export MAVEN_OPTS="-Xmx1024m -Xms512m"
export SPRING_PROFILES_ACTIVE="replit"
export SERVER_PORT="8080"

# Production mode with real agents
export DEMO_MODE="false"
export USE_MOCK_DATA="false"
export HACKATHON_MODE="true"

# Solace connection (Docker container)
export SOLACE_HOST="tcp://localhost:55555"
export SOLACE_VPN="default"
export SOLACE_USERNAME="admin"
export SOLACE_PASSWORD="admin"
export SOLACE_CLIENT_NAME="signalzero-replit"

# SAM Agent Mesh configuration
export SOLACE_BROKER_URL="ws://localhost:8008"
export SOLACE_BROKER_VPN="default"
export SOLACE_BROKER_USERNAME="default"
export SOLACE_BROKER_PASSWORD="default"
export NAMESPACE="signalzero/"

# LLM Configuration (required for SAM)
export LLM_SERVICE_ENDPOINT="${LLM_SERVICE_ENDPOINT:-https://api.anthropic.com}"
export LLM_SERVICE_API_KEY="${LLM_SERVICE_API_KEY:-your_key_here}"
export LLM_SERVICE_PLANNING_MODEL_NAME="anthropic/claude-3-haiku-20240307"
export LLM_SERVICE_GENERAL_MODEL_NAME="anthropic/claude-3-haiku-20240307"
export LLM_SERVICE_ANALYSIS_MODEL_NAME="anthropic/claude-3-haiku-20240307"

log "Environment setup complete for hybrid architecture"
log "JAVA_HOME: $JAVA_HOME"
log "Spring Profile: $SPRING_PROFILES_ACTIVE"
log "Solace Host: $SOLACE_HOST"

# Initialize PostgreSQL if not already done
log "Setting up PostgreSQL database..."

if [ ! -d "$REPL_HOME/postgres" ]; then
    log "Initializing PostgreSQL database..."
    mkdir -p "$REPL_HOME/postgres"
    initdb -D "$REPL_HOME/postgres"
    
    # Configure PostgreSQL for Replit
    cat >> "$REPL_HOME/postgres/postgresql.conf" << EOF
port = 5432
listen_addresses = 'localhost'
max_connections = 20
shared_buffers = 32MB
EOF
    
    cat >> "$REPL_HOME/postgres/pg_hba.conf" << EOF
local all all trust
host all all 127.0.0.1/32 trust
host all all ::1/128 trust
EOF
fi

# Start PostgreSQL if not running
if ! check_service "PostgreSQL" 5432; then
    log "Starting PostgreSQL..."
    pg_ctl -D "$REPL_HOME/postgres" -l "$REPL_HOME/postgres/logfile" start
    sleep 3
    
    # Create database and user if they don't exist
    if ! psql -lqt | cut -d \| -f 1 | grep -qw main; then
        log "Creating database and user..."
        createdb main
        psql -d main -c "CREATE SCHEMA IF NOT EXISTS signalzero;"
    fi
fi

# Set up database schema
log "Setting up database schema..."
if [ -f "database/01_create_schema.sql" ]; then
    log "Applying database schema..."
    export DATABASE_URL="jdbc:postgresql://localhost:5432/main"
    export DATABASE_USERNAME="$USER"
    export DATABASE_PASSWORD=""
    
    # Apply database scripts
    psql -d main -f database/01_create_schema.sql || warn "Schema creation had warnings"
    psql -d main -f database/02_create_functions.sql || warn "Functions creation had warnings"
    psql -d main -f database/03_create_views.sql || warn "Views creation had warnings"
    psql -d main -f database/04_seed_demo_data.sql || warn "Data seeding had warnings"
fi

# Start Docker daemon if not running
log "Starting Docker daemon..."
if ! pgrep dockerd > /dev/null 2>&1; then
    dockerd > /dev/null 2>&1 &
    sleep 5
    log "Docker daemon started"
fi

# Start Solace PubSub+ Event Broker in Docker
log "Starting Solace PubSub+ Event Broker..."
if ! docker ps | grep -q "solace"; then
    log "Starting Solace container..."
    docker run -d -p 55555:55555 -p 8080:8080 -p 1883:1883 -p 8008:8008 \
        --shm-size=1g --env username_admin_globalaccesslevel=admin \
        --env username_admin_password=admin --name=solace \
        solace/solace-pubsub-standard:latest || warn "Solace container may have startup issues"
    
    # Wait for Solace to be ready
    wait_for_service "Solace SMF" 55555
    wait_for_service "Solace Web Messaging" 8008
    log "Solace PubSub+ Event Broker is ready!"
else
    log "Solace container already running"
fi

# Install Python dependencies for agents and SAM
log "Setting up Python environment for AI agents and SAM..."
if [ -f "agents/requirements.txt" ]; then
    log "Installing Python agent dependencies..."
    pip install -r agents/requirements.txt --user --quiet || warn "Some Python packages may not have installed"
fi

# Install SAM Agent Mesh
log "Installing Solace Agent Mesh (SAM)..."
pip install solace-agent-mesh --user --quiet || warn "SAM installation may have had issues"

# Install FastMCP dependencies
log "Installing FastMCP dependencies..."
pip install fastmcp --user --quiet || warn "FastMCP installation may have had issues"

# Start FastMCP servers
log "Starting FastMCP servers..."
cd agents/mcp_servers

# Start all MCP servers in background
log "Starting Bot Detection MCP Server (port 8001)..."
python bot_detection_server.py > ../logs/bot_detection.log 2>&1 &
BOT_DETECTION_PID=$!

log "Starting Trend Analysis MCP Server (port 8002)..."  
python trend_analysis_server.py > ../logs/trend_analysis.log 2>&1 &
TREND_ANALYSIS_PID=$!

log "Starting Review Validator MCP Server (port 8003)..."
python review_validator_server.py > ../logs/review_validator.log 2>&1 &
REVIEW_VALIDATOR_PID=$!

log "Starting Paid Promotion MCP Server (port 8004)..."
python paid_promotion_server.py > ../logs/paid_promotion.log 2>&1 &
PAID_PROMOTION_PID=$!

log "Starting Score Aggregator MCP Server (port 8005)..."
python score_aggregator_server.py > ../logs/score_aggregator.log 2>&1 &
SCORE_AGGREGATOR_PID=$!

# Create log directory
mkdir -p ../logs

# Wait for FastMCP servers to be ready
sleep 5
wait_for_service "Bot Detection MCP" 8001
wait_for_service "Trend Analysis MCP" 8002
wait_for_service "Review Validator MCP" 8003
wait_for_service "Paid Promotion MCP" 8004
wait_for_service "Score Aggregator MCP" 8005

log "All FastMCP servers are running!"

# Start SAM Event Mesh Gateway
log "Starting SAM Event Mesh Gateway..."
cd ../../
sam run configs/gateways/signalzero-event-mesh.yaml > sam_gateway.log 2>&1 &
SAM_GATEWAY_PID=$!

log "SAM Event Mesh Gateway started"

# Create cleanup function
cleanup() {
    log "Shutting down services..."
    kill $BOT_DETECTION_PID $TREND_ANALYSIS_PID $REVIEW_VALIDATOR_PID $PAID_PROMOTION_PID $SCORE_AGGREGATOR_PID $SAM_GATEWAY_PID 2>/dev/null || true
    docker stop solace 2>/dev/null || true
    docker rm solace 2>/dev/null || true
}

# Set up trap for cleanup on exit
trap cleanup EXIT

# Create Replit-specific application properties
log "Creating Replit-specific configuration..."
cat > backend/src/main/resources/application-replit.properties << EOF
# S1GNAL.ZERO Replit Configuration
spring.application.name=signalzero
server.port=8080

# Replit Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/main
spring.datasource.username=$USER
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration for Replit
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.default_schema=signalzero
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Demo Mode Configuration
app.demo-mode=true
app.use-mock-data=true
app.hackathon-mode=true
app.auto-seed-data=true

# Vaadin Configuration for Replit
vaadin.launch-browser=false
vaadin.whitelisted-packages=io.signalzero
vaadin.servlet.productionMode=false

# Security Configuration
jwt.secret=replit-s1gn4lz3r0-demo-secret-key
spring.security.user.name=admin
spring.security.user.password=admin

# Disable external services for Replit demo
solace.host=tcp://localhost:55555
solace.vpnName=default
solace.username=admin
solace.password=admin

# Logging Configuration
logging.level.io.signalzero=INFO
logging.level.root=WARN
EOF

# Build the application
log "Building S1GNAL.ZERO application..."
cd backend

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    error "Maven not found! Please ensure Maven is installed."
    exit 1
fi

# Clean and compile
log "Cleaning previous build..."
mvn clean -q

log "Compiling application..."
mvn compile -q -DskipTests

log "Preparing Vaadin frontend..."
mvn vaadin:prepare-frontend -q

# Start the application
log "Starting S1GNAL.ZERO application..."
log "🌐 Application will be available at: https://$REPL_SLUG.$REPL_OWNER.repl.co"

echo ""
echo "==================================================================="
echo "   🎯 S1GNAL.ZERO - Ready for Demo!"
echo "   📊 Solace Admin: Will be available when agents are connected"
echo "   🤖 Multi-Agent System: Mock data mode for Replit demo"
echo "   💾 Database: PostgreSQL with demo data loaded"
echo "   ⚡ WebSocket: Push notifications enabled"
echo "   🎨 UI: Dark theme with Reality Score™ gauge"
echo "==================================================================="

# Run the application
exec mvn spring-boot:run -Dspring-boot.run.profiles=replit -Dspring-boot.run.jvmArguments="-Xmx1024m -Xms512m"
