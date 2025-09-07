package io.signalzero.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
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

import java.time.format.DateTimeFormatter;
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
    private AnalysisRepository analysisRepository;
    
    @Autowired
    private AgentResultRepository agentResultRepository;

    // UI Components
    private TextField queryField;
    private Button analyzeButton;
    private Checkbox botAnalysisCheckbox;
    private Checkbox reviewAuthenticityCheckbox;
    private Checkbox paidPromotionCheckbox;
    private Checkbox trendPatterningCheckbox;
    private Grid<AgentResult> agentResultsGrid;
    private Div narrativeSummaryDiv;
    
    // Real-time update registration
    private Registration broadcasterRegistration;
    
    // Current analysis tracking
    private Analysis currentAnalysis;

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
        
        Span signalsLabel = new Span("Which signals?");
        signalsLabel.addClassName("muted");
        signalsLabel.getStyle().set("color", "var(--muted)");
        
        // Signal checkboxes
        HorizontalLayout signalsCheckboxes = new HorizontalLayout();
        signalsCheckboxes.setSpacing(true);
        signalsCheckboxes.getStyle().set("margin-top", "8px");
        signalsCheckboxes.getStyle().set("flex-wrap", "wrap");
        
        botAnalysisCheckbox = createSignalCheckbox("Bot Analysis", true);
        reviewAuthenticityCheckbox = createSignalCheckbox("Review Authenticity", true);
        paidPromotionCheckbox = createSignalCheckbox("Paid Promotion", true);
        trendPatterningCheckbox = createSignalCheckbox("Trend Patterning", true);
        
        signalsCheckboxes.add(botAnalysisCheckbox, reviewAuthenticityCheckbox, paidPromotionCheckbox, trendPatterningCheckbox);
        
        // Tech stack info
        Span techInfo = new Span("Transport: Solace PubSub+ · Orchestration: Agent Mesh · Backend: Java");
        techInfo.addClassName("muted");
        techInfo.getStyle().set("color", "var(--muted)");
        techInfo.getStyle().set("margin-top", "12px");
        techInfo.getStyle().set("font-size", "12px");
        
        rightColumn.add(signalsLabel, signalsCheckboxes, techInfo);
        
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
        
        // Narrative Summary Panel
        Div narrativeSummaryPanel = createNarrativeSummaryPanel();
        
        resultsLayout.add(agentResultsPanel, narrativeSummaryPanel);
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
        agentResultsGrid.addClassName("s1gnal-grid");
        agentResultsGrid.setHeight("300px");
        
        // Configure columns
        agentResultsGrid.addColumn(AgentResult::getAgentType)
            .setHeader("Agent")
            .setFlexGrow(1);
            
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
            return new Span(keyFinding);
        })).setHeader("Key Finding").setFlexGrow(2);
            
        agentResultsGrid.addColumn(new ComponentRenderer<>(result -> {
            Span scoreSpan = new Span(result.getScore() + "%");
            scoreSpan.getStyle().set("font-weight", "bold");
            return scoreSpan;
        })).setHeader("Score").setFlexGrow(1);
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
     * Initialize component state and data.
     */
    private void initializeComponents() {
        // Initialize with empty state
        agentResultsGrid.setItems();
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
        
        // Disable form during processing
        setFormEnabled(false);
        
        // Show processing notification
        showNotification("🔍 Analyzing '" + query + "' with AI agents...", NotificationVariant.LUMO_PRIMARY);
        
        try {
            // Call analysis service - returns Analysis entity
            CompletableFuture<Analysis> analysisResult = analysisService.submitAnalysisAsync(query);
            
            analysisResult.whenComplete((analysis, throwable) -> {
                // Always run on UI thread via access()
                UI.getCurrent().access(() -> {
                    try {
                        if (throwable != null) {
                            // Handle errors
                            setFormEnabled(true);
                            showNotification("❌ Analysis failed: " + throwable.getMessage(), NotificationVariant.LUMO_ERROR);
                        } else if (analysis != null) {
                            // Handle success
                            currentAnalysis = analysis;
                            updateUIWithAnalysisResult(analysis);
                            setFormEnabled(true);
                            
                            // Show completion notification with entity data
                            String message = String.format("✅ Analysis complete! %s has %s%% Reality Score", 
                                analysis.getQuery(), analysis.getRealityScore());
                            showNotification(message, NotificationVariant.LUMO_SUCCESS);
                        } else {
                            // Fallback case
                            setFormEnabled(true);
                            showNotification("❌ Analysis completed but no result returned", NotificationVariant.LUMO_ERROR);
                        }
                    } catch (Exception e) {
                        setFormEnabled(true);
                        showNotification("❌ UI update failed: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
                    }
                });
            });
            
        } catch (Exception e) {
            setFormEnabled(true);
            showNotification("❌ Error starting analysis: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Update UI components with analysis results from entity.
     */
    private void updateUIWithAnalysisResult(Analysis analysis) {
        if (analysis != null && analysis.getStatus() == AnalysisStatus.COMPLETE) {
            // Load agent results from repository
            var agentResults = agentResultRepository.findByAnalysisIdOrderByCreatedAtAsc(analysis.getId());
            agentResultsGrid.setItems(agentResults);
            
            // Generate narrative summary
            generateNarrativeSummary(analysis);
        }
    }
    
    /**
     * Generate executive summary for the analysis.
     */
    private void generateNarrativeSummary(Analysis analysis) {
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
        
        // Register for real-time analysis updates
        UI ui = attachEvent.getUI();
        broadcasterRegistration = AnalysisUpdateBroadcaster.register(analysis -> {
            ui.access(() -> {
                // Update current analysis if it matches
                if (currentAnalysis != null && 
                    currentAnalysis.getId().equals(analysis.getId())) {
                    updateUIWithAnalysisResult(analysis);
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
