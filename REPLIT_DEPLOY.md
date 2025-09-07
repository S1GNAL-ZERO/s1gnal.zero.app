# 🚀 S1GNAL.ZERO - Replit Deployment Guide

## AGI Ventures Canada Hackathon 3.0 | September 6-7, 2025

This guide explains how to deploy and run S1GNAL.ZERO on Replit for demonstration purposes.

## 📋 Prerequisites

- Replit account
- Basic understanding of Java/Spring Boot applications

## 🎯 Quick Start

1. **Import to Replit**
   ```bash
   # Fork this repository or import via GitHub URL
   https://github.com/S1GNAL-ZERO/s1gnal.zero.app.git
   ```

2. **Auto-Configuration**
   - Replit will automatically detect the `.replit` and `replit.nix` files
   - The environment will be set up with Java 17, Maven, PostgreSQL, and Python

3. **Run the Application**
   - Click the "Run" button in Replit
   - The startup script will automatically:
     - Set up PostgreSQL database
     - Install dependencies
     - Build the application
     - Start the web server

4. **Access the Application**
   - Once started, the app will be available at your Replit URL
   - Look for: `https://YOUR-REPL-NAME.YOUR-USERNAME.repl.co`

## 🏗️ Architecture for Replit

### Hybrid Production Setup

This Replit deployment now runs the **full production architecture** with:

- **Spring Boot Backend**: Java 17 + Vaadin Flow (port 8081 → 8080)
- **Solace PubSub+ Event Broker**: Docker container (ports 55555, 8080, 1883, 8008)
- **SAM Agent Mesh**: Native installation with event mesh gateway
- **FastMCP Servers**: 5 Python servers (ports 8001-8005)
- **PostgreSQL Database**: Native local instance (port 5432)
- **Real AI Agent Processing**: All agents working with live message broker

### What's Enabled

✅ **Production Features**:
- **Real Agent Processing**: All 5 AI agents connected via SAM
- **Solace Event Broker**: Full message routing and guaranteed delivery
- **Reality Score™ Calculation**: Live agent result aggregation
- **WebSocket Push**: Real-time updates from agent processing
- **Event Mesh Gateway**: SAM routing analysis requests to agents
- **FastMCP Protocol**: Standardized tool integration
- **Parallel Processing**: < 3 second analysis with real agents

✅ **UI Features**:
- Reality Score™ Gauge with live data updates
- Dashboard with real agent results
- Wall of Shame with live bot detection
- Analysis interface with production backend
- Dark theme UI with WebSocket notifications

⚠️ **Simplified for Replit**:
- External API connections may be limited
- LLM API key required for full SAM functionality
- Memory optimized for ~2GB constraint

## 🔧 Configuration Files

### `.replit`
Main configuration file that defines:
- Run command: `bash scripts/start-replit.sh`
- Environment variables for demo mode
- Language server settings
- Deployment configuration

### `replit.nix`
Nix environment specification including:
- Java 17 OpenJDK
- Maven build tool
- PostgreSQL database
- Python 3.11
- Node.js for frontend compilation

### `scripts/start-replit.sh`
Startup script that:
- Initializes PostgreSQL database
- Applies database schema
- Builds the Java application
- Starts the web server

## 🗄️ Database Setup

The startup script automatically:
1. Creates a PostgreSQL instance in `$REPL_HOME/postgres`
2. Applies schema from `database/01_create_schema.sql`
3. Creates functions from `database/02_create_functions.sql`
4. Sets up views from `database/03_create_views.sql`
5. Seeds demo data from `database/04_seed_demo_data.sql`

## 🎮 Demo Features

### Reality Score™ Analysis
- Search for any product/trend (e.g., "Stanley Cup tumbler")
- Watch the spinning gauge animation during processing
- See mock results with 34% Reality Score, 62% bot detection

### Wall of Shame
- View products with high bot manipulation
- Real-time updates via WebSocket

### Dashboard
- Overview of system metrics
- Recent analysis results
- Agent activity simulation

## 🔍 Environment Variables

Key environment variables set by `.replit`:

```bash
SPRING_PROFILES_ACTIVE=replit
DEMO_MODE=true
USE_MOCK_DATA=true
HACKATHON_MODE=true
DATABASE_URL=jdbc:postgresql://localhost:5432/main
SERVER_PORT=8080
```

## 🛠️ Development

### Local Development in Replit
1. Use the built-in terminal to run commands
2. Edit Java files with full IntelliSense support
3. Hot-reload is enabled for most changes
4. View logs in the console

### Debugging
- Java debugger is configured on port 5005
- Use Replit's debugging tools
- Check console output for detailed logging

## 📱 Mobile Compatibility

The Vaadin UI is responsive and works on:
- Desktop browsers
- Mobile browsers
- Tablets

## 🚨 Limitations on Replit

Due to Replit's environment constraints:
- Memory limited to ~1GB
- No persistent external connections
- No real-time message broker
- Limited to mock data for AI agents

## 🎯 Perfect for Hackathon Demo

This Replit setup is ideal for:
- Quick demonstrations
- Hackathon presentations
- Proof of concept sharing
- No-setup testing

## 📞 Support

If you encounter issues:
1. Check the console logs
2. Verify all files are present
3. Try restarting the Repl
4. Check the database connection

## 🔗 Related Links

- [Main Repository](https://github.com/S1GNAL-ZERO/s1gnal.zero.app)
- [Technical Documentation](DETAILED_DESIGN.md)
- [Project README](README.md)

---

**S1GNAL.ZERO** - AI-Powered Authenticity Verification  
*Built for AGI Ventures Canada Hackathon 3.0*
