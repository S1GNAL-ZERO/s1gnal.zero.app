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
import com.vaadin.flow.component.html.Span;
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
import io.signalzero.repository.AnalysisRepository;
import io.signalzero.service.AnalysisService;
import io.signalzero.ui.components.RealityScoreGauge;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    // UI Components
    private TextField queryField;
    private Button analyzeButton;
    private RealityScoreGauge realityScoreGauge;
    private Grid<Analysis> recentAnalysesGrid;
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
        setPadding(true);
        setSpacing(true);
        
        // Initialize the query field first
        queryField = new TextField();
        queryField.setPlaceholder("Enter product name, influencer handle, or trending topic...");
        queryField.addClassName("analysis-input");
        queryField.setWidthFull();
        queryField.setClearButtonVisible(true);
        queryField.setHelperText("Try: 'Stanley Cup', 'Prime Energy', '$BUZZ', or any viral product");
        
        createMainContent();
        
        // Load initial data from repository
        refreshDashboardData();
    }

    
    /**
     * Create main content area matching the mockup design.
     */
    private void createMainContent() {
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.addClassName("content");
        mainContent.setPadding(true);
        mainContent.setSpacing(true);
        mainContent.getStyle().set("display", "grid");
        mainContent.getStyle().set("gap", "20px");
        
        // KPI Cards Section
        HorizontalLayout kpiSection = createKPISection();
        
        // Reality Score Overview Panel
        Div realityScorePanel = createRealityScorePanel();
        
        // Latest Flags Panel
        Div latestFlagsPanel = createLatestFlagsPanel();
        
        // Position main content in column 2, row 2
        mainContent.getStyle().set("grid-column", "2");
        mainContent.getStyle().set("grid-row", "2");
        mainContent.getStyle().set("padding-top", "35px");
        
        mainContent.add(kpiSection, realityScorePanel, latestFlagsPanel);
        add(mainContent);
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
        body.addClassName("grid");
        body.addClassName("cols-2");
        body.setSpacing(true);
        body.getStyle().set("padding", "16px");
        
        // Score wrap section
        HorizontalLayout scoreWrap = new HorizontalLayout();
        scoreWrap.addClassName("score-wrap");
        scoreWrap.setAlignItems(Alignment.CENTER);
        scoreWrap.setSpacing(true);
        
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
        scoreValue.getStyle().set("font-size", "40px");
        scoreValue.getStyle().set("font-weight", "900");
        scoreValue.getStyle().set("line-height", "1");
        
        Span scoreCaption = new Span("Mostly manufactured hype");
        scoreCaption.addClassName("muted");
        scoreCaption.getStyle().set("color", "var(--muted)");
        
        // Tags for findings
        HorizontalLayout findingsTags = new HorizontalLayout();
        findingsTags.setSpacing(true);
        findingsTags.getStyle().set("margin-top", "10px");
        
        Span botTag = new Span("Bot surge");
        botTag.addClassName("tag");
        Span promoTag = new Span("Paid promos");
        promoTag.addClassName("tag");
        Span clusterTag = new Span("Review clusters");
        clusterTag.addClassName("tag");
        
        findingsTags.add(botTag, promoTag, clusterTag);
        
        scoreDetails.add(scoreLabel, scoreValue, scoreCaption, findingsTags);
        scoreWrap.add(realityScoreGauge, scoreDetails);
        
        // Chart placeholder
        Div chartPlaceholder = new Div();
        chartPlaceholder.addClassName("chart");
        chartPlaceholder.getStyle().set("height", "220px");
        chartPlaceholder.getStyle().set("border-radius", "14px");
        chartPlaceholder.getStyle().set("border", "1px dashed #2b376f");
        chartPlaceholder.getStyle().set("display", "grid");
        chartPlaceholder.getStyle().set("place-items", "center");
        chartPlaceholder.getStyle().set("color", "#8aa0ff");
        chartPlaceholder.setText("(Trend chart placeholder)");
        
        body.add(scoreWrap, chartPlaceholder);
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
        
        // Panel header
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("panel-header");
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.getStyle().set("padding", "16px");
        header.getStyle().set("border-bottom", "1px solid #1b2452");
        
        Span title = new Span("Latest Flags");
        title.addClassName("panel-title");
        title.getStyle().set("font-weight", "700");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.addClassName("btn");
        refreshButton.addClassName("secondary");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> refreshDashboardData());
        
        header.add(title, refreshButton);
        
        // Panel body with table (using existing grid)
        Div body = new Div();
        body.addClassName("panel-body");
        body.getStyle().set("padding", "16px");
        
        createRecentAnalysesGrid();
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
        Div gridSection = new Div();
        gridSection.addClassName("s1gnal-card");
        gridSection.getStyle().set("margin", "2rem 0");
        gridSection.getStyle().set("padding", "2rem");
        
        H2 gridTitle = new H2("📈 Recent Analyses");
        gridTitle.getStyle().set("margin-top", "0");
        gridTitle.getStyle().set("color", "var(--s1gnal-primary)");
        
        // Create grid with direct entity binding
        recentAnalysesGrid = new Grid<>(Analysis.class, false);
        recentAnalysesGrid.addClassName("s1gnal-grid");
        recentAnalysesGrid.setHeight("400px");
        
        // Configure columns to display entity data directly
        recentAnalysesGrid.addColumn(Analysis::getQuery)
            .setHeader("Query")
            .setFlexGrow(2);
            
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            Span botSpan = new Span(analysis.getBotPercentage() + "%");
            botSpan.getStyle().set("color", "var(--s1gnal-error)");
            botSpan.getStyle().set("font-weight", "bold");
            return botSpan;
        })).setHeader("Bot %").setFlexGrow(1);
        
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            Span scoreSpan = new Span(analysis.getRealityScore() + "%");
            ManipulationLevel level = analysis.getManipulationLevel();
            String color = level == ManipulationLevel.GREEN ? "var(--s1gnal-success)" :
                          level == ManipulationLevel.YELLOW ? "var(--s1gnal-warning)" : "var(--s1gnal-error)";
            scoreSpan.getStyle().set("color", color);
            scoreSpan.getStyle().set("font-weight", "bold");
            return scoreSpan;
        })).setHeader("Reality Score").setFlexGrow(1);
        
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            Span badge = new Span(analysis.getManipulationLevel().getDescription());
            badge.addClassName("manipulation-badge");
            badge.addClassName("manipulation-" + analysis.getManipulationLevel().name().toLowerCase());
            return badge;
        })).setHeader("Classification").setFlexGrow(1);
        
        recentAnalysesGrid.addColumn(new ComponentRenderer<>(analysis -> {
            Icon statusIcon;
            String color;
            switch (analysis.getStatus()) {
                case PENDING:
                    statusIcon = new Icon(VaadinIcon.CLOCK);
                    color = "var(--s1gnal-warning)";
                    break;
                case PROCESSING:
                    statusIcon = new Icon(VaadinIcon.REFRESH);
                    color = "var(--s1gnal-primary)";
                    statusIcon.addClassName("s1gnal-loading");
                    break;
                case COMPLETE:
                    statusIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
                    color = "var(--s1gnal-success)";
                    break;
                default:
                    statusIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
                    color = "var(--s1gnal-error)";
            }
            statusIcon.getStyle().set("color", color);
            return statusIcon;
        })).setHeader("Status").setFlexGrow(0).setWidth("80px");
        
        recentAnalysesGrid.addColumn(analysis -> 
            analysis.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
        ).setHeader("Time").setFlexGrow(1);
        
        gridSection.add(gridTitle, recentAnalysesGrid);
        add(gridSection);
    }

    /**
     * Create footer with hackathon and project information.
     */
    private void createFooter() {
        Div footer = new Div();
        footer.addClassName("s1gnal-footer");
        
        Paragraph footerText = new Paragraph();
        footerText.add("Built with ❤️ for ");
        footerText.add(new Span("AGI Ventures Canada Hackathon 3.0"));
        footerText.add(" | September 6-7, 2025 | Powered by ");
        footerText.add(new Span("Solace PubSub+"));
        
        footer.add(footerText);
        add(footer);
    }

    /**
     * Perform analysis by calling service layer.
     * Repository pattern: Service works with entities, returns entity results.
     */
    private void performAnalysis() {
        String query = queryField.getValue().trim();
        
        if (query.isEmpty()) {
            showNotification("Please enter a product, trend, or influencer to analyze", NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Disable form during processing
        setFormEnabled(false);
        realityScoreGauge.setProcessing(true);
        
        // Show processing notification
        showNotification("🔍 Analyzing '" + query + "' with AI agents...", NotificationVariant.LUMO_PRIMARY);
        
        try {
            // Call analysis service - returns Analysis entity
            CompletableFuture<Analysis> analysisResult = analysisService.submitAnalysisAsync(query);
            
            analysisResult.thenAccept(analysis -> {
                // Update UI with entity data - runs on UI thread via access()
                UI.getCurrent().access(() -> {
                    currentAnalysis = analysis;
                    updateUIWithAnalysisResult(analysis);
                    refreshDashboardData();
                    setFormEnabled(true);
                    
                    // Show completion notification with entity data
                    String message = String.format("✅ Analysis complete! %s has %s%% Reality Score", 
                        analysis.getQuery(), analysis.getRealityScore());
                    showNotification(message, NotificationVariant.LUMO_SUCCESS);
                });
            }).exceptionally(throwable -> {
                // Handle errors - runs on UI thread
                UI.getCurrent().access(() -> {
                    setFormEnabled(true);
                    realityScoreGauge.setProcessing(false);
                    showNotification("❌ Analysis failed: " + throwable.getMessage(), NotificationVariant.LUMO_ERROR);
                });
                return null;
            });
            
        } catch (Exception e) {
            setFormEnabled(true);
            realityScoreGauge.setProcessing(false);
            showNotification("❌ Error starting analysis: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Update UI components with analysis results from entity.
     */
    private void updateUIWithAnalysisResult(Analysis analysis) {
        if (analysis != null && analysis.getStatus() == AnalysisStatus.COMPLETE) {
            // Update gauge with entity data
            realityScoreGauge.updateScore(
                analysis.getRealityScore().intValue(),
                analysis.getBotPercentage().intValue(),
                analysis.getManipulationLevel()
            );
        }
    }

    /**
     * Refresh dashboard statistics using repository queries.
     * Repository pattern: Direct queries to get aggregate data.
     */
    private void refreshDashboardData() {
        try {
            // For now, use empty lists since we don't have data yet
            // This will be populated once analyses are created
            recentAnalysesGrid.setItems(java.util.Collections.emptyList());
            
        } catch (Exception e) {
            // Gracefully handle any errors
            System.err.println("Error refreshing dashboard data: " + e.getMessage());
        }
    }

    /**
     * Enable/disable form components during processing.
     */
    private void setFormEnabled(boolean enabled) {
        queryField.setEnabled(enabled);
        analyzeButton.setEnabled(enabled);
        
        if (enabled) {
            analyzeButton.setText("🚀 ANALYZE");
            realityScoreGauge.setProcessing(false);
        } else {
            analyzeButton.setText("🔄 PROCESSING...");
        }
    }

    /**
     * Show notification to user.
     */
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification();
        notification.addClassName("s1gnal-notification");
        
        if (variant == NotificationVariant.LUMO_SUCCESS) {
            notification.addClassName("success");
        } else if (variant == NotificationVariant.LUMO_ERROR) {
            notification.addClassName("error");
        }
        
        notification.setText(message);
        notification.setDuration(5000);
        notification.setPosition(Notification.Position.TOP_END);
        notification.open();
    }

    /**
     * Set up real-time updates when view is attached.
     * CRITICAL: Required for @Push functionality (CLAUDE.md requirement).
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Register for real-time analysis updates
        UI ui = attachEvent.getUI();
        broadcasterRegistration = AnalysisUpdateBroadcaster.register(analysis -> {
            ui.access(() -> {
                // Flash update animation
                addClassName("s1gnal-update-flash");
                
                // Update current analysis if it matches
                if (currentAnalysis != null && 
                    currentAnalysis.getId().equals(analysis.getId())) {
                    updateUIWithAnalysisResult(analysis);
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
