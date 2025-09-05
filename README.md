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
│     Python Agent Mesh (5 Agents)        │
├──────────────────────────────────────────┤
│ • Bot Detection Agent                   │
│ • Trend Analysis Agent                  │
│ • Review Validator Agent                │
│ • Promotion Detector Agent              │
│ • Score Aggregator Agent                │
└──────────────┬──────────────────────────┘
               │
         ┌─────▼─────┐
         │PostgreSQL │
         └───────────┘
```

## 🚦 Getting Started

### Prerequisites

* Docker Desktop running
* Java 17+ 
* Python 3.10+
* PostgreSQL 14+
* Maven 3.8+

### Quick Installation

Follow the hour-by-hour timeline in [signalzero-timeline-claude.md](signalzero-timeline-claude.md) for detailed build instructions.

```bash
# Clone the repository
git clone https://github.com/S1GNAL-ZERO/S1GNAL-ZERO.github.io.git
cd S1GNAL-ZERO

# Start Solace PubSub+ Docker
docker run -d -p 55555:55555 -p 8080:8080 -p 1883:1883 \
  --shm-size=2g --env username_admin_globalaccesslevel=admin \
  --env username_admin_password=admin --name=solace \
  solace/solace-pubsub-standard

# Set up PostgreSQL database
createdb signalzero
psql signalzero < database/schema.sql
psql signalzero < database/demo_data.sql

# Start Spring Boot backend (port 8081)
cd backend
mvn clean install
mvn spring-boot:run

# Start Python agents (in new terminal)
cd agents
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python start_all_agents.py

# Access the application
open http://localhost:8081
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

Total: **73+ files** organized as:

```
S1GNAL-ZERO/
├── backend/           (35+ Java files)
│   └── src/main/java/io/signalzero/
│       ├── config/    (Solace, Security)
│       ├── controller/(REST endpoints)
│       ├── dto/       (Data transfer objects)
│       ├── messaging/ (Solace pub/sub)
│       ├── model/     (JPA entities)
│       ├── repository/(Database access)
│       ├── service/   (Business logic)
│       └── ui/        (Vaadin views)
├── agents/            (10+ Python files)
│   ├── agents/        (5 agent implementations)
│   ├── base/          (Base agent class)
│   ├── config/        (Connection settings)
│   └── utils/         (Mock data generators)
├── database/          (2 SQL files)
│   ├── schema.sql     (8 tables, views, functions)
│   └── demo_data.sql  (Seed data)
├── frontend/          (Vaadin Flow themes & resources)
└── docs/              (Landing page)
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

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

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
