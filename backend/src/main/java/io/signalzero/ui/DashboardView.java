package io.signalzero.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import io.signalzero.model.Analysis;
import io.signalzero.model.AnalysisStatus;
import io.signalzero.model.ManipulationLevel;
import io.signalzero.model.AgentResult;
import io.signalzero.repository.AnalysisRepository;
import io.signalzero.repository.AgentResultRepository;
import io.signalzero.service.AnalysisService;
import io.signalzero.ui.components.RealityScoreGauge;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * S1GNAL.ZERO - Main Dashboard View
 * AGI Ventures Canada Hackathon 3.0 (September 6-7, 2025)
 * 
 * Main dashboard interface for AI-powered authenticity verification system.
 * Features:
 * - Real-time analysis input form
 * - Reality Score™ gauge visualization  
 * - Live analysis results grid
 * - WebSocket push notifications for real-time updates
 * 
 * CRITICAL REQUIREMENTS (from CLAUDE.md):
 * - Repository pattern with direct JPA entity binding (NO DTOs)
 * - Real-time updates via @Push annotation
 * - Hardcoded demo values for consistent hackathon demo
 * - Production-ready UI components with proper error handling
 * 
 * Reference: DETAILED_DESIGN.md Section 11 - Vaadin UI Components
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("S1GNAL.ZERO Dashboard - AI-Powered Authenticity Verification")
@AnonymousAllowed
public class DashboardView extends VerticalLayout {

    // Dependencies - direct repository access (no service layer needed for simple operations)
    @Autowired
    private AnalysisService analysisService;
    
    @Autowired 
    private AnalysisRepository analysisRepository;
    
    @Autowired
    private AgentResultRepository agentResultRepository;

    // UI Components
    private TextField queryField;
    private Button analyzeButton;
    private RealityScoreGauge realityScoreGauge;
    private Grid<Analysis> recentAnalysesGrid;
    private Grid<AgentResult> agentResultsGrid;
    private Span totalAnalysesCount;
    private Span avgBotPercentage;
    private Span avgRealityScore;
    
    // Real-time update registration
    private Registration broadcasterRegistration;
    
    // Current analysis tracking
    private Analysis currentAnalysis;

    /**
     * Initialize the dashboard view with all components.
     * Uses repository pattern with direct JPA entity binding throughout.
     */
    public DashboardView() {
        addClassName("dashboard-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Allow the view to be taller than viewport to accommodate all content
        getStyle().set("min-height", "100vh");
        getStyle().set("height", "auto"); // Allow natural height based on content
        getStyle().set("display", "flex");
        getStyle().set("flex-direction", "column");
        getStyle().set("overflow", "visible"); // Allow natural scrolling
        
        // Initialize the query field first
        queryField = new TextField();
        queryField.setPlaceholder("Enter product name, influencer handle, or trending topic...");
        queryField.addClassName("analysis-input");
        queryField.setWidthFull();
        queryField.setClearButtonVisible(true);
        queryField.setHelperText("Try: 'Stanley Cup', 'Prime Energy', '$BUZZ', or any viral product");
        
        createMainContent();
    }
    
    /**
     * Initialize dashboard data after Spring dependency injection is complete.
     * This ensures repositories are properly injected before attempting to query them.
     */
    @PostConstruct
    private void initDashboardData() {
        // Load initial data from repository after Spring DI is complete
        refreshDashboardData();
    }

    /**
     * Create main content area matching the mockup design.
     */
    private void createMainContent() {
        // Set up the main view container to fill full height
        setFlexGrow(1);
        getStyle().set("padding", "20px 20px 0 20px"); // Padding but no bottom padding
        getStyle().set("box-sizing", "border-box");
        
        // KPI Cards Section - compact fixed height
        HorizontalLayout kpiSection = createKPISection();
        kpiSection.setWidthFull();
        kpiSection.getStyle().set("margin-bottom", "12px");
        kpiSection.getStyle().set("flex-shrink", "0");
        
        // Reality Score Overview Panel - more compact
        Div realityScorePanel = createRealityScorePanel();
        realityScorePanel.setWidthFull();
        realityScorePanel.getStyle().set("flex", "0 0 auto");
        realityScorePanel.getStyle().set("height", "320px"); // Increased height to better fit agent data
        realityScorePanel.getStyle().set("margin-bottom", "12px");
        
        // Latest Flags Panel - takes all remaining space to bottom with increased height
        Div latestFlagsPanel = createLatestFlagsPanel();
        latestFlagsPanel.setWidthFull();
        latestFlagsPanel.getStyle().set("flex", "1 1 0"); // Grow to fill remaining space
        latestFlagsPanel.getStyle().set("min-height", "400px"); // Increased minimum height to show more rows
        latestFlagsPanel.getStyle().set("max-height", "none");
        latestFlagsPanel.getStyle().set("overflow", "hidden");
        
        // Footer section - fixed at bottom
        Div footer = createFooter();
        footer.getStyle().set("flex-shrink", "0");
        footer.getStyle().set("margin-top", "12px");
        footer.getStyle().set("padding", "16px 0");
        
        // Add all sections directly to the main view
        add(kpiSection, realityScorePanel, latestFlagsPanel, footer);
    }
    
    /**
     * Create KPI section with three metric cards matching the mockup.
     */
    private HorizontalLayout createKPISection() {
        HorizontalLayout kpiLayout = new HorizontalLayout();
        kpiLayout.addClassName("grid");
        kpiLayout.addClassName("cols-3");   
        kpiLayout.setWidthFull();
        kpiLayout.setSpacing(true);
        
        // Avg Bot Detection KPI
        Div botKPI = createKPICard("📈", "Avg. Bot Detection", "73%");
        
        // Median Analysis Time KPI
        Div timeKPI = createKPICard("⚡", "Median Analysis Time", "5s");
        
        // Active Data Sources KPI
        Div sourcesKPI = createKPICard("🔌", "Active Data Sources", "12");
        
        kpiLayout.add(botKPI, timeKPI, sourcesKPI);
        return kpiLayout;
    }
    
    /**
     * Create a KPI card matching the mockup design.
     */
    private Div createKPICard(String icon, String label, String value) {
        Div kpiCard = new Div();
        kpiCard.addClassName("kpi");
        kpiCard.getStyle().set("background", "var(--panel-2)");
        kpiCard.getStyle().set("border-radius", "14px");
        kpiCard.getStyle().set("border", "1px solid #27306a");
        kpiCard.getStyle().set("padding", "16px");
        kpiCard.getStyle().set("display", "flex");
        kpiCard.getStyle().set("align-items", "center");
        kpiCard.getStyle().set("gap", "14px");
        
        // Icon container
        Div iconContainer = new Div();
        iconContainer.addClassName("icon");
        iconContainer.getStyle().set("width", "38px");
        iconContainer.getStyle().set("height", "38px");
        iconContainer.getStyle().set("border-radius", "10px");
        iconContainer.getStyle().set("background", "var(--accent)");
        iconContainer.getStyle().set("display", "grid");
        iconContainer.getStyle().set("place-items", "center");
        iconContainer.getElement().setProperty("innerHTML", icon);
        
        // Text content
        VerticalLayout textContent = new VerticalLayout();
        textContent.setSpacing(false);
        textContent.setPadding(false);
        
        Span labelSpan = new Span(label);
        labelSpan.addClassName("muted");
        labelSpan.getStyle().set("color", "var(--muted)");
        
        Span valueSpan = new Span(value);
        valueSpan.addClassName("big");
        valueSpan.getStyle().set("font-size", "24px");
        valueSpan.getStyle().set("font-weight", "800");
        
        textContent.add(labelSpan, valueSpan);
        kpiCard.add(iconContainer, textContent);
        
        return kpiCard;
    }
    
    /**
     * Create Reality Score Overview panel.
     */
    private Div createRealityScorePanel() {
        Div panel = new Div();
        panel.addClassName("panel");
        panel.getStyle().set("background", "var(--panel)");
        panel.getStyle().set("border", "1px solid #1b2452");
        panel.getStyle().set("border-radius", "var(--radius)");
        panel.getStyle().set("box-shadow", "var(--shadow)");
        
        // Panel header
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("panel-header");
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.getStyle().set("padding", "16px");
        header.getStyle().set("border-bottom", "1px solid #1b2452");
        
        Span title = new Span("Reality Score Overview");
        title.addClassName("panel-title");
        title.getStyle().set("font-weight", "700");
        
        // Tags
        HorizontalLayout tags = new HorizontalLayout();
        Span liveTag = new Span("Live");
        liveTag.addClassName("tag");
        liveTag.getStyle().set("padding", "6px 10px");
        liveTag.getStyle().set("border-radius", "999px");
        liveTag.getStyle().set("font-size", "12px");
        liveTag.getStyle().set("border", "1px solid #2b376f");
        liveTag.getStyle().set("background", "#121942");
        liveTag.getStyle().set("color", "#b6c3ff");
        
        Span timeTag = new Span("Last 24h");
        timeTag.addClassName("tag");
        timeTag.getStyle().set("padding", "6px 10px");
        timeTag.getStyle().set("border-radius", "999px");
        timeTag.getStyle().set("font-size", "12px");
        timeTag.getStyle().set("border", "1px solid #2b376f");
        timeTag.getStyle().set("background", "#121942");
        timeTag.getStyle().set("color", "#b6c3ff");
        
        tags.add(liveTag, timeTag);
        header.add(title, tags);
        
        // Panel body with Reality Score gauge
        HorizontalLayout body = new HorizontalLayout();
        body.addClassName("panel-body");
        body.setSpacing(true);
        body.getStyle().set("padding", "16px");
        body.setWidthFull();
        
        // Score wrap section (left side) - takes 55% of width
        HorizontalLayout scoreWrap = new HorizontalLayout();
        scoreWrap.addClassName("score-wrap");
        scoreWrap.setAlignItems(Alignment.CENTER);
        scoreWrap.setSpacing(true);
        scoreWrap.setWidth("55%");
        scoreWrap.getStyle().set("flex-shrink", "0");
        
        // Add the existing Reality Score gauge
        realityScoreGauge = new RealityScoreGauge();
        
        // Score details
        VerticalLayout scoreDetails = new VerticalLayout();
        scoreDetails.setSpacing(false);
        scoreDetails.setPadding(false);
        
        Span scoreLabel = new Span("Reality Score");
        scoreLabel.addClassName("score-label");
        scoreLabel.getStyle().set("font-size", "12px");
        scoreLabel.getStyle().set("letter-spacing", ".08em");
        scoreLabel.getStyle().set("text-transform", "uppercase");
        scoreLabel.getStyle().set("color", "var(--muted)");
        
        Span scoreValue = new Span("34%");
        scoreValue.addClassName("score-value");
        scoreValue.getStyle().set("font-size", "28px"); // Reduced from 40px
        scoreValue.getStyle().set("font-weight", "900");
        scoreValue.getStyle().set("line-height", "1");
        scoreValue.getStyle().set("color", "#f59e0b"); // Orange color for mixed signals
        
        Span scoreCaption = new Span("Mostly manufactured hype");
        scoreCaption.addClassName("muted");
        scoreCaption.getStyle().set("color", "#94a3b8"); // Light gray color
        scoreCaption.getStyle().set("font-size", "14px");
        
        // Tags for findings with proper styling
        HorizontalLayout findingsTags = new HorizontalLayout();
        findingsTags.setSpacing(true);
        findingsTags.getStyle().set("margin-top", "10px");
        
        Span botTag = new Span("🤖 Bot surge");
        botTag.addClassName("tag");
        botTag.getStyle().set("padding", "4px 8px");
        botTag.getStyle().set("border-radius", "999px");
        botTag.getStyle().set("font-size", "12px");
        botTag.getStyle().set("font-weight", "500");
        botTag.getStyle().set("background", "rgba(239, 68, 68, 0.1)");
        botTag.getStyle().set("color", "#ef4444");
        botTag.getStyle().set("border", "1px solid rgba(239, 68, 68, 0.3)");
        
        Span promoTag = new Span("💰 Paid promos");
        promoTag.addClassName("tag");
        promoTag.getStyle().set("padding", "4px 8px");
        promoTag.getStyle().set("border-radius", "999px");
        promoTag.getStyle().set("font-size", "12px");
        promoTag.getStyle().set("font-weight", "500");
        promoTag.getStyle().set("background", "rgba(245, 158, 11, 0.1)");
        promoTag.getStyle().set("color", "#f59e0b");
        promoTag.getStyle().set("border", "1px solid rgba(245, 158, 11, 0.3)");
        
        Span clusterTag = new Span("⭐ Review clusters");
        clusterTag.addClassName("tag");
        clusterTag.getStyle().set("padding", "4px 8px");
        clusterTag.getStyle().set("border-radius", "999px");
        clusterTag.getStyle().set("font-size", "12px");
        clusterTag.getStyle().set("font-weight", "500");
        clusterTag.getStyle().set("background", "rgba(168, 85, 247, 0.1)");
        clusterTag.getStyle().set("color", "#a855f7");
        clusterTag.getStyle().set("border", "1px solid rgba(168, 85, 247, 0.3)");
        
        findingsTags.add(botTag, promoTag, clusterTag);
        
        scoreDetails.add(scoreLabel, scoreValue, scoreCaption, findingsTags);
        scoreWrap.add(realityScoreGauge, scoreDetails);
        
        // Agent Results section - shows real agent data (right side) - takes 45% of width
        Div agentResultsSection = createAgentResultsSection();
        agentResultsSection.setWidth("45%");
        agentResultsSection.getStyle().set("flex-shrink", "0");
        
        body.add(scoreWrap, agentResultsSection);
        panel.add(header, body);
        
        return panel;
    }
    
    /**
     * Create Latest Flags panel.
     */
    private Div createLatestFlagsPanel() {
        Div panel = new Div();
        panel.addClassName("panel");
        panel.getStyle().set("background", "var(--panel)");
        panel.getStyle().set("border", "1px solid #1b2452");
        panel.getStyle().set("border-radius", "var(--radius)");
        panel.getStyle().set("box-shadow", "var(--shadow)");
        panel.getStyle().set("display", "flex");
        panel.getStyle().set("flex-direction", "column");
        panel.getStyle().set("height", "100%");
        panel.getStyle().set("overflow", "hidden");
        
        // Panel header - compact fixed size
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("panel-header");
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.getStyle().set("padding", "12px 16px"); // More compact padding
        header.getStyle().set("border-bottom", "1px solid #1b2452");
        header.getStyle().set("flex-shrink", "0");
        
        Span title = new Span("Latest Flags");
        title.addClassName("panel-title");
        title.getStyle().set("font-weight", "700");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.addClassName("btn");
        refreshButton.addClassName("secondary");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> refreshDashboardData());
        
        header.add(title, refreshButton);
        
        // Panel body with table - grows to fill remaining space
        Div body = new Div();
        body.addClassName("panel-body");
        body.getStyle().set("padding", "8px 16px 16px 16px"); // Reduce top padding
        body.getStyle().set("flex", "1 1 0"); // Grow to fill remaining space
        body.getStyle().set("display", "flex");
        body.getStyle().set("flex-direction", "column");
        body.getStyle().set("min-height", "0");
        body.getStyle().set("overflow", "hidden");
        
        createRecentAnalysesGrid();
        // Make grid fill the body completely
        recentAnalysesGrid.getStyle().set("flex", "1 1 0");
        recentAnalysesGrid.getStyle().set("min-height", "0");
        recentAnalysesGrid.getStyle().set("height", "100%");
        recentAnalysesGrid.getStyle().set("overflow", "auto"); // Let grid handle its own scrolling
        
        body.add(recentAnalysesGrid);
        
        panel.add(header, body);
        return panel;
    }

    /**
     * Create the analysis input form with Reality Score gauge.
     * Repository pattern: Form submits directly to AnalysisService which works with entities.
     */
    private void createAnalysisForm() {
        Div formSection = new Div();
        formSection.addClassName("s1gnal-card");
        formSection.getStyle().set("margin", "2rem 0");
        formSection.getStyle().set("padding", "2rem");
        
        H2 formTitle = new H2("🔍 Analyze Viral Trends & Products");
        formTitle.getStyle().set("margin-top", "0");
        formTitle.getStyle().set("color", "var(--s1gnal-primary)");
        
        Paragraph description = new Paragraph(
            "Enter any viral product, trend, or influencer to instantly detect bot manipulation, fake reviews, and coordinated campaigns."
        );
        description.getStyle().set("color", "var(--s1gnal-text-secondary)");
        description.getStyle().set("margin-bottom", "2rem");
        
        // Analysis input form
        HorizontalLayout inputLayout = new HorizontalLayout();
        inputLayout.setWidthFull();
        inputLayout.setAlignItems(Alignment.END);
        
        queryField = new TextField();
        queryField.setPlaceholder("Enter product name, influencer handle, or trending topic...");
        queryField.addClassName("analysis-input");
        queryField.setWidthFull();
        queryField.setClearButtonVisible(true);
        
        // Add demo suggestions
        queryField.setHelperText("Try: 'Stanley Cup', 'Prime Energy', '$BUZZ', or any viral product");
        
        analyzeButton = new Button("🚀 ANALYZE");
        analyzeButton.addClassName("analyze-button");
        analyzeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        analyzeButton.setIcon(new Icon(VaadinIcon.SEARCH));
        
        // Handle analysis submission - works directly with entities via service
        analyzeButton.addClickListener(e -> performAnalysis());
        queryField.addKeyPressListener(event -> {
            if (event.getKey().equals("Enter")) {
                performAnalysis();
            }
        });
        
        inputLayout.add(queryField, analyzeButton);
        inputLayout.setFlexGrow(1, queryField);
        
        // Reality Score gauge - displays current analysis results
        realityScoreGauge = new RealityScoreGauge();
        realityScoreGauge.getStyle().set("align-self", "center");
        realityScoreGauge.getStyle().set("margin", "2rem 0");
        
        formSection.add(formTitle, description, inputLayout, realityScoreGauge);
        add(formSection);
    }

    /**
     * Create statistics section showing aggregate data from repository.
     */
    private void createStatsSection() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        statsLayout.getStyle().set("gap", "2rem");
        
        // Total analyses stat
        Div totalAnalysesCard = createStatCard("📊", "Total Analyses", "0");
        totalAnalysesCount = (Span) totalAnalysesCard.getChildren()
            .filter(component -> component instanceof Span && ((Span) component).getText().matches("\\d+"))
            .findFirst()
            .orElse(new Span("0"));
        
        // Average bot percentage stat  
        Div avgBotCard = createStatCard("🤖", "Avg Bot %", "0%");
        avgBotPercentage = (Span) avgBotCard.getChildren()
            .filter(component -> component instanceof Span && ((Span) component).getText().contains("%"))
            .findFirst()
            .orElse(new Span("0%"));
        
        // Average Reality Score stat
        Div avgScoreCard = createStatCard("🎯", "Avg Reality Score", "0%");
        avgRealityScore = (Span) avgScoreCard.getChildren()
            .filter(component -> component instanceof Span && ((Span) component).getText().contains("%"))
            .findFirst()
            .orElse(new Span("0%"));
        
        statsLayout.add(totalAnalysesCard, avgBotCard, avgScoreCard);
        add(statsLayout);
    }

    /**
     * Create a statistics card component.
     */
    private Div createStatCard(String icon, String label, String value) {
        Div card = new Div();
        card.addClassName("s1gnal-card");
        card.getStyle().set("text-align", "center");
        card.getStyle().set("padding", "1.5rem");
        card.getStyle().set("min-width", "200px");
        
        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "2rem");
        iconSpan.getStyle().set("margin-bottom", "0.5rem");
        
        H3 labelH3 = new H3(label);
        labelH3.getStyle().set("margin", "0.5rem 0");
        labelH3.getStyle().set("color", "var(--s1gnal-text-secondary)");
        labelH3.getStyle().set("font-size", "0.9rem");
        labelH3.getStyle().set("font-weight", "normal");
        
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-size", "2rem");
        valueSpan.getStyle().set("font-weight", "bold");
        valueSpan.getStyle().set("color", "var(--s1gnal-primary)");
        
        card.add(iconSpan, labelH3, valueSpan);
        return card;
    }

    /**
     * Create the binary shield logo component using the design from the logos file.
     */
    private Div createBinaryShieldLogo() {
        Div logoDiv = new Div();
        logoDiv.getElement().setProperty("innerHTML", 
            "<svg width=\"48\" height=\"48\" viewBox=\"0 0 100 100\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<!-- Shield -->" +
            "<path d=\"M50 10 L80 22 L80 55 C80 73 50 90 50 90 C50 90 20 73 20 55 L20 22 Z\" " +
            "fill=\"url(#logoGrad)\"/>" +
            "<!-- Binary pattern -->" +
            "<text x=\"35\" y=\"35\" font-family=\"monospace\" font-size=\"10\" fill=\"white\" opacity=\"0.7\">101</text>" +
            "<text x=\"55\" y=\"35\" font-family=\"monospace\" font-size=\"10\" fill=\"white\" opacity=\"0.7\">010</text>" +
            "<text x=\"30\" y=\"48\" font-family=\"monospace\" font-size=\"10\" fill=\"white\" opacity=\"0.7\">0110</text>" +
            "<text x=\"55\" y=\"48\" font-family=\"monospace\" font-size=\"10\" fill=\"white\" opacity=\"0.7\">1001</text>" +
            "<text x=\"35\" y=\"61\" font-family=\"monospace\" font-size=\"10\" fill=\"white\" opacity=\"0.7\">110</text>" +
            "<text x=\"55\" y=\"61\" font-family=\"monospace\" font-size=\"10\" fill=\"white\" opacity=\"0.7\">001</text>" +
            "<!-- Central checkmark -->" +
            "<path d=\"M38 45 L45 52 L62 38\" stroke=\"white\" stroke-width=\"4\" stroke-linecap=\"round\" stroke-linejoin=\"round\" fill=\"none\"/>" +
            "<defs>" +
            "<linearGradient id=\"logoGrad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">" +
            "<stop offset=\"0%\" style=\"stop-color:#667eea;stop-opacity:1\" />" +
            "<stop offset=\"100%\" style=\"stop-color:#764ba2;stop-opacity:1\" />" +
            "</linearGradient>" +
            "</defs>" +
            "</svg>");
        return logoDiv;
    }

    /**
     * Create recent analyses grid using direct repository queries.
     * Repository pattern: Grid bound directly to Analysis entities.
     */
    private void createRecentAnalysesGrid() {
        // Create grid with direct entity binding - no wrapper div needed
        recentAnalysesGrid = new Grid<>(Analysis.class, false);
        recentAnalysesGrid.addClassName("s1gnal-grid");
        recentAnalysesGrid.setSizeFull(); // Fill available space
        
        // Configure columns to display entity data directly
        recentAnalysesGrid.addColumn(Analysis::getQuery)
            .setHeader("Query")
            .setFlexGrow(2);
            
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            HorizontalLayout botLayout = new HorizontalLayout();
            botLayout.setAlignItems(Alignment.CENTER);
            botLayout.setSpacing(false);
            botLayout.getStyle().set("gap", "8px");
            
            // Bot percentage with text-only styling (no background)
            Span botSpan = new Span(analysis.getBotPercentage() + "%");
            int botPercentage = analysis.getBotPercentage().intValue();
            
            // Color-coded bot percentage - text only
            if (botPercentage > 60) {
                botSpan.getStyle().set("color", "#ef4444"); // Red for high bot activity
            } else if (botPercentage > 30) {
                botSpan.getStyle().set("color", "#f59e0b"); // Orange for moderate bot activity
            } else {
                botSpan.getStyle().set("color", "#10b981"); // Green for low bot activity
            }
            
            botSpan.getStyle().set("font-weight", "700");
            botSpan.getStyle().set("font-size", "14px");
            
            botLayout.add(botSpan);
            return botLayout;
        })).setHeader("Bot %").setFlexGrow(1);
        
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            HorizontalLayout scoreLayout = new HorizontalLayout();
            scoreLayout.setAlignItems(Alignment.CENTER);
            scoreLayout.setSpacing(false);
            scoreLayout.getStyle().set("gap", "8px");
            
            // Reality Score without decimals and smaller font
            int realityScore = analysis.getRealityScore().intValue();
            Span scoreSpan = new Span(realityScore + "%");
            ManipulationLevel level = analysis.getManipulationLevel();
            
            // Color coding - text only (no background)
            String color;
            if (level == ManipulationLevel.GREEN) {
                color = "#10b981";
            } else if (level == ManipulationLevel.YELLOW) {
                color = "#f59e0b";
            } else {
                color = "#ef4444";
            }
            
            // Reduced font size from 2.5rem to 1.8rem
            scoreSpan.getStyle().set("color", color);
            scoreSpan.getStyle().set("font-size", "1.8rem");
            scoreSpan.getStyle().set("font-weight", "700");
            scoreSpan.getStyle().set("line-height", "1");
            scoreSpan.getStyle().set("margin", "0");
            
            scoreLayout.add(scoreSpan);
            return scoreLayout;
        })).setHeader("Reality Score").setFlexGrow(1);
        
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            HorizontalLayout classificationLayout = new HorizontalLayout();
            classificationLayout.setAlignItems(Alignment.CENTER);
            classificationLayout.setSpacing(false);
            
            // Create status tag matching AnalysisView style (no background)
            Span badge = createStatusTag(analysis.getManipulationLevel().getDescription(), 
                analysis.getManipulationLevel());
            
            classificationLayout.add(badge);
            return classificationLayout;
        })).setHeader("Classification").setFlexGrow(2).setWidth("200px"); // Increased flex and width for classification
        
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            HorizontalLayout statusLayout = new HorizontalLayout();
            statusLayout.setAlignItems(Alignment.CENTER);
            statusLayout.setSpacing(false);
            statusLayout.getStyle().set("gap", "6px");
            
            Icon statusIcon;
            String color, bgColor;
            switch (analysis.getStatus()) {
                case PENDING:
                    statusIcon = new Icon(VaadinIcon.CLOCK);
                    color = "#f59e0b";
                    bgColor = "rgba(245, 158, 11, 0.1)";
                    break;
                case PROCESSING:
                    statusIcon = new Icon(VaadinIcon.REFRESH);
                    color = "#667eea";
                    bgColor = "rgba(102, 126, 234, 0.1)";
                    statusIcon.addClassName("s1gnal-loading");
                    break;
                case COMPLETE:
                    statusIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
                    color = "#10b981";
                    bgColor = "rgba(16, 185, 129, 0.1)";
                    break;
                default:
                    statusIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
                    color = "#ef4444";
                    bgColor = "rgba(239, 68, 68, 0.1)";
            }
            
            // Enhanced status icon with background
            Div iconContainer = new Div();
            iconContainer.add(statusIcon);
            iconContainer.getStyle().set("display", "flex");
            iconContainer.getStyle().set("align-items", "center");
            iconContainer.getStyle().set("justify-content", "center");
            iconContainer.getStyle().set("width", "28px");
            iconContainer.getStyle().set("height", "28px");
            iconContainer.getStyle().set("border-radius", "50%");
            iconContainer.getStyle().set("background", bgColor);
            iconContainer.getStyle().set("border", "1px solid " + color + "33");
            
            statusIcon.getStyle().set("color", color);
            statusIcon.getStyle().set("width", "16px");
            statusIcon.getStyle().set("height", "16px");
            
            statusLayout.add(iconContainer);
            return statusLayout;
        })).setHeader("Status").setFlexGrow(0).setWidth("80px");
        
        recentAnalysesGrid.addColumn(analysis -> 
            analysis.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
        ).setHeader("Time").setFlexGrow(1);
    }

    /**
     * Create Agent Results section showing real production agent data.
     * Repository pattern: Direct AgentResult entity binding.
     */
    private Div createAgentResultsSection() {
        Div section = new Div();
        section.addClassName("agent-results");
        section.getStyle().set("height", "280px"); // Increased height to match panel increase
        section.getStyle().set("overflow-y", "auto");
        section.getStyle().set("margin-left", "-30px");
        
        // Title
        Span title = new Span("Agent Results");
        title.getStyle().set("font-size", "14px");
        title.getStyle().set("font-weight", "600");
        title.getStyle().set("color", "var(--text)");
        title.getStyle().set("margin-bottom", "8px"); // Reduced margin
        title.getStyle().set("display", "block");   
        
        // Create agent results grid
        agentResultsGrid = new Grid<>(AgentResult.class, false);
        agentResultsGrid.addClassName("agent-grid");
        agentResultsGrid.setHeight("190px"); // Reduced height by 50px to better fit data
        agentResultsGrid.getStyle().set("font-size", "12px"); // Smaller font for compactness
        agentResultsGrid.getStyle().set("--lumo-font-size-s", "11px");
        
        // Remove default padding/margins for better space utilization
        agentResultsGrid.getStyle().set("border", "1px solid #2b376f");
        agentResultsGrid.getStyle().set("border-radius", "8px");
        agentResultsGrid.getStyle().set("background", "var(--panel-2)");
        
        // Configure columns for agent data with improved sizing
        agentResultsGrid.addColumn(new ComponentRenderer<>(agentResult -> {
            Span agentSpan = new Span(getAgentDisplayName(agentResult.getAgentType()));
            agentSpan.getStyle().set("font-weight", "600");
            agentSpan.getStyle().set("font-size", "12px");
            agentSpan.getStyle().set("white-space", "nowrap");
            agentSpan.getStyle().set("overflow", "hidden");
            agentSpan.getStyle().set("text-overflow", "ellipsis");
            return agentSpan;
        })).setHeader("Agent")
          .setWidth("140px") // Increased width for agent names
          .setFlexGrow(0)
          .setResizable(true);
        
        agentResultsGrid.addColumn(new ComponentRenderer<>(agentResult -> {
            String finding = extractKeyFinding(agentResult);
            Span findingSpan = new Span(finding);
            findingSpan.getStyle().set("font-size", "12px"); // Improved readability
            findingSpan.getStyle().set("line-height", "1.3");
            findingSpan.getStyle().set("color", "#b6c3ff");
            // Allow text wrapping for long findings
            findingSpan.getStyle().set("white-space", "normal");
            findingSpan.getStyle().set("word-wrap", "break-word");
            findingSpan.getStyle().set("overflow-wrap", "break-word");
            findingSpan.setTitle(finding); // Tooltip for full text
            return findingSpan;
        })).setHeader("Key Finding")
          .setAutoWidth(true) // Auto width based on content
          .setFlexGrow(2) // Take more space
          .setResizable(true);
        
        agentResultsGrid.addColumn(new ComponentRenderer<>(agentResult -> {
            BigDecimal score = agentResult.getScore();
            Span scoreSpan = new Span(score.intValue() + "%");
            
            // Color based on score
            String color = score.intValue() >= 70 ? "#22c55e" : 
                          score.intValue() >= 40 ? "#f59e0b" : "#ef4444";
            scoreSpan.getStyle().set("color", color);
            scoreSpan.getStyle().set("font-weight", "600");
            scoreSpan.getStyle().set("font-size", "12px");
            scoreSpan.getStyle().set("text-align", "center");
            scoreSpan.getStyle().set("white-space", "nowrap");
            return scoreSpan;
        })).setHeader("Score")
          .setWidth("80px") // Increased width for score
          .setFlexGrow(0);
        
        // Enable column reordering and improve general appearance
        agentResultsGrid.setColumnReorderingAllowed(true);
        agentResultsGrid.setMultiSort(false);
        agentResultsGrid.setRowsDraggable(false);
        
        // Improve row spacing
        agentResultsGrid.getStyle().set("--lumo-space-m", "8px");
        
        section.add(title, agentResultsGrid);
        
        return section;
    }
    
    /**
     * Get display name for agent type.
     */
    private String getAgentDisplayName(String agentType) {
        switch (agentType.toLowerCase()) {
            case "bot-detector":
            case "bot-detection":
                return "Bot Detection";
            case "trend-analyzer":
            case "trend-analysis":
                return "Trend Analysis";
            case "review-validator":
                return "Review Validator";
            case "paid-promotion":
            case "promotion-detector":
                return "Paid Promotion";
            case "score-aggregator":
                return "Score Aggregator";
            default:
                return agentType.substring(0, 1).toUpperCase() + 
                       agentType.substring(1).replace("-", " ");
        }
    }
    
    /**
     * Extract key finding from agent evidence.
     */
    private String extractKeyFinding(AgentResult agentResult) {
        if (agentResult.getEvidence() != null) {
            Object keyFinding = agentResult.getEvidence().get("keyFinding");
            if (keyFinding != null) {
                return keyFinding.toString();
            }
            
            // Fallback to other evidence fields
            Object finding = agentResult.getEvidence().get("finding");
            if (finding != null) {
                return finding.toString();
            }
            
            Object summary = agentResult.getEvidence().get("summary");
            if (summary != null) {
                return summary.toString();
            }
        }
        
        // Default findings based on agent type and score
        String agentType = agentResult.getAgentType().toLowerCase();
        BigDecimal score = agentResult.getScore();
        
        if (agentType.contains("bot")) {
            return score.intValue() > 60 ? "High bot activity detected" : 
                   score.intValue() > 30 ? "Moderate bot presence" : "Low bot activity";
        } else if (agentType.contains("trend")) {
            return score.intValue() > 70 ? "Organic trend growth" :
                   score.intValue() > 40 ? "Accelerated pattern" : "Artificial spike";
        } else if (agentType.contains("aggregator")) {
            return score.intValue() > 66 ? "High authenticity" :
                   score.intValue() > 33 ? "Mixed signals" : "Low authenticity";
        } else {
            return "Analysis complete";
        }
    }

    /**
     * Create footer with hackathon and project information.
     */
    private Div createFooter() {
        Div footer = new Div();
        footer.addClassName("s1gnal-footer");
        
        Paragraph footerText = new Paragraph();
        footerText.add("Built with ❤️ for ");
        footerText.add(new Span("AGI Ventures Canada Hackathon 3.0"));
        footerText.add(" | September 6-7, 2025 | Powered by ");
        footerText.add(new Span("Solace PubSub+"));
        
        footer.add(footerText);
        return footer;
    }

    /**
     * Refresh dashboard data from repository.
     */
    private void refreshDashboardData() {
        System.out.println("DEBUG: Refreshing dashboard data...");
        
        // Load recent completed analyses
        List<Analysis> recentAnalyses = analysisRepository
            .findTop10ByStatusOrderByCreatedAtDesc(AnalysisStatus.COMPLETE);
        System.out.println("DEBUG: Found " + recentAnalyses.size() + " recent completed analyses");
        
        // Update recent analyses grid
        if (recentAnalysesGrid != null) {
            recentAnalysesGrid.setItems(recentAnalyses);
        }
        
        // Load current analysis data if available
        if (!recentAnalyses.isEmpty() && realityScoreGauge != null) {
            Analysis latestAnalysis = recentAnalyses.get(0);
            currentAnalysis = latestAnalysis;
            System.out.println("DEBUG: Updating Reality Score gauge with analysis: " + latestAnalysis.getQuery());
            
            // Update the gauge with latest analysis
            if (latestAnalysis.getRealityScore() != null) {
                realityScoreGauge.updateScore(
                    latestAnalysis.getRealityScore().intValue(),
                    latestAnalysis.getBotPercentage() != null ? latestAnalysis.getBotPercentage().intValue() : 0,
                    latestAnalysis.getManipulationLevel() != null ? latestAnalysis.getManipulationLevel() : ManipulationLevel.YELLOW
                );
            }
            
            // Load agent results for the current analysis
            loadAgentResultsForAnalysis(latestAnalysis.getId());
        }
    }

    /**
     * Load agent results for a specific analysis.
     */
    private void loadAgentResultsForAnalysis(UUID analysisId) {
        System.out.println("DEBUG: Loading agent results for analysis ID: " + analysisId);
        
        List<AgentResult> agentResults = agentResultRepository.findByAnalysisIdAndStatusOrderByCreatedAtAsc(
            analysisId, 
            io.signalzero.model.AnalysisStatus.COMPLETE
        );
        
        System.out.println("DEBUG: Found " + agentResults.size() + " agent results for analysis " + analysisId);
        
        // Log agent results for debugging
        for (AgentResult result : agentResults) {
            System.out.println("DEBUG: Agent result - Type: " + result.getAgentType() + 
                             ", Score: " + result.getScore() + ", Status: " + result.getStatus());
        }
        
        // Update agent results grid
        if (agentResultsGrid != null) {
            agentResultsGrid.setItems(agentResults);
            System.out.println("DEBUG: Updated agent results grid with " + agentResults.size() + " results");
        }
    }

    /**
     * Perform analysis on the entered query.
     */
    private void performAnalysis() {
        String query = queryField.getValue();
        if (query == null || query.trim().isEmpty()) {
            Notification.show("Please enter a query to analyze", 3000, Notification.Position.TOP_CENTER);
            return;
        }
        
        // Disable button and show processing state
        analyzeButton.setEnabled(false);
        analyzeButton.setText("Analyzing...");
        
        if (realityScoreGauge != null) {
            realityScoreGauge.setProcessing(true);
        }
        
        // Start async analysis
        analysisService.submitAnalysisAsync(query.trim()).whenComplete((analysis, throwable) -> {
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(() -> {
                    // Re-enable button
                    analyzeButton.setEnabled(true);
                    analyzeButton.setText("🚀 ANALYZE");
                    
                    if (throwable != null || analysis == null) {
                        if (realityScoreGauge != null) {
                            realityScoreGauge.updateScore(25, 0, ManipulationLevel.YELLOW); // Default fallback score
                        }
                        Notification notification = Notification.show(
                            "Analysis completed with limited data. Using fallback results.", 
                            5000, 
                            Notification.Position.TOP_CENTER
                        );
                        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                    } else {
                        // Update gauge with results
                        if (realityScoreGauge != null && analysis.getRealityScore() != null) {
                            realityScoreGauge.updateScore(
                                analysis.getRealityScore().intValue(),
                                analysis.getBotPercentage() != null ? analysis.getBotPercentage().intValue() : 0,
                                analysis.getManipulationLevel() != null ? analysis.getManipulationLevel() : ManipulationLevel.YELLOW
                            );
                        }
                        
                        currentAnalysis = analysis;
                        
                        // Load agent results for this analysis
                        loadAgentResultsForAnalysis(analysis.getId());
                        
                        // Refresh dashboard to show new analysis
                        refreshDashboardData();
                        
                        Notification notification = Notification.show(
                            "Analysis complete! Reality Score: " + analysis.getRealityScore() + "%", 
                            4000, 
                            Notification.Position.TOP_CENTER
                        );
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }
                    
                    // Clear the input field
                    queryField.clear();
                });
            }
        });
    }

    /**
     * Create a status indicator tag with specified text and manipulation level.
     * Matches AnalysisView style but without background colors (text only).
     */
    private Span createStatusTag(String text, ManipulationLevel level) {
        Span tag = new Span(text);
        tag.getStyle().set("display", "inline-flex");
        tag.getStyle().set("align-items", "center");
        tag.getStyle().set("padding", "4px 8px");
        tag.getStyle().set("border-radius", "999px");
        tag.getStyle().set("font-size", "0.75rem");
        tag.getStyle().set("font-weight", "500");
        tag.getStyle().set("border", "1px solid transparent");
        
        // Color coding based on manipulation level - text only (no background)
        String color;
        if (level == ManipulationLevel.GREEN) {
            color = "#10b981";
        } else if (level == ManipulationLevel.YELLOW) {
            color = "#f59e0b";
        } else {
            color = "#ef4444";
        }
        
        tag.getStyle().set("color", color);
        return tag;
    }

    /**
     * Capitalize first letter of a string.
     */
    private String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Set up real-time updates when view is attached.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        
        // Register for real-time analysis updates
        broadcasterRegistration = AnalysisUpdateBroadcaster.register(analysis -> {
            ui.access(() -> {
                // Flash animation for new data
                getElement().getClassList().add("s1gnal-update-flash");
                
                // Update current analysis if it matches
                if (currentAnalysis != null && currentAnalysis.getId().equals(analysis.getId())) {
                    currentAnalysis = analysis;
                    
                    // Update Reality Score gauge
                    if (realityScoreGauge != null && analysis.getRealityScore() != null) {
                        realityScoreGauge.updateScore(
                            analysis.getRealityScore().intValue(),
                            analysis.getBotPercentage() != null ? analysis.getBotPercentage().intValue() : 0,
                            analysis.getManipulationLevel() != null ? analysis.getManipulationLevel() : ManipulationLevel.YELLOW
                        );
                    }
                    
                    // Load updated agent results
                    loadAgentResultsForAnalysis(analysis.getId());
                }
                
                // Refresh dashboard data with new entity
                refreshDashboardData();
                
                // Remove flash animation
                ui.getPage().executeJs("setTimeout(() => { $0.classList.remove('s1gnal-update-flash'); }, 500)", getElement());
            });
        });
    }

    /**
     * Clean up real-time updates when view is detached.
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
        }
        super.onDetach(detachEvent);
    }
}
