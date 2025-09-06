# S1GNAL.ZERO - Technical Build Timeline for Claude

## AGI Ventures Canada Hackathon 3.0 - September 6-7, 2025

> **PRIMARY REFERENCE**: Follow `DETAILED_DESIGN.md` for all implementation details. This timeline provides the build order and quick references to design sections.

---

## 📋 PRE-HACKATHON TECHNICAL SETUP

### Development Environment

Refer to **DETAILED_DESIGN.md Section 21.1** for complete environment setup.

- [x] Java 17+ installed and `java -version` works
- [x] Maven 3.8+ installed and `mvn -version` works  
- [x] Python 3.10+ installed and `python --version` works
- [x] Docker Desktop running
- [x] PostgreSQL 14+ installed and `psql --version` works
- [x] Git configured with GitHub credentials

### Required Downloads

- [x] Solace Java JCSMP samples from GitHub
- [x] Solace Python samples from GitHub
- [x] Spring Initializr accessible (for Vaadin Flow project)

---

## 🚀 HOUR 0: INFRASTRUCTURE SETUP (0:00-1:00) - ✅ COMPLETED

### 0:00-0:15 | Start Core Services - ✅ COMPLETED

**Reference**: DETAILED_DESIGN.md Section 2.2 & Section 5

**Claude Task: Start Solace and PostgreSQL**

```bash
# Start Solace PubSub+ Docker (Section 5.1)
docker run -d -p 55555:55555 -p 8080:8080 -p 1883:1883 \
  --shm-size=2g --env username_admin_globalaccesslevel=admin \
  --env username_admin_password=admin --name=solace \
  solace/solace-pubsub-standard

# Create PostgreSQL database (Section 6)
createdb signalzero
```

**Files to Create**: None yet - just services

### 0:15-0:30 | Database Schema - ✅ COMPLETED (via Supabase MCP)

**Reference**: DETAILED_DESIGN.md Section 6 (Complete Database Schema)

**Claude Task: Create database schema**
Create: Database schema created via Supabase MCP - All 8 tables, views, functions and demo data

**Files Created**:

- Database schema with 8 tables, 3 views, 2 functions (created via Supabase MCP)

### 0:30-0:45 | Spring Boot Project - ✅ COMPLETED

**Reference**: DETAILED_DESIGN.md Section 7 & Section 20.2

**Claude Task: Generate Spring Boot project with Vaadin Flow**

**Files Created**:

- ✅ `backend/pom.xml` (dependencies from Section 7.1, fixed Maven issues)
- ✅ `backend/src/main/resources/application.properties` (Section 5.3)

### 0:45-1:00 | Core Configuration - ✅ COMPLETED

**Reference**: DETAILED_DESIGN.md Section 5.3 & Section 7.2

**Claude Task: Configure application.properties**

**Files Created**:

- ✅ `backend/src/main/resources/application.properties`
- ✅ `backend/src/main/java/io/signalzero/SignalZeroApplication.java` (with @Push)

---

## 🔧 HOUR 1: SOLACE INTEGRATION (1:00-2:00) - 🔄 IN PROGRESS

### 1:00-1:15 | Solace Configuration Classes - ✅ COMPLETED

**Reference**: DETAILED_DESIGN.md Section 5.4

**Files Created** (from Section 20.4 order):

1. ✅ `backend/src/main/java/io/signalzero/config/SolaceProperties.java`
2. ✅ `backend/src/main/java/io/signalzero/config/SolaceConfig.java` (Section 5.4)
3. ✅ `backend/src/main/java/io/signalzero/messaging/SolaceTopics.java` (Section 3.3)

### 1:15-1:30 | Publisher Service - ✅ COMPLETED

**Reference**: DETAILED_DESIGN.md Section 8.1

**Files Created**:

1. ✅ `backend/src/main/java/io/signalzero/messaging/SolacePublisher.java` (Section 8.1)

### 1:30-1:45 | Consumer Service - ✅ COMPLETED

**Reference**: DETAILED_DESIGN.md Section 8.2

**Files Created**:

1. ✅ `backend/src/main/java/io/signalzero/messaging/SolaceConsumer.java` (Section 8.2)
2. ✅ `backend/src/main/java/io/signalzero/messaging/AgentResponseHandler.java`

### 1:45-2:00 | Solace Message Models (Entity-Based) - ✅ COMPLETED

**Reference**: DETAILED_DESIGN.md Section 8 - Repository Pattern

**Files Created**:

1. ✅ `backend/src/main/java/io/signalzero/messaging/AnalysisRequestMessage.java` - Simple message wrapper
2. ✅ `backend/src/main/java/io/signalzero/messaging/AgentResponseMessage.java` - Agent result wrapper
3. ✅ `backend/src/main/java/io/signalzero/messaging/MessageUtils.java` - Entity serialization utilities

**Repository Pattern**: Even Solace messages work with entities. No DTOs anywhere in the system.

```java
// Message classes work with entities directly
public class AnalysisRequestMessage {
    private String analysisId;
    private String userId; 
    private String query;
    // Convert to/from Analysis entity
}

// Services convert messages to entities immediately
@Service
public class SolaceConsumer {
    @Autowired
    private AnalysisRepository analysisRepository;

    public void handleMessage(AnalysisRequestMessage message) {
        Analysis analysis = analysisRepository.findById(UUID.fromString(message.getAnalysisId()));
        // Work with entity throughout
    }
}
```

---

## 🏗️ HOUR 2: CORE BACKEND SERVICES (2:00-3:00)

### 2:00-2:15 | JPA Entities

**Reference**: DETAILED_DESIGN.md Section 7.3

**Files to Create** (exact order from Section 20.4):

1. `backend/src/main/java/io/signalzero/model/User.java`
2. `backend/src/main/java/io/signalzero/model/Analysis.java`
3. `backend/src/main/java/io/signalzero/model/AgentResult.java`
4. `backend/src/main/java/io/signalzero/model/SubscriptionTier.java` (enum)
5. `backend/src/main/java/io/signalzero/model/AnalysisStatus.java` (enum)

### 2:15-2:30 | Repository Interfaces (Pure Data Access Layer)

**Reference**: DETAILED_DESIGN.md Section 8 - Repository-Based Data Access Pattern

**Files to Create**:

1. `backend/src/main/java/io/signalzero/repository/UserRepository.java` - extends JpaRepository<User, UUID>
2. `backend/src/main/java/io/signalzero/repository/AnalysisRepository.java` - extends JpaRepository<Analysis, UUID>
3. `backend/src/main/java/io/signalzero/repository/AgentResultRepository.java` - extends JpaRepository<AgentResult, UUID>
4. `backend/src/main/java/io/signalzero/repository/WallOfShameRepository.java` - extends JpaRepository<WallOfShame, UUID>

**No DTOs Anywhere**: All data operations work directly with JPA entities through repository methods.

```java
public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    List<Analysis> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Analysis> findByIsPublicTrueOrderByCreatedAtDesc();
    List<Analysis> findByBotPercentageGreaterThan(BigDecimal threshold);

    @Query("SELECT a FROM Analysis a WHERE a.realityScore < :score")
    List<Analysis> findManipulatedAnalyses(@Param("score") BigDecimal score);
}
```

### 2:30-2:45 | Core Services

**Reference**: DETAILED_DESIGN.md Section 9

**Files to Create**:

1. `backend/src/main/java/io/signalzero/service/AnalysisService.java` (Section 9.1)
2. `backend/src/main/java/io/signalzero/service/UserService.java`
3. `backend/src/main/java/io/signalzero/service/UsageTrackingService.java` (Section 9.3)

**Important**: Use EXACT Reality Score calculation from Section 9.2:

- Bot: 40%, Trend: 30%, Review: 20%, Promotion: 10%

### 2:45-3:00 | REST Controllers

**Reference**: DETAILED_DESIGN.md Section 10

**Files to Create**:

1. `backend/src/main/java/io/signalzero/controller/AnalysisController.java` (Section 10.1)
2. `backend/src/main/java/io/signalzero/controller/AuthController.java`
3. `backend/src/main/java/io/signalzero/controller/DashboardController.java`

---

## 🎨 HOUR 3: VAADIN FLOW UI - SERVER-SIDE JAVA (3:00-4:00)

### 3:00-3:15 | Vaadin Flow Setup

**Reference**: DETAILED_DESIGN.md Section 11

**Important**: Vaadin Flow is server-side Java - NO Node.js, React, or separate frontend build

**Files to Create**:

1. `backend/src/main/java/io/signalzero/SignalZeroApplication.java` (with @Push)
2. `backend/src/main/resources/META-INF/resources/themes/signalzero/styles.css` (Vaadin theme)

### 3:15-3:30 | Main Dashboard with Repository Pattern

**Reference**: DETAILED_DESIGN.md Section 11.1 & Section 8

**Files to Create**:

1. `backend/src/main/java/io/signalzero/ui/DashboardView.java` - Direct entity binding to Vaadin Grid
2. `backend/src/main/java/io/signalzero/ui/components/RealityScoreGauge.java`

**Repository Pattern in Vaadin**:

```java
@Route("")
@Push
public class DashboardView extends VerticalLayout {
    @Autowired
    private AnalysisRepository analysisRepository;

    private Grid<Analysis> analysisGrid;

    public DashboardView() {
        // Grid directly bound to Analysis entities
        analysisGrid = new Grid<>(Analysis.class);
        analysisGrid.setItems(analysisRepository.findAllByIsPublicTrueOrderByCreatedAtDesc());
        // No DTO conversion needed - work directly with entities
    }
}
```

### 3:30-3:45 | Analysis View

**Reference**: DETAILED_DESIGN.md Section 11.2

**Files to Create**:

1. `backend/src/main/java/io/signalzero/ui/AnalysisView.java`
2. `backend/src/main/java/io/signalzero/ui/components/AnalysisForm.java`

### 3:45-4:00 | Real-time Updates

**Reference**: DETAILED_DESIGN.md Section 11.3

**Files to Create**:

1. `backend/src/main/java/io/signalzero/ui/AnalysisUpdateBroadcaster.java` (Section 11.3)
2. `backend/src/main/java/io/signalzero/ui/WallOfShameView.java`

---

## 🤖 HOUR 4: PYTHON AGENTS - BASE (4:00-5:00)

### 4:00-4:15 | Python Project Structure

**Reference**: DETAILED_DESIGN.md Section 20.3 & Section 4

**Create Directory Structure** (from Section 20.3):

```
agents/
├── requirements.txt        (Section 4.1 dependencies)
├── config/
│   └── config.py           (Section 4.3 connection details)
├── base/
│   └── base_agent.py       (Section 4.2 base class)
├── agents/                 (individual agents)
└── utils/
    └── mock_data.py        (Section 4.5 demo data)
```

### 4:15-4:30 | Base Agent Class

**Reference**: DETAILED_DESIGN.md Section 4.2

**Files to Create**:

1. `agents/base/base_agent.py` - Copy EXACT code from Section 4.2
2. `agents/config/config.py` - Solace connection settings

### 4:30-4:45 | Bot Detection Agent

**Reference**: DETAILED_DESIGN.md Section 4.4

**Files to Create**:

1. `agents/agents/bot_detection_agent.py` - Use implementation from Section 4.4

**Critical**: Must return 62% bots for "Stanley Cup" (hardcoded)

### 4:45-5:00 | Mock Data Module

**Reference**: DETAILED_DESIGN.md Section 4.5

**Files to Create**:

1. `agents/utils/mock_data.py` - Demo data generators from Section 4.5
2. `agents/test_integration.py` - Test script

---

## 🚀 HOUR 5: MULTI-AGENT SYSTEM (5:00-6:00)

### 5:00-5:10 | Trend Analysis Agent

**Reference**: DETAILED_DESIGN.md Section 4.4

**Files to Create**:

1. `agents/agents/trend_analysis_agent.py`

### 5:10-5:20 | Review Validator Agent

**Reference**: DETAILED_DESIGN.md Section 4.4

**Files to Create**:

1. `agents/agents/review_validator_agent.py`

### 5:20-5:30 | Promotion Detector Agent

**Reference**: DETAILED_DESIGN.md Section 4.4

**Files to Create**:

1. `agents/agents/promotion_detector_agent.py`

### 5:30-5:45 | Score Aggregator Agent

**Reference**: DETAILED_DESIGN.md Section 4.4

**Files to Create**:

1. `agents/agents/score_aggregator_agent.py`

**Critical**: Use EXACT weighted calculation from Section 9.2

### 5:45-6:00 | Agent Orchestrator

**Reference**: DETAILED_DESIGN.md Section 4

**Files to Create**:

1. `agents/start_all_agents.py` - Launches all 5 agents
2. `agents/stop_all_agents.sh` - Cleanup script

---

## ⚡ HOUR 6: INTEGRATION & TESTING (6:00-7:00)

### 6:00-6:15 | WebSocket Integration

**Reference**: DETAILED_DESIGN.md Section 11.3

**Update Files**:

- `AnalysisService.java` - Add real-time broadcasting
- `DashboardView.java` - Add push listeners

### 6:15-6:30 | Demo Data Setup

**Reference**: DETAILED_DESIGN.md Section 14

**Files to Create**:

1. `database/demo_data.sql` - Copy from Section 14

**Critical Demo Values** (Section 14):

- Stanley Cup: 62% bots, 34% Reality Score
- $BUZZ: 87% bots, 12% Reality Score  
- Prime Energy: 71% bots, 29% Reality Score

### 6:30-6:45 | Error Handling

**Reference**: DETAILED_DESIGN.md Section 13

**Update All Services** with:

- Timeout handling (5 seconds max)
- Fallback to mock data
- User-friendly error messages

### 6:45-7:00 | End-to-End Testing

**Reference**: DETAILED_DESIGN.md Section 15

**Test Checklist** (from Section 15.1):

1. [ ] Submit "Stanley Cup" analysis
2. [ ] All 5 agents process in parallel
3. [ ] Reality Score = 34% (±2%)
4. [ ] Processing time < 3 seconds
5. [ ] Real-time updates work
6. [ ] Wall of Shame shows high-bot items
7. [ ] Usage limits enforced after 3 analyses

---

## 🎯 HOURS 7-12: REMAINING FEATURES

### Payment Integration (Optional)

**Reference**: DETAILED_DESIGN.md Section 16

- Skip if running out of time
- Use mock payment flow for demo

### Marketing Features (Optional)

**Reference**: DETAILED_DESIGN.md Section 12

- Skip automation agents
- Focus on core functionality

### Static Landing Page (Hour 11)

**Reference**: DETAILED_DESIGN.md Section 18

- Separate marketing page (static HTML/CSS/JS)
- Deploy to GitHub Pages (marketing only)
- Main app UI is Vaadin Flow (runs with Spring Boot)

---

## 📝 CRITICAL NOTES FOR CLAUDE

### Follow DETAILED_DESIGN.md Exactly For:

1. **Reality Score Calculation** - Section 9.2 (40/30/20/10 weights)
2. **Demo Data Values** - Section 14 (Stanley Cup = 34%)
3. **Solace Topics** - Section 3.3 (exact naming)
4. **Database Schema** - Section 6 (all 8 tables)
5. **File Structure** - Section 20 (73+ files)

### Hardcoded Demo Values (Section 14):

```python
# In every agent - MUST match
if "stanley cup" in query.lower():
    return {"bot_percentage": 62, "reality_score": 34}
elif "$buzz" in query.lower():  
    return {"bot_percentage": 87, "reality_score": 12}
```

### Skip If Time Constrained (Section 19):

1. Payment processing (use mock)
2. Email notifications
3. Marketing automation
4. Advanced authentication
5. API rate limiting

### Must Work For Demo (Section 15):

- [ ] Live analysis of any query
- [ ] Stanley Cup returns 34% Reality Score
- [ ] All 5 agents running
- [ ] < 3 second response time
- [ ] Wall of Shame populated
- [ ] Real-time UI updates

---

## 🚨 EMERGENCY FALLBACKS

From DETAILED_DESIGN.md Section 13:

1. **Solace fails**: Use in-memory Spring Events
2. **Agents timeout**: Return hardcoded scores
3. **Database issues**: Use H2 in-memory
4. **UI doesn't update**: Add manual refresh
5. **Everything fails**: Static demo video ready

---

## 📂 FINAL FILE COUNT CHECK

Per DETAILED_DESIGN.md Section 20.4, you should have:

- **Backend**: 35+ Java files
- **Frontend**: 5+ Vaadin Flow views (server-side Java)
- **Agents**: 10+ Python files
- **Database**: 2 SQL files
- **Config**: 5+ configuration files
- **Landing Page**: 3 static files (separate from Vaadin UI)

**Total**: ~73 files minimum

---

**Remember**: This timeline references DETAILED_DESIGN.md which is your source of truth. When in doubt, check the design document for exact implementation details.
