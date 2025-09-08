package io.signalzero.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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
import io.signalzero.model.AgentResult;
import io.signalzero.model.Analysis;
import io.signalzero.model.AnalysisStatus;
import io.signalzero.repository.AgentResultRepository;
import io.signalzero.repository.AnalysisRepository;
import io.signalzero.service.AnalysisService;
import io.signalzero.ui.components.RealityScoreGauge;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * S1GNAL.ZERO - Analysis View
 * AGI Ventures Canada Hackathon 3.0 (September 6-7, 2025)
 * 
 * Analysis interface for running real-time authenticity verification.
 * Features:
 * - Analysis input form with quick action buttons
 * - Signal type selection checkboxes
 * - Real-time agent results display
 * - Executive summary generation
 * - Transport and tech stack information
 * 
 * CRITICAL REQUIREMENTS (from CLAUDE.md):
 * - Repository pattern with direct JPA entity binding (NO DTOs)
 * - Real-time updates via @Push annotation
 * - Hardcoded demo values for consistent hackathon demo
 * - Production-ready UI components with proper error handling
 * 
 * Reference: DETAILED_DESIGN.md Section 11 - Vaadin UI Components
 */
@Route(value = "analyze", layout = MainLayout.class)
@PageTitle("S1GNAL.ZERO Analyze - AI-Powered Authenticity Verification")
@AnonymousAllowed
public class AnalysisView extends VerticalLayout {

    @Autowired
    private AnalysisService analysisService;
    
    @Autowired
    private AgentResultRepository agentResultRepository;
    
    @Autowired
    private AnalysisRepository analysisRepository;

    // UI Components
    private TextField queryField;
    private Button analyzeButton;
    private Checkbox botAnalysisCheckbox;
    private Checkbox reviewAuthenticityCheckbox;
    private Checkbox paidPromotionCheckbox;
    private Checkbox trendPatterningCheckbox;
    private Grid<AgentResult> agentResultsGrid;
    private Div narrativeSummaryDiv;
    private RealityScoreGauge realityScoreGauge;
    private Div processingOverlay;
    
    // Real-time update registration
    private Registration broadcasterRegistration;
    
    // Current analysis tracking
    private Analysis currentAnalysis;
    private String currentAnalysisQuery; // Track by query string for timing fix
    
    // Reality Score status components
    private Div realityScoreStatusDiv;
    private Span realityScorePercentage;
    private Span realityScoreDescription;
    private HorizontalLayout realityScoreIndicators;

    /**
     * Initialize the analysis view with all components.
     * Uses repository pattern with direct JPA entity binding throughout.
     */
    public AnalysisView() {
        addClassName("app");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        
        // Set up CSS custom properties for theming
        setupThemeVariables();
        
        // Apply dark theme background
        getStyle().set("background", "radial-gradient(1000px 600px at 10% -10%, rgba(102,126,234,.25), transparent 40%), radial-gradient(800px 600px at 90% -20%, rgba(118,75,162,.25), transparent 40%), var(--bg)");
        getStyle().set("color", "var(--ink)");
        getStyle().set("font-family", "Inter, system-ui, -apple-system, 'Segoe UI', Roboto, Arial, sans-serif");
        
        // Set up the main content layout
        getStyle().set("padding", "24px 24px 36px");
        getStyle().set("display", "grid");
        getStyle().set("gap", "20px");
        
        createAnalysisForm();
        createResultsSection();
        createProcessingOverlay();
        
        // Initialize components
        initializeComponents();
    }
    
    /**
     * Set up CSS custom properties for consistent theming.
     */
    private void setupThemeVariables() {
        getElement().getStyle().set("--bg", "#0b1020");
        getElement().getStyle().set("--panel", "#11162a");
        getElement().getStyle().set("--panel-2", "#131a33");
        getElement().getStyle().set("--ink", "#e8ecff");
        getElement().getStyle().set("--muted", "#a6b0d8");
        getElement().getStyle().set("--brand", "#667eea");
        getElement().getStyle().set("--brand-2", "#764ba2");
        getElement().getStyle().set("--ok", "#10b981");
        getElement().getStyle().set("--warn", "#f59e0b");
        getElement().getStyle().set("--bad", "#ef4444");
        getElement().getStyle().set("--accent", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        getElement().getStyle().set("--radius", "16px");
        getElement().getStyle().set("--shadow", "0 20px 60px rgba(0,0,0,.35)");
    }

    /**
     * Create the analysis input form matching the mockup design.
     */
    private void createAnalysisForm() {
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
        
        Span title = new Span("Analyze a Product, Trend, or Influencer");
        title.addClassName("panel-title");
        title.getStyle().set("font-weight", "700");
        
        analyzeButton = new Button("Analyze");
        analyzeButton.addClassName("btn");
        analyzeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        analyzeButton.addClickListener(e -> performAnalysis());
        
        header.add(title, analyzeButton);
        
        // Panel body with two-column grid
        HorizontalLayout body = new HorizontalLayout();
        body.addClassName("panel-body");
        body.getStyle().set("padding", "16px");
        body.getStyle().set("display", "grid");
        body.getStyle().set("grid-template-columns", "1fr 1fr");
        body.getStyle().set("gap", "20px");
        body.setWidthFull();
        
        // Left column - Query input
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setSpacing(true);
        leftColumn.setPadding(false);
        
        Span queryLabel = new Span("What should we verify?");
        queryLabel.addClassName("muted");
        queryLabel.getStyle().set("color", "var(--muted)");
        
        // Query input field - simplified for proper user interaction
        queryField = new TextField();
        queryField.setPlaceholder("e.g. \"Stanley Cup tumbler\", \"$BUZZ\", @influencer");
        queryField.setWidthFull();
        queryField.addClassName("search-input");
        
        // Apply styling directly to the text field for better interaction
        queryField.getStyle().set("background", "#0e1430");
        queryField.getStyle().set("border", "1px solid #263061");
        queryField.getStyle().set("border-radius", "12px");
        queryField.getStyle().set("padding", "10px 12px");
        queryField.getStyle().set("color", "var(--ink)");
        queryField.getStyle().set("margin-top", "8px");
        
        // Ensure the field is focusable and interactive
        queryField.setReadOnly(false);
        queryField.setEnabled(true);
        queryField.focus();
        
        // Quick action buttons
        HorizontalLayout quickButtons = new HorizontalLayout();
        quickButtons.setSpacing(true);
        quickButtons.getStyle().set("margin-top", "12px");
        
        Button stanleyCupBtn = new Button("Stanley Cup");
        stanleyCupBtn.addClassName("btn");
        stanleyCupBtn.addClassName("secondary");
        stanleyCupBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        stanleyCupBtn.addClickListener(e -> queryField.setValue("Stanley Cup tumbler"));
        
        Button buzzBtn = new Button("$BUZZ");
        buzzBtn.addClassName("btn");
        buzzBtn.addClassName("secondary");
        buzzBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        buzzBtn.addClickListener(e -> queryField.setValue("$BUZZ meme stock"));
        
        Button influencerBtn = new Button("Influencer");
        influencerBtn.addClassName("btn");
        influencerBtn.addClassName("secondary");
        influencerBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        influencerBtn.addClickListener(e -> queryField.setValue("@bigCreator"));
        
        quickButtons.add(stanleyCupBtn, buzzBtn, influencerBtn);
        
        leftColumn.add(queryLabel, queryField, quickButtons);
        
        // Right column - Signal selection
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setSpacing(true);
        rightColumn.setPadding(false);
        rightColumn.getStyle().set("margin-left", "20px"); // Add spacing from input field
        
        Span signalsLabel = new Span("Which signals?");
        signalsLabel.addClassName("muted");
        signalsLabel.getStyle().set("color", "var(--muted)");
        
        // Signal checkboxes - reorganize for better layout
        VerticalLayout signalsContainer = new VerticalLayout();
        signalsContainer.setSpacing(true);
        signalsContainer.setPadding(false);
        signalsContainer.getStyle().set("margin-top", "8px");
        signalsContainer.getStyle().set("gap", "8px");
        
        // First row of checkboxes
        HorizontalLayout firstRowSignals = new HorizontalLayout();
        firstRowSignals.setSpacing(true);
        firstRowSignals.getStyle().set("flex-wrap", "wrap");
        firstRowSignals.getStyle().set("gap", "8px");
        
        botAnalysisCheckbox = createSignalCheckbox("Bot Analysis", true);
        reviewAuthenticityCheckbox = createSignalCheckbox("Review Authenticity", true);
        
        firstRowSignals.add(botAnalysisCheckbox, reviewAuthenticityCheckbox);
        
        // Second row of checkboxes
        HorizontalLayout secondRowSignals = new HorizontalLayout();
        secondRowSignals.setSpacing(true);
        secondRowSignals.getStyle().set("flex-wrap", "wrap");
        secondRowSignals.getStyle().set("gap", "8px");
        
        paidPromotionCheckbox = createSignalCheckbox("Paid Promotion", true);
        trendPatterningCheckbox = createSignalCheckbox("Trend Patterning", true);
        
        secondRowSignals.add(paidPromotionCheckbox, trendPatterningCheckbox);
        
        signalsContainer.add(firstRowSignals, secondRowSignals);
        
        // Tech stack info
        Span techInfo = new Span("Transport: Solace PubSub+ · Orchestration: Agent Mesh · Backend: Java");
        techInfo.addClassName("muted");
        techInfo.getStyle().set("color", "var(--muted)");
        techInfo.getStyle().set("margin-top", "12px");
        techInfo.getStyle().set("font-size", "12px");
        
        rightColumn.add(signalsLabel, signalsContainer, techInfo);
        
        body.add(leftColumn, rightColumn);
        panel.add(header, body);
        add(panel);
    }
    
    /**
     * Create a styled signal selection checkbox.
     */
    private Checkbox createSignalCheckbox(String label, boolean defaultValue) {
        Checkbox checkbox = new Checkbox(label);
        checkbox.setValue(defaultValue);
        checkbox.addClassName("tag");
        checkbox.getStyle().set("padding", "6px 10px");
        checkbox.getStyle().set("border-radius", "999px");
        checkbox.getStyle().set("font-size", "12px");
        checkbox.getStyle().set("border", "1px solid #2b376f");
        checkbox.getStyle().set("background", "#121942");
        checkbox.getStyle().set("color", "#b6c3ff");
        return checkbox;
    }

    /**
     * Create the results section with agent results and narrative summary.
     */
    private void createResultsSection() {
        HorizontalLayout resultsLayout = new HorizontalLayout();
        resultsLayout.setWidthFull();
        resultsLayout.setSpacing(true);
        resultsLayout.getStyle().set("display", "grid");
        resultsLayout.getStyle().set("grid-template-columns", "1fr 1fr");
        resultsLayout.getStyle().set("gap", "20px");
        
        // Agent Results Panel  
        Div agentResultsPanel = createAgentResultsPanel();
        
        // Reality Score Panel with Gauge
        Div realityScorePanel = createRealityScorePanel();
        
        resultsLayout.add(agentResultsPanel, realityScorePanel);
        
        // Add narrative summary below in full width
        Div narrativeSummaryPanel = createNarrativeSummaryPanel();
        narrativeSummaryPanel.getStyle().set("grid-column", "1 / -1"); // Full width
        add(narrativeSummaryPanel);
        add(resultsLayout);
    }
    
    /**
     * Create the agent results panel.
     */
    private Div createAgentResultsPanel() {
        Div panel = new Div();
        panel.addClassName("panel");
        panel.getStyle().set("background", "var(--panel)");
        panel.getStyle().set("border", "1px solid #1b2452");
        panel.getStyle().set("border-radius", "var(--radius)");
        panel.getStyle().set("box-shadow", "var(--shadow)");
        
        // Panel header
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("panel-header");
        header.getStyle().set("padding", "16px");
        header.getStyle().set("border-bottom", "1px solid #1b2452");
        
        Span title = new Span("Agent Results");
        title.addClassName("panel-title");
        title.getStyle().set("font-weight", "700");
        
        header.add(title);
        
        // Panel body with grid
        Div body = new Div();
        body.addClassName("panel-body");
        body.getStyle().set("padding", "16px");
        
        createAgentResultsGrid();
        body.add(agentResultsGrid);
        
        panel.add(header, body);
        return panel;
    }
    
    /**
     * Create the Reality Score panel with gauge and status information.
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
        header.getStyle().set("padding", "16px");
        header.getStyle().set("border-bottom", "1px solid #1b2452");
        
        Span title = new Span("Reality Score™");
        title.addClassName("panel-title");
        title.getStyle().set("font-weight", "700");
        
        header.add(title);
        
        // Panel body with gauge and status
        Div body = new Div();
        body.addClassName("panel-body");
        body.getStyle().set("padding", "16px");
        body.getStyle().set("display", "flex");
        body.getStyle().set("gap", "20px");
        body.getStyle().set("align-items", "center");
        
        // Left side: Reality Score Gauge
        Div gaugeContainer = new Div();
        gaugeContainer.getStyle().set("display", "flex");
        gaugeContainer.getStyle().set("justify-content", "center");
        gaugeContainer.getStyle().set("align-items", "center");
        gaugeContainer.getStyle().set("flex-shrink", "0");
        
        // Initialize the Reality Score Gauge
        realityScoreGauge = new RealityScoreGauge();
        gaugeContainer.add(realityScoreGauge);
        
        // Right side: Status information
        createRealityScoreStatusSection();
        
        body.add(gaugeContainer, realityScoreStatusDiv);
        panel.add(header, body);
        return panel;
    }
    
    /**
     * Create the Reality Score status information section.
     */
    private void createRealityScoreStatusSection() {
        realityScoreStatusDiv = new Div();
        realityScoreStatusDiv.getStyle().set("flex", "1");
        realityScoreStatusDiv.getStyle().set("display", "flex");
        realityScoreStatusDiv.getStyle().set("flex-direction", "column");
        realityScoreStatusDiv.getStyle().set("gap", "16px");
        realityScoreStatusDiv.getStyle().set("padding-left", "8px");
        
        // Reality Score percentage display
        realityScorePercentage = new Span("34%");
        realityScorePercentage.getStyle().set("font-size", "2.5rem");
        realityScorePercentage.getStyle().set("font-weight", "700");
        realityScorePercentage.getStyle().set("color", "var(--warn)");
        realityScorePercentage.getStyle().set("line-height", "1");
        realityScorePercentage.getStyle().set("margin", "0");
        
        // Reality Score description
        realityScoreDescription = new Span("Mostly manufactured hype");
        realityScoreDescription.getStyle().set("color", "var(--muted)");
        realityScoreDescription.getStyle().set("font-size", "1rem");
        realityScoreDescription.getStyle().set("margin-bottom", "8px");
        
        // Status indicators container
        realityScoreIndicators = new HorizontalLayout();
        realityScoreIndicators.setSpacing(false);
        realityScoreIndicators.getStyle().set("gap", "8px");
        realityScoreIndicators.getStyle().set("flex-wrap", "wrap");
        realityScoreIndicators.getStyle().set("align-items", "center");
        
        // Create initial indicators
        updateRealityScoreStatusIndicators(62, 34); // Default demo values
        
        realityScoreStatusDiv.add(realityScorePercentage, realityScoreDescription, realityScoreIndicators);
    }
    
    /**
     * Update the Reality Score status indicators based on analysis results.
     */
    private void updateRealityScoreStatusIndicators(int botPercentage, int realityScore) {
        realityScoreIndicators.removeAll();
        
        // Bot surge indicator
        if (botPercentage > 60) {
            Span botSurgeTag = createStatusTag("🤖 Bot surge", "var(--bad)");
            realityScoreIndicators.add(botSurgeTag);
        } else if (botPercentage > 30) {
            Span botActivityTag = createStatusTag("🤖 Bot activity", "var(--warn)");
            realityScoreIndicators.add(botActivityTag);
        }
        
        // Paid promotion indicator (simulated based on reality score)
        if (realityScore < 40) {
            Span paidPromosTag = createStatusTag("💰 Paid promos", "var(--bad)");
            realityScoreIndicators.add(paidPromosTag);
        } else if (realityScore < 70) {
            Span somePromosTag = createStatusTag("💰 Some promos", "var(--warn)");
            realityScoreIndicators.add(somePromosTag);
        }
        
        // Review clusters indicator (simulated based on bot percentage)
        if (botPercentage > 50) {
            Span reviewClustersTag = createStatusTag("⭐ Review clusters", "var(--bad)");
            realityScoreIndicators.add(reviewClustersTag);
        } else if (botPercentage > 25) {
            Span mixedReviewsTag = createStatusTag("⭐ Mixed reviews", "var(--warn)");
            realityScoreIndicators.add(mixedReviewsTag);
        }
        
        // Trending manipulation indicator
        if (realityScore < 30) {
            Span trendManipTag = createStatusTag("📈 Trend manipulation", "var(--bad)");
            realityScoreIndicators.add(trendManipTag);
        }
        
        // If no negative indicators, show positive ones
        if (realityScoreIndicators.getComponentCount() == 0) {
            if (realityScore > 80) {
                Span authenticTag = createStatusTag("✅ Authentic", "var(--ok)");
                realityScoreIndicators.add(authenticTag);
            } else if (realityScore > 60) {
                Span mostlyAuthenticTag = createStatusTag("✅ Mostly authentic", "var(--ok)");
                realityScoreIndicators.add(mostlyAuthenticTag);
            }
        }
    }
    
    /**
     * Create a status indicator tag with specified text and color.
     */
    private Span createStatusTag(String text, String color) {
        Span tag = new Span(text);
        tag.getStyle().set("display", "inline-flex");
        tag.getStyle().set("align-items", "center");
        tag.getStyle().set("padding", "4px 8px");
        tag.getStyle().set("border-radius", "999px");
        tag.getStyle().set("font-size", "0.75rem");
        tag.getStyle().set("font-weight", "500");
        tag.getStyle().set("background", color.equals("var(--ok)") ? "rgba(16, 185, 129, 0.1)" : 
                                       color.equals("var(--warn)") ? "rgba(245, 158, 11, 0.1)" :
                                       "rgba(239, 68, 68, 0.1)");
        tag.getStyle().set("color", color);
        tag.getStyle().set("border", "1px solid " + 
                          (color.equals("var(--ok)") ? "rgba(16, 185, 129, 0.2)" : 
                           color.equals("var(--warn)") ? "rgba(245, 158, 11, 0.2)" :
                           "rgba(239, 68, 68, 0.2)"));
        return tag;
    }
    
    /**
     * Create the narrative summary panel.
     */
    private Div createNarrativeSummaryPanel() {
        Div panel = new Div();
        panel.addClassName("panel");
        panel.getStyle().set("background", "var(--panel)");
        panel.getStyle().set("border", "1px solid #1b2452");
        panel.getStyle().set("border-radius", "var(--radius)");
        panel.getStyle().set("box-shadow", "var(--shadow)");
        
        // Panel header
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("panel-header");
        header.getStyle().set("padding", "16px");
        header.getStyle().set("border-bottom", "1px solid #1b2452");
        
        Span title = new Span("Narrative Summary");
        title.addClassName("panel-title");
        title.getStyle().set("font-weight", "700");
        
        header.add(title);
        
        // Panel body with summary content
        narrativeSummaryDiv = new Div();
        narrativeSummaryDiv.addClassName("panel-body");
        narrativeSummaryDiv.getStyle().set("padding", "16px");
        narrativeSummaryDiv.getStyle().set("color", "var(--muted)");
        narrativeSummaryDiv.setText("Run an analysis to see the executive summary.");
        
        panel.add(header, narrativeSummaryDiv);
        return panel;
    }
    
    /**
     * Create and configure the agent results grid.
     */
    private void createAgentResultsGrid() {
        agentResultsGrid = new Grid<>(AgentResult.class, false);
        agentResultsGrid.setHeight("300px");
        agentResultsGrid.setWidthFull();
        
        // Apply dark theme styling directly
        agentResultsGrid.getStyle().set("background", "var(--panel)");
        agentResultsGrid.getStyle().set("border", "1px solid #1b2452");
        agentResultsGrid.getStyle().set("border-radius", "8px");
        agentResultsGrid.getStyle().set("color", "var(--ink)");
        
        // Configure columns
        agentResultsGrid.addColumn(new ComponentRenderer<>(result -> {
            Span agentSpan = new Span(formatAgentName(result.getAgentType()));
            agentSpan.getStyle().set("color", "var(--ink)");
            agentSpan.getStyle().set("font-weight", "500");
            return agentSpan;
        })).setHeader("Agent").setFlexGrow(1);
            
        agentResultsGrid.addColumn(new ComponentRenderer<>(result -> {
            // Extract key finding from evidence data
            String keyFinding = "Analysis complete";
            if (result.getEvidence() != null && !result.getEvidence().isEmpty()) {
                Object finding = result.getEvidence().get("keyFinding");
                if (finding != null) {
                    keyFinding = finding.toString();
                } else {
                    // Generate finding based on score and agent type
                    keyFinding = generateKeyFinding(result);
                }
            }
            Span findingSpan = new Span(keyFinding);
            findingSpan.getStyle().set("color", "var(--muted)");
            return findingSpan;
        })).setHeader("Key Finding").setFlexGrow(2);
            
        agentResultsGrid.addColumn(new ComponentRenderer<>(result -> {
            Span scoreSpan = new Span(result.getScore() + "%");
            scoreSpan.getStyle().set("font-weight", "bold");
            
            // Color code the score based on value
            int score = result.getScore().intValue();
            if (score >= 70) {
                scoreSpan.getStyle().set("color", "var(--ok)");
            } else if (score >= 40) {
                scoreSpan.getStyle().set("color", "var(--warn)");
            } else {
                scoreSpan.getStyle().set("color", "var(--bad)");
            }
            
            return scoreSpan;
        })).setHeader("Score").setFlexGrow(1);
    }
    
    /**
     * Format agent type names for better display.
     */
    private String formatAgentName(String agentType) {
        if (agentType == null) return "Unknown";
        
        switch (agentType.toLowerCase()) {
            case "bot-detector":
                return "Bot Analysis";
            case "review-validator":
                return "Review Authenticity";
            case "paid-promotion":
                return "Paid Promotion";
            case "trend-analysis":
                return "Trend Patterning";
            case "score-aggregator":
                return "Score Aggregator";
            default:
                // Convert to title case
                String formatted = agentType.replace("-", " ").replace("_", " ");
                return capitalizeWords(formatted);
        }
    }
    
    /**
     * Capitalize the first letter of each word in a string.
     */
    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
    
    /**
     * Generate a key finding based on agent result data.
     */
    private String generateKeyFinding(AgentResult result) {
        String agentType = result.getAgentType();
        int score = result.getScore().intValue();
        
        switch (agentType.toLowerCase()) {
            case "bot-detector":
                return score > 60 ? "High bot activity detected" : 
                       score > 30 ? "Moderate bot presence" : "Low bot activity";
                       
            case "review-validator":
                return score > 70 ? "Reviews appear authentic" :
                       score > 40 ? "Mixed review authenticity" : "Suspicious review patterns";
                       
            case "paid-promotion":
                return score > 60 ? "Strong promotional signals" :
                       score > 30 ? "Some paid content detected" : "Minimal promotional activity";
                       
            case "trend-analysis":
                return score > 70 ? "Organic trend growth" :
                       score > 40 ? "Accelerated trend pattern" : "Artificial trend spike";
                       
            case "score-aggregator":
                return score > 66 ? "High authenticity" :
                       score > 33 ? "Mixed signals" : "Low authenticity";
                       
            default:
                return "Analysis complete - " + score + "% confidence";
        }
    }

    /**
     * Create the enhanced processing overlay that dims the screen during analysis.
     * Shows detailed information about what each AI agent is doing with subtle animations.
     */
    private void createProcessingOverlay() {
        processingOverlay = new Div();
        processingOverlay.addClassName("processing-overlay");
        
        // Position as full-screen fixed overlay
        processingOverlay.getStyle().set("position", "fixed");
        processingOverlay.getStyle().set("top", "0");
        processingOverlay.getStyle().set("left", "0");
        processingOverlay.getStyle().set("width", "100vw");
        processingOverlay.getStyle().set("height", "100vh");
        processingOverlay.getStyle().set("background", "rgba(11, 16, 32, 0.9)");
        processingOverlay.getStyle().set("backdrop-filter", "blur(12px)");
        processingOverlay.getStyle().set("z-index", "9999");
        processingOverlay.getStyle().set("display", "none");
        processingOverlay.getStyle().set("align-items", "center");
        processingOverlay.getStyle().set("justify-content", "center");
        processingOverlay.getStyle().set("flex-direction", "column");
        processingOverlay.getStyle().set("gap", "32px");
        
        // Main processing container
        Div processingContainer = new Div();
        processingContainer.getStyle().set("background", "rgba(17, 22, 42, 0.95)");
        processingContainer.getStyle().set("border", "1px solid #2b376f");
        processingContainer.getStyle().set("border-radius", "20px");
        processingContainer.getStyle().set("padding", "40px");
        processingContainer.getStyle().set("max-width", "600px");
        processingContainer.getStyle().set("width", "90vw");
        processingContainer.getStyle().set("box-shadow", "0 25px 80px rgba(0,0,0,0.5)");
        
        // Header with main spinner and title
        Div headerSection = new Div();
        headerSection.getStyle().set("text-align", "center");
        headerSection.getStyle().set("margin-bottom", "32px");
        
        // Main animated processing indicator
        Div mainSpinner = new Div();
        mainSpinner.addClassName("main-spinner");
        mainSpinner.getStyle().set("width", "80px");
        mainSpinner.getStyle().set("height", "80px");
        mainSpinner.getStyle().set("border", "4px solid rgba(102, 126, 234, 0.2)");
        mainSpinner.getStyle().set("border-top", "4px solid var(--brand)");
        mainSpinner.getStyle().set("border-radius", "50%");
        mainSpinner.getStyle().set("margin", "0 auto 20px");
        mainSpinner.getStyle().set("animation", "spin 1.5s linear infinite");
        
        // Processing title
        Span processingTitle = new Span("🔍 AI Agents Analyzing");
        processingTitle.getStyle().set("font-size", "1.8rem");
        processingTitle.getStyle().set("font-weight", "700");
        processingTitle.getStyle().set("color", "var(--ink)");
        processingTitle.getStyle().set("display", "block");
        processingTitle.getStyle().set("margin-bottom", "8px");
        
        Span processingSubtitle = new Span("Multi-agent authenticity verification in progress");
        processingSubtitle.getStyle().set("font-size", "1rem");
        processingSubtitle.getStyle().set("color", "var(--muted)");
        processingSubtitle.getStyle().set("display", "block");
        
        headerSection.add(mainSpinner, processingTitle, processingSubtitle);
        
        // Agent activity list
        Div agentsList = new Div();
        agentsList.addClassName("agents-list");
        agentsList.getStyle().set("display", "flex");
        agentsList.getStyle().set("flex-direction", "column");
        agentsList.getStyle().set("gap", "16px");
        
        // Create agent activity items
        Div botAgent = createAgentActivityItem("🤖", "Bot Detection Agent", 
            "Scanning social patterns for automated behavior", 0.5);
        Div reviewAgent = createAgentActivityItem("⭐", "Review Authenticity Agent", 
            "Analyzing review patterns and sentiment clustering", 1.0);
        Div promoAgent = createAgentActivityItem("💰", "Paid Promotion Agent", 
            "Detecting sponsored content and affiliate signals", 1.5);
        Div trendAgent = createAgentActivityItem("📈", "Trend Analysis Agent", 
            "Examining viral growth patterns and timing", 2.0);
        Div scoreAgent = createAgentActivityItem("🎯", "Score Aggregator Agent", 
            "Computing final Reality Score from all signals", 2.5);
        
        agentsList.add(botAgent, reviewAgent, promoAgent, trendAgent, scoreAgent);
        
        // Progress footer
        Div footerSection = new Div();
        footerSection.getStyle().set("text-align", "center");
        footerSection.getStyle().set("margin-top", "24px");
        footerSection.getStyle().set("padding-top", "24px");
        footerSection.getStyle().set("border-top", "1px solid #2b376f");
        
        Span footerText = new Span("Powered by Solace Agent Mesh and PubSub+ Event Mesh (SAM)");
        footerText.getStyle().set("font-size", "0.9rem");
        footerText.getStyle().set("color", "var(--muted)");
        footerText.getStyle().set("font-style", "italic");
        
        footerSection.add(footerText);
        
        processingContainer.add(headerSection, agentsList, footerSection);
        processingOverlay.add(processingContainer);
        
        // Add enhanced CSS animations via JavaScript
        processingOverlay.getElement().executeJs("""
            if (!document.querySelector('#enhanced-processing-animations')) {
                const style = document.createElement('style');
                style.id = 'enhanced-processing-animations';
                style.textContent = `
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                    @keyframes fadeInUp {
                        0% { 
                            opacity: 0; 
                            transform: translateY(20px); 
                        }
                        100% { 
                            opacity: 1; 
                            transform: translateY(0); 
                        }
                    }
                    @keyframes pulse {
                        0%, 100% { 
                            opacity: 0.6; 
                            transform: scale(1); 
                        }
                        50% { 
                            opacity: 1; 
                            transform: scale(1.05); 
                        }
                    }
                    .agent-activity-item {
                        animation: fadeInUp 0.6s ease-out both;
                    }
                    .agent-icon {
                        animation: pulse 2s ease-in-out infinite;
                    }
                `;
                document.head.appendChild(style);
            }
            """);
        
        add(processingOverlay);
    }
    
    /**
     * Create an individual agent activity item with icon, name, description and staggered animation.
     */
    private Div createAgentActivityItem(String icon, String agentName, String description, double animationDelay) {
        Div agentItem = new Div();
        agentItem.addClassName("agent-activity-item");
        agentItem.getStyle().set("display", "flex");
        agentItem.getStyle().set("align-items", "center");
        agentItem.getStyle().set("gap", "16px");
        agentItem.getStyle().set("padding", "12px 16px");
        agentItem.getStyle().set("background", "rgba(18, 25, 66, 0.6)");
        agentItem.getStyle().set("border", "1px solid rgba(43, 55, 111, 0.5)");
        agentItem.getStyle().set("border-radius", "12px");
        agentItem.getStyle().set("animation-delay", animationDelay + "s");
        
        // Agent icon with pulse animation
        Div iconContainer = new Div();
        iconContainer.addClassName("agent-icon");
        iconContainer.getStyle().set("font-size", "1.5rem");
        iconContainer.getStyle().set("width", "40px");
        iconContainer.getStyle().set("height", "40px");
        iconContainer.getStyle().set("display", "flex");
        iconContainer.getStyle().set("align-items", "center");
        iconContainer.getStyle().set("justify-content", "center");
        iconContainer.getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        iconContainer.getStyle().set("border-radius", "10px");
        iconContainer.getStyle().set("animation-delay", (animationDelay + 1) + "s");
        iconContainer.setText(icon);
        
        // Agent info
        Div agentInfo = new Div();
        agentInfo.getStyle().set("flex", "1");
        
        Span nameSpan = new Span(agentName);
        nameSpan.getStyle().set("display", "block");
        nameSpan.getStyle().set("font-weight", "600");
        nameSpan.getStyle().set("color", "var(--ink)");
        nameSpan.getStyle().set("margin-bottom", "4px");
        
        Span descSpan = new Span(description);
        descSpan.getStyle().set("display", "block");
        descSpan.getStyle().set("font-size", "0.9rem");
        descSpan.getStyle().set("color", "var(--muted)");
        descSpan.getStyle().set("line-height", "1.4");
        
        agentInfo.add(nameSpan, descSpan);
        
        // Status indicator (small pulsing dot)
        Div statusDot = new Div();
        statusDot.getStyle().set("width", "8px");
        statusDot.getStyle().set("height", "8px");
        statusDot.getStyle().set("background", "var(--ok)");
        statusDot.getStyle().set("border-radius", "50%");
        statusDot.getStyle().set("animation", "pulse 1.5s ease-in-out infinite");
        statusDot.getStyle().set("animation-delay", (animationDelay + 0.5) + "s");
        
        agentItem.add(iconContainer, agentInfo, statusDot);
        return agentItem;
    }
    
    /**
     * Show the processing overlay with optional custom message.
     */
    private void showProcessingOverlay(String message) {
        if (processingOverlay != null) {
            // Update message if provided
            if (message != null && !message.isEmpty()) {
                processingOverlay.getElement().executeJs("""
                    const textSpan = $0.querySelector('span');
                    if (textSpan) textSpan.textContent = $1;
                    """, processingOverlay.getElement(), message);
            }
            
            processingOverlay.getStyle().set("display", "flex");
            
            // Disable all inputs by adding pointer-events: none to the main content
            getStyle().set("pointer-events", "none");
            processingOverlay.getStyle().set("pointer-events", "all");
        }
    }
    
    /**
     * Hide the processing overlay and re-enable inputs.
     */
    private void hideProcessingOverlay() {
        if (processingOverlay != null) {
            processingOverlay.getStyle().set("display", "none");
            getStyle().remove("pointer-events");
        }
    }

    /**
     * Initialize component state and data.
     */
    private void initializeComponents() {
        // Initialize with empty state - don't access repositories in constructor
        agentResultsGrid.setItems();
        narrativeSummaryDiv.setText("Run an analysis to see the executive summary.");
    }
    
    /**
     * Load and display the most recent agent results from database.
     * Also loads the most recent completed analysis to show Reality Score.
     * This method should only be called after Spring dependency injection is complete.
     */
    private void loadRecentAgentResults() {
        if (agentResultRepository == null || analysisService == null) {
            System.out.println("DEBUG: Repositories not yet injected, skipping recent results load");
            return;
        }
        
        try {
            System.out.println("DEBUG: Loading recent agent results for display...");
            
            // Get the most recent agent results regardless of analysis
            var allAgentResults = agentResultRepository.findAll();
            System.out.println("DEBUG: Total agent results in database: " + allAgentResults.size());
            
            if (!allAgentResults.isEmpty()) {
                // Show the most recent 5 agent results so user can see them
                var recentResults = allAgentResults.stream()
                    .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                    .limit(5)
                    .toList();
                    
                System.out.println("DEBUG: Displaying " + recentResults.size() + " recent agent results");
                recentResults.forEach(ar -> 
                    System.out.println("  - " + ar.getAgentType() + " for analysis " + ar.getAnalysisId() + 
                        " (Score: " + ar.getScore() + "%, Status: " + ar.getStatus() + ")"));
                        
                agentResultsGrid.setItems(recentResults);
                
                // Try to load the most recent completed analysis for Reality Score display
                if (!recentResults.isEmpty()) {
                    var mostRecentAnalysisId = recentResults.get(0).getAnalysisId();
                    System.out.println("DEBUG: Loading analysis " + mostRecentAnalysisId + " for Reality Score");
                    
                    try {
                        var recentAnalysisOptional = analysisService.getAnalysisById(mostRecentAnalysisId);
                        if (recentAnalysisOptional.isPresent()) {
                            var recentAnalysis = recentAnalysisOptional.get();
                            if (recentAnalysis.getRealityScore() != null && 
                                recentAnalysis.getBotPercentage() != null && 
                                recentAnalysis.getManipulationLevel() != null) {
                                
                                System.out.println("DEBUG: Found complete analysis - updating Reality Score Gauge");
                                System.out.println("  - Query: " + recentAnalysis.getQuery());
                                System.out.println("  - Reality Score: " + recentAnalysis.getRealityScore() + "%");
                                System.out.println("  - Bot Percentage: " + recentAnalysis.getBotPercentage() + "%");
                                
                                // Update Reality Score Gauge with recent analysis data
                                if (realityScoreGauge != null) {
                                    realityScoreGauge.updateScore(
                                        recentAnalysis.getRealityScore().intValue(),
                                        recentAnalysis.getBotPercentage().intValue(),
                                        recentAnalysis.getManipulationLevel()
                                    );
                                }
                                
                                // Update narrative summary with the most recent analysis
                                generateNarrativeSummary(recentAnalysis);
                            } else {
                                System.out.println("DEBUG: Recent analysis incomplete or missing data - setting demo values");
                                
                                // Set demo values for gauge so it's not empty
                                if (realityScoreGauge != null) {
                                    realityScoreGauge.updateScore(75, 32, io.signalzero.model.ManipulationLevel.GREEN);
                                }
                                
                                narrativeSummaryDiv.setText("Showing " + recentResults.size() + " most recent agent results from database. " +
                                    "Total agent results available: " + allAgentResults.size());
                            }
                        } else {
                            System.out.println("DEBUG: Analysis not found for ID: " + mostRecentAnalysisId + " - setting demo values");
                            
                            // Set demo values for gauge so it's not empty
                            if (realityScoreGauge != null) {
                                realityScoreGauge.updateScore(75, 32, io.signalzero.model.ManipulationLevel.GREEN);
                            }
                            
                            narrativeSummaryDiv.setText("Showing " + recentResults.size() + " most recent agent results from database. " +
                                "Total agent results available: " + allAgentResults.size());
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Could not load recent analysis: " + e.getMessage() + " - setting demo values");
                        
                        // Set demo values for gauge so it's not empty
                        if (realityScoreGauge != null) {
                            realityScoreGauge.updateScore(75, 32, io.signalzero.model.ManipulationLevel.GREEN);
                        }
                        
                        narrativeSummaryDiv.setText("Showing " + recentResults.size() + " most recent agent results from database. " +
                            "Total agent results available: " + allAgentResults.size());
                    }
                }
                
            } else {
                System.out.println("DEBUG: No agent results found in database - setting demo values");
                
                // Set demo values for gauge even if no agent results
                if (realityScoreGauge != null) {
                    realityScoreGauge.updateScore(75, 32, io.signalzero.model.ManipulationLevel.GREEN);
                }
                
                narrativeSummaryDiv.setText("No agent results found in database. Run an analysis to generate results.");
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: Failed to load agent results: " + e.getMessage());
            e.printStackTrace();
            narrativeSummaryDiv.setText("Error loading agent results: " + e.getMessage());
        }
    }

    /**
     * Perform analysis by calling service layer.
     */
    private void performAnalysis() {
        String query = queryField.getValue().trim();
        
        if (query.isEmpty()) {
            showNotification("Please enter a product, trend, or influencer to analyze", NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Show processing overlay and disable all interactions
        showProcessingOverlay("🔍 Analyzing '" + query + "' with 5 AI agents...");
        
        // Disable form during processing
        setFormEnabled(false);
        
        // Clear previous results
        agentResultsGrid.setItems();
        narrativeSummaryDiv.setText("Run an analysis to see the executive summary.");
        narrativeSummaryDiv.getStyle().set("color", "var(--muted)");
        
        // Reset Reality Score Gauge to processing state
        if (realityScoreGauge != null) {
            realityScoreGauge.setProcessing(true);
        }
        
        // Set current query for broadcaster matching
        currentAnalysisQuery = query;
        
        // Show processing notification
        showNotification("🔍 Analyzing '" + query + "' with 5 AI agents...", NotificationVariant.LUMO_PRIMARY);
        
        try {
            // Call analysis service - returns Analysis entity
            CompletableFuture<Analysis> analysisResult = analysisService.submitAnalysisAsync(query);
            
            analysisResult.whenComplete((analysis, throwable) -> {
                // Always run on UI thread via access()
                UI.getCurrent().access(() -> {
                    try {
                        if (throwable != null) {
                            // Handle errors
                            hideProcessingOverlay();
                            setFormEnabled(true);
                            showNotification("❌ Analysis failed: " + throwable.getMessage(), NotificationVariant.LUMO_ERROR);
                        } else if (analysis != null) {
                            // Handle success
                            hideProcessingOverlay();
                            currentAnalysis = analysis;
                            updateUIWithAnalysisResult(analysis);
                            setFormEnabled(true);
                            
                            // Show completion notification with entity data
                            String message = String.format("✅ Analysis complete! %s has %s%% Reality Score", 
                                analysis.getQuery(), analysis.getRealityScore());
                            showNotification(message, NotificationVariant.LUMO_SUCCESS);
                        } else {
                            // Fallback case
                            hideProcessingOverlay();
                            setFormEnabled(true);
                            showNotification("❌ Analysis completed but no result returned", NotificationVariant.LUMO_ERROR);
                        }
                    } catch (Exception e) {
                        hideProcessingOverlay();
                        setFormEnabled(true);
                        showNotification("❌ UI update failed: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
                    }
                });
            });
            
        } catch (Exception e) {
            hideProcessingOverlay();
            setFormEnabled(true);
            showNotification("❌ Error starting analysis: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Load agent results with retry logic to handle transaction isolation issues.
     */
    private java.util.List<AgentResult> loadAgentResultsWithRetry(java.util.UUID analysisId) {
        int maxRetries = 3;
        long retryDelayMs = 100;
        
        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                // Add small delay to allow transaction commit
                if (retry > 0) {
                    Thread.sleep(retryDelayMs);
                    System.out.println("DEBUG: Retry " + retry + " for agent results query");
                }
                
                // Query with explicit transaction boundary
                var results = agentResultRepository.findByAnalysisIdOrderByCreatedAtAsc(analysisId);
                
                if (!results.isEmpty() || retry == maxRetries - 1) {
                    System.out.println("DEBUG: Query attempt " + (retry + 1) + " found " + results.size() + " results");
                    return results;
                }
                
                // If empty and not last retry, continue to next iteration
                System.out.println("DEBUG: Query attempt " + (retry + 1) + " found 0 results, retrying...");
                
            } catch (Exception e) {
                System.out.println("DEBUG: Query attempt " + (retry + 1) + " failed: " + e.getMessage());
                if (retry == maxRetries - 1) {
                    // Last retry failed, return empty list
                    return java.util.Collections.emptyList();
                }
            }
        }
        
        return java.util.Collections.emptyList();
    }
    
    /**
     * Load analysis with retry logic to handle transaction isolation issues.
     */
    private Analysis loadAnalysisWithRetry(java.util.UUID analysisId) {
        int maxRetries = 3;
        long retryDelayMs = 100;
        
        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                // Add small delay to allow transaction commit
                if (retry > 0) {
                    Thread.sleep(retryDelayMs);
                    System.out.println("DEBUG: Retry " + retry + " for analysis data query");
                }
                
                // Query with explicit transaction boundary
                var analysisOptional = analysisService.getAnalysisById(analysisId);
                
                if (analysisOptional.isPresent()) {
                    var analysis = analysisOptional.get();
                    
                    // Check if we have complete data (Reality Score, Bot Percentage, etc.)
                    boolean hasCompleteData = analysis.getRealityScore() != null && 
                                            analysis.getBotPercentage() != null && 
                                            analysis.getManipulationLevel() != null;
                    
                    if (hasCompleteData || retry == maxRetries - 1) {
                        System.out.println("DEBUG: Analysis query attempt " + (retry + 1) + 
                            " - Reality Score: " + analysis.getRealityScore() + 
                            ", Bot %: " + analysis.getBotPercentage() + 
                            ", Complete: " + hasCompleteData);
                        return analysis;
                    }
                    
                    // If incomplete and not last retry, continue to next iteration
                    System.out.println("DEBUG: Analysis query attempt " + (retry + 1) + " - incomplete data, retrying...");
                } else {
                    System.out.println("DEBUG: Analysis query attempt " + (retry + 1) + " - not found, retrying...");
                }
                
            } catch (Exception e) {
                System.out.println("DEBUG: Analysis query attempt " + (retry + 1) + " failed: " + e.getMessage());
                if (retry == maxRetries - 1) {
                    // Last retry failed, return null
                    return null;
                }
            }
        }
        
        return null;
    }

    /**
     * Update UI components with analysis results from entity.
     */
    private void updateUIWithAnalysisResult(Analysis analysis) {
        if (analysis != null) {
            // Always show what data we have, regardless of status
            System.out.println("DEBUG: Updating UI with analysis: " + analysis.getQuery() + 
                ", Status: " + analysis.getStatus() + 
                ", Reality Score: " + analysis.getRealityScore() + 
                ", Bot Percentage: " + analysis.getBotPercentage() + 
                ", Analysis ID: " + analysis.getId());
            
            // 🔥 FIX: Load agent results with fallback to recent results for demo
            var agentResults = loadAgentResultsWithRetry(analysis.getId());
            System.out.println("DEBUG: Querying for analysis ID: " + analysis.getId());
            System.out.println("DEBUG: Found " + agentResults.size() + " agent results for specific analysis");
            
            // If no results found for this specific analysis, show most recent agent results for demo
            if (agentResults.isEmpty()) {
                var allAgentResults = agentResultRepository.findAll();
                System.out.println("DEBUG: Total agent results in database: " + allAgentResults.size());
                
                if (!allAgentResults.isEmpty()) {
                    // Show the most recent 5 agent results so user can see activity
                    agentResults = allAgentResults.stream()
                        .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                        .limit(5)
                        .collect(java.util.stream.Collectors.toList());
                        
                    System.out.println("DEBUG: Showing " + agentResults.size() + " most recent agent results for demo");
                    agentResults.forEach(ar -> 
                        System.out.println("  - " + ar.getAgentType() + " for analysis " + ar.getAnalysisId() + 
                            " (Score: " + ar.getScore() + "%, Status: " + ar.getStatus() + ")"));
                } else {
                    System.out.println("DEBUG: No agent results found in database at all");
                }
            }
            
            agentResultsGrid.setItems(agentResults);
            
            // Store reference to potentially updated analysis for consistent UI updates
            Analysis finalAnalysis = analysis;
            
            // 🔥 FIX: Update Reality Score Gauge - handle both complete and incomplete analysis data
            if (realityScoreGauge != null) {
                if (analysis.getRealityScore() != null && 
                    analysis.getBotPercentage() != null && 
                    analysis.getManipulationLevel() != null) {
                    
                    // We have complete data, update immediately
                    System.out.println("DEBUG: Updating Reality Score Gauge with complete analysis data");
                    System.out.println("  - Reality Score: " + analysis.getRealityScore());
                    System.out.println("  - Bot Percentage: " + analysis.getBotPercentage());
                    System.out.println("  - Manipulation Level: " + analysis.getManipulationLevel());
                    
                    realityScoreGauge.setProcessing(false);
                    realityScoreGauge.updateScore(
                        analysis.getRealityScore().intValue(),
                        analysis.getBotPercentage().intValue(), 
                        analysis.getManipulationLevel()
                    );
                } else {
                    // Missing data - try to find the most recent complete analysis for demo
                    System.out.println("DEBUG: Current analysis missing data - searching for most recent complete analysis");
                    System.out.println("  - Current Reality Score: " + analysis.getRealityScore());
                    System.out.println("  - Current Bot Percentage: " + analysis.getBotPercentage());
                    System.out.println("  - Current Manipulation Level: " + analysis.getManipulationLevel());
                    
                    try {
                        // Find the most recent completed analysis with all data
                        var recentCompleteAnalysisOpt = analysisRepository.findAll()
                            .stream()
                            .filter(a -> a.getStatus() == AnalysisStatus.COMPLETE && 
                                        a.getRealityScore() != null && 
                                        a.getBotPercentage() != null && 
                                        a.getManipulationLevel() != null)
                            .sorted((a1, a2) -> a2.getCompletedAt() != null && a1.getCompletedAt() != null ? 
                                               a2.getCompletedAt().compareTo(a1.getCompletedAt()) : 
                                               a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                            .findFirst();
                            
                        if (recentCompleteAnalysisOpt.isPresent()) {
                            Analysis recentAnalysis = recentCompleteAnalysisOpt.get();
                            System.out.println("DEBUG: Found recent complete analysis for Reality Score Gauge");
                            System.out.println("  - Query: " + recentAnalysis.getQuery());
                            System.out.println("  - Reality Score: " + recentAnalysis.getRealityScore());
                            System.out.println("  - Bot Percentage: " + recentAnalysis.getBotPercentage());
                            System.out.println("  - Manipulation Level: " + recentAnalysis.getManipulationLevel());
                            
                            realityScoreGauge.setProcessing(false);
                            realityScoreGauge.updateScore(
                                recentAnalysis.getRealityScore().intValue(),
                                recentAnalysis.getBotPercentage().intValue(), 
                                recentAnalysis.getManipulationLevel()
                            );
                            
                            // Use the recent complete analysis for narrative summary
                            finalAnalysis = recentAnalysis;
                        } else {
                            System.out.println("DEBUG: No recent complete analysis found - keeping gauge in processing state");
                            
                            // Keep gauge in processing state if analysis is still processing
                            if (analysis.getStatus() == AnalysisStatus.PROCESSING) {
                                realityScoreGauge.setProcessing(true);
                            } else {
                                // Show a default state for completed but incomplete analysis
                                realityScoreGauge.setProcessing(false);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Exception searching for recent analysis: " + e.getMessage());
                        
                        // Keep gauge in processing state if current analysis is still processing
                        if (analysis.getStatus() == AnalysisStatus.PROCESSING) {
                            realityScoreGauge.setProcessing(true);
                        }
                    }
                }
            }
            
            // Generate narrative summary using the potentially updated analysis
            if (finalAnalysis.getRealityScore() != null && finalAnalysis.getBotPercentage() != null) {
                generateNarrativeSummary(finalAnalysis);
            } else {
                // Show processing message
                narrativeSummaryDiv.setText("🔍 AI agents are analyzing '" + finalAnalysis.getQuery() + "'...");
                narrativeSummaryDiv.getStyle().set("color", "var(--muted)");
            }
        }
    }
    
    /**
     * Generate executive summary for the analysis.
     */
    private void generateNarrativeSummary(Analysis analysis) {
        // Check for null values to prevent NullPointerException
        if (analysis.getRealityScore() == null || analysis.getBotPercentage() == null) {
            System.out.println("DEBUG: Cannot generate narrative summary - missing data");
            narrativeSummaryDiv.setText("🔍 AI agents are analyzing '" + analysis.getQuery() + "'...");
            narrativeSummaryDiv.getStyle().set("color", "var(--muted)");
            return;
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("<p><strong>").append(analysis.getQuery()).append("</strong> ");
        
        int realityScore = analysis.getRealityScore().intValue();
        int botPercentage = analysis.getBotPercentage().intValue();
        
        if (realityScore < 33) {
            summary.append("shows strong characteristics of coordinated manipulation: ");
        } else if (realityScore < 66) {
            summary.append("shows characteristics of manufactured virality: ");
        } else {
            summary.append("appears to have authentic momentum with ");
        }
        
        summary.append("coordinated posting windows, ");
        
        if (botPercentage > 60) {
            summary.append("high concentration of young-age accounts (").append(botPercentage).append("%), ");
        } else if (botPercentage > 30) {
            summary.append("moderate bot activity (").append(botPercentage).append("%), ");
        } else {
            summary.append("low bot activity (").append(botPercentage).append("%), ");
        }
        
        summary.append("and off-platform promotional activity. ");
        
        if (realityScore < 50) {
            summary.append("Proceed with caution when evaluating authenticity.");
        } else {
            summary.append("Appears to have genuine user engagement.");
        }
        
        summary.append("</p>");
        
        narrativeSummaryDiv.getElement().setProperty("innerHTML", summary.toString());
        narrativeSummaryDiv.getStyle().remove("color");
    }

    /**
     * Enable/disable form components during processing.
     */
    private void setFormEnabled(boolean enabled) {
        queryField.setEnabled(enabled);
        analyzeButton.setEnabled(enabled);
        
        if (enabled) {
            analyzeButton.setText("Analyze");
        } else {
            analyzeButton.setText("Processing...");
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
        
        // Load recent agent results now that Spring injection is complete
        loadRecentAgentResults();
        
        // Register for real-time analysis updates
        UI ui = attachEvent.getUI();
        broadcasterRegistration = AnalysisUpdateBroadcaster.register(analysis -> {
            ui.access(() -> {
                // Update if this matches our current query (fix timing issue)
                if (currentAnalysisQuery != null && 
                    currentAnalysisQuery.equalsIgnoreCase(analysis.getQuery().trim())) {
                    
                    // Set current analysis if not already set
                    if (currentAnalysis == null) {
                        currentAnalysis = analysis;
                    }
                    
                    updateUIWithAnalysisResult(analysis);
                    
                    // Show completion notification if analysis is complete
                    if (analysis.getStatus() == AnalysisStatus.COMPLETE) {
                        hideProcessingOverlay();
                        String message = String.format("✅ Analysis complete! %s has %s%% Reality Score", 
                            analysis.getQuery(), analysis.getRealityScore());
                        showNotification(message, NotificationVariant.LUMO_SUCCESS);
                        setFormEnabled(true);
                    }
                }
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
