# CLAUDE.md - S1GNAL.ZERO Project Instructions

This file provides critical instructions to Claude when building the S1GNAL.ZERO hackathon project.

## 🚨 CRITICAL RULES - NO EXCEPTIONS

1. **NO PLACEHOLDERS** - Every piece of code must be complete and functional
2. **NO TODO COMMENTS** - Implement everything fully or don't include it
3. **NO MOCK IMPLEMENTATIONS** - Use real Solace connections, real agents, real calculations
4. **NO PARTIAL SOLUTIONS** - If you start a feature, complete it entirely
5. **PRODUCTION READY** - Code must work in the hackathon demo without modification

## 📚 Primary References

- **DETAILED_DESIGN.md** - Your source of truth for ALL implementation details
- **signalzero-timeline-claude.md** - Hour-by-hour build timeline with section references

## 🎯 Core Requirements

### Hardcoded Demo Values (MUST MATCH EXACTLY)
```java
// In AnalysisService.java and all Python agents
if (query.toLowerCase().contains("stanley cup")) {
    return new AnalysisResult(62, 34); // 62% bots, 34% Reality Score
}
if (query.toLowerCase().contains("$buzz")) {
    return new AnalysisResult(87, 12); // 87% bots, 12% Reality Score  
}
if (query.toLowerCase().contains("prime energy")) {
    return new AnalysisResult(71, 29); // 71% bots, 29% Reality Score
}
```

### Reality Score Calculation (EXACT WEIGHTS)
```java
// From DETAILED_DESIGN.md Section 9.2
public BigDecimal calculateRealityScore(BigDecimal botScore, 
                                       BigDecimal trendScore,
                                       BigDecimal reviewScore,
                                       BigDecimal promotionScore) {
    return botScore.multiply(new BigDecimal("0.4"))
        .add(trendScore.multiply(new BigDecimal("0.3")))
        .add(reviewScore.multiply(new BigDecimal("0.2")))
        .add(promotionScore.multiply(new BigDecimal("0.1")));
}
```

## 🏗️ Build Commands

### Java Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Python Agents
```bash
cd agents
python -m venv venv
source venv/bin/activate  # or venv\Scripts\activate on Windows
pip install -r requirements.txt
python start_all_agents.py
```

### Database Setup
```bash
createdb signalzero
psql -d signalzero < database/schema.sql
psql -d signalzero < database/demo_data.sql
```

### Solace Docker
```bash
docker run -d -p 55555:55555 -p 8080:8080 -p 1883:1883 \
  --shm-size=2g --env username_admin_globalaccesslevel=admin \
  --env username_admin_password=admin --name=solace \
  solace/solace-pubsub-standard
```

## 💻 Java Code Standards

### Package Structure
```
io.signalzero
├── config        # Solace, Security configurations
├── controller    # REST endpoints
├── dto          # Data transfer objects
├── messaging    # Solace publishers/consumers
├── model        # JPA entities
├── repository   # Database access
├── service      # Business logic
└── ui           # Vaadin views
```

### Required Annotations
```java
@Service          // All service classes
@RestController   // All REST controllers  
@Entity          // All JPA entities
@Repository      // All repository interfaces
@Configuration   // All config classes
@Transactional   // Service methods modifying data
@Push            // Main application class for Vaadin
```

### Error Handling
```java
// NEVER return null or empty responses
try {
    return analysisService.analyze(request);
} catch (Exception e) {
    log.error("Analysis failed for query: {}", request.getQuery(), e);
    // Return fallback with mock data - NEVER fail the demo
    return createFallbackResponse(request.getQuery());
}
```

### Solace Integration
```java
// ALWAYS include correlation IDs
TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
msg.setCorrelationId(analysisId);
msg.setText(jsonPayload);
producer.send(msg, topic);
```

## 🐍 Python Agent Standards

### Base Agent Pattern (MUST FOLLOW)
```python
class BotDetectionAgent(BaseAgent):
    def __init__(self):
        super().__init__("bot-detector")
        
    def process(self, data: dict) -> dict:
        query = data.get('query', '')
        
        # HARDCODED DEMO VALUES - REQUIRED
        if "stanley cup" in query.lower():
            return {
                'analysisId': data['analysisId'],
                'botPercentage': 62,
                'score': 38  # 100 - botPercentage
            }
        
        # Generate realistic data for other queries
        # NEVER return empty or None
        return self.generate_analysis(query)
```

### Agent Requirements
1. All 5 agents MUST run simultaneously
2. Each agent MUST connect to Solace on startup
3. Each agent MUST handle messages within 2 seconds
4. Each agent MUST include hardcoded demo values
5. Each agent MUST publish responses to correct topics

## 🎨 Vaadin UI Standards

### View Structure
```java
@Route("")
@PageTitle("S1GNAL.ZERO Dashboard")
@Push // REQUIRED for real-time updates
public class DashboardView extends VerticalLayout {
    // NO placeholder components
    // Every button must work
    // Every grid must display real data
    // WebSocket updates must function
}
```

### Real-time Updates
```java
// REQUIRED in every view showing analysis data
@Override
protected void onAttach(AttachEvent event) {
    UI ui = event.getUI();
    broadcasterRegistration = AnalysisUpdateBroadcaster.register(analysis -> {
        ui.access(() -> {
            // Update UI with real data
            updateAnalysisInGrid(analysis);
        });
    });
}
```

## 🗄️ Database Requirements

### Schema Rules
1. Use ALL 8 tables from DETAILED_DESIGN.md Section 6
2. Include ALL indexes for performance
3. Include ALL views for reporting
4. Include calculate_reality_score function
5. Seed demo data on startup

### JPA Entities
```java
@Entity
@Table(name = "analyses")
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    // ALL fields from schema - NO shortcuts
    // Include proper relationships
    // Include timestamps
}
```

## 📡 Solace Topic Structure (EXACT NAMING)

```java
public class SolaceTopics {
    // From DETAILED_DESIGN.md Section 3.3 - USE EXACTLY
    public static final String ANALYSIS_REQUEST = "signalzero/analysis/request/";
    public static final String BOT_DETECTOR_REQUEST = "signalzero/agent/bot-detector/request";
    public static final String BOT_DETECTOR_RESPONSE = "signalzero/agent/bot-detector/response";
    // ... ALL topics from Section 3.3
}
```

## ✅ Demo Checklist (MUST ALL WORK)

1. [ ] Submit "Stanley Cup" returns 34% Reality Score
2. [ ] All 5 agents process messages in parallel
3. [ ] Real-time UI updates via WebSocket
4. [ ] Processing completes in < 3 seconds
5. [ ] Wall of Shame shows items > 60% bots
6. [ ] Usage limits enforced after 3 analyses
7. [ ] No errors in console during demo
8. [ ] Fallback to mock data if agents timeout

## 🚫 FORBIDDEN PRACTICES

1. **NO** `// TODO: Implement later`
2. **NO** `throw new NotImplementedException()`
3. **NO** `return null;` without fallback
4. **NO** empty catch blocks
5. **NO** commented-out code
6. **NO** System.out.println (use proper logging)
7. **NO** hardcoded localhost URLs (use config)
8. **NO** Thread.sleep() in production code
9. **NO** incomplete UI components
10. **NO** "Coming Soon" placeholders

## 🔥 Emergency Fallbacks (REQUIRED)

```java
// In AnalysisService.java
private Analysis createFallbackAnalysis(String query) {
    // NEVER let the demo fail
    if (query.toLowerCase().contains("stanley cup")) {
        return createHardcodedAnalysis(query, 62, 34);
    }
    // Generate believable random data
    int botPercentage = ThreadLocalRandom.current().nextInt(30, 80);
    int realityScore = 100 - (int)(botPercentage * 0.7);
    return createHardcodedAnalysis(query, botPercentage, realityScore);
}
```

## 📊 File Count Verification

Per DETAILED_DESIGN.md Section 20.4, you MUST create:
- **35+ Java files** in backend/
- **10+ Python files** in agents/
- **2 SQL files** in database/
- **5+ Vaadin views** 
- **3 landing page files**
- **Total: ~73 files minimum**

## 🎯 Final Reminder

**Every line of code you write must work in the hackathon demo.**

If you're unsure about implementation details:
1. Check DETAILED_DESIGN.md for specifications
2. Use the fallback patterns shown above
3. Make it work first, optimize never
4. The demo must not fail under any circumstances

**Ship working code. No excuses. No placeholders. Production ready.**
