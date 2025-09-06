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
@Route("")
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
        addClassName("s1gnal-main-layout");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        createHeader();
        createAnalysisForm();
        createStatsSection();
        createRecentAnalysesGrid();
        createFooter();
        
        // Load initial data from repository
        refreshDashboardData();
    }

    /**
     * Create the main header with logo and title.
     */
    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("s1gnal-header");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setPadding(true);
        
        // Logo and title
        Icon logo = new Icon(VaadinIcon.SHIELD);
        logo.addClassName("s1gnal-logo");
        logo.setSize("2rem");
        
        H1 title = new H1("S1GNAL.ZERO");
        title.addClassName("s1gnal-logo");
        title.getStyle().set("margin", "0");
        
        Span subtitle = new Span("AI-Powered Authenticity Verification System");
        subtitle.getStyle().set("color", "var(--s1gnal-text-secondary)");
        subtitle.getStyle().set("margin-left", "1rem");
        
        VerticalLayout titleSection = new VerticalLayout(title, subtitle);
        titleSection.setSpacing(false);
        titleSection.setPadding(false);
        
        header.add(logo, titleSection);
        header.setFlexGrow(1, titleSection);
        
        add(header);
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
            // Get statistics directly from repository
            long totalCount = analysisRepository.count();
            totalAnalysesCount.setText(String.valueOf(totalCount));
            
            if (totalCount > 0) {
                // Calculate averages using repository queries
                List<Analysis> completedAnalyses = analysisRepository.findByStatusOrderByCreatedAtDesc(AnalysisStatus.COMPLETE);
                
                if (!completedAnalyses.isEmpty()) {
                    double avgBot = completedAnalyses.stream()
                        .mapToDouble(a -> a.getBotPercentage().doubleValue())
                        .average()
                        .orElse(0.0);
                    
                    double avgReality = completedAnalyses.stream()
                        .mapToDouble(a -> a.getRealityScore().doubleValue())
                        .average()
                        .orElse(0.0);
                    
                    avgBotPercentage.setText(String.format("%.0f%%", avgBot));
                    avgRealityScore.setText(String.format("%.0f%%", avgReality));
                }
            }
            
            // Refresh recent analyses grid with repository data
            List<Analysis> recentAnalyses = analysisRepository.findTop10ByIsPublicTrueAndStatusOrderByCreatedAtDesc(AnalysisStatus.COMPLETE);
            recentAnalysesGrid.setItems(recentAnalyses);
            
        } catch (Exception e) {
            showNotification("Error refreshing dashboard data: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
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
