package io.signalzero.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import io.signalzero.model.WallOfShame;
import io.signalzero.model.ManipulationLevel;
import io.signalzero.repository.WallOfShameRepository;
import io.signalzero.ui.AnalysisUpdateBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Wall of Shame View - Public display of highly manipulated products/trends
 * 
 * This view shows products/trends with high bot activity and manipulation.
 * Uses the repository pattern for direct entity binding without DTOs.
 * Includes real-time updates via WebSocket broadcasting.
 * 
 * Features:
 * - Public access (no authentication required)
 * - Real-time updates of new manipulated products
 * - Entity-based grid display with custom renderers
 * - Blue theme styling matching UI mockup
 * - Mobile responsive design
 * 
 * @author S1GNAL.ZERO Team
 * @since 1.0.0
 */
@Route("wall-of-shame")
@PageTitle("Wall of Shame | S1GNAL.ZERO")
@AnonymousAllowed  // Public access - no authentication required
public class WallOfShameView extends VerticalLayout {

    private final WallOfShameRepository wallOfShameRepository;
    private Grid<WallOfShame> wallOfShameGrid;
    private Registration broadcasterRegistration;
    
    // Statistics components
    private Span totalItemsCount;
    private Span avgBotPercentage;
    private Span mostManipulatedProduct;

    @Autowired
    public WallOfShameView(WallOfShameRepository wallOfShameRepository) {
        this.wallOfShameRepository = wallOfShameRepository;
        
        setSizeFull();
        setSpacing(true);
        setPadding(false);
        setMargin(false);
        addClassName("s1gnal-main-layout");
        
        createHeader();
        createStatisticsSection();
        createWallOfShameGrid();
        loadWallOfShameData();
    }

    private void createHeader() {
        // Header section matching UI mockup design
        Div headerSection = new Div();
        headerSection.addClassName("s1gnal-header");
        
        // Logo section
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.addClassName("s1gnal-logo");
        logoLayout.setAlignItems(Alignment.CENTER);
        
        // Logo badge with emoji
        Div logoBadge = new Div();
        logoBadge.addClassName("logo-badge");
        logoBadge.setText("🎭"); // Mask emoji for manipulation detection
        
        // Logo text
        VerticalLayout logoText = new VerticalLayout();
        logoText.addClassName("logo-text");
        logoText.setSpacing(false);
        logoText.setPadding(false);
        
        H1 logoTitle = new H1("S1GNAL.ZERO");
        logoTitle.addClassName("logo-title");
        
        Span logoSubtitle = new Span("Wall of Shame");
        logoSubtitle.addClassName("logo-subtitle");
        
        logoText.add(logoTitle, logoSubtitle);
        logoLayout.add(logoBadge, logoText);
        
        // Action buttons
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setAlignItems(Alignment.CENTER);
        
        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> refreshData());
        
        Button dashboardButton = new Button("Dashboard", new Icon(VaadinIcon.DASHBOARD));
        dashboardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dashboardButton.addClickListener(e -> UI.getCurrent().navigate(""));
        
        actionsLayout.add(refreshButton, dashboardButton);
        
        headerSection.add(logoLayout, actionsLayout);
        add(headerSection);
    }

    private void createStatisticsSection() {
        // Statistics cards matching KPI design from mockup
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        
        // Total manipulated products
        Div totalCard = createStatCard(
            "🚨", 
            "Flagged Products",
            totalItemsCount = new Span("0"),
            "High manipulation detected"
        );
        
        // Average bot percentage
        Div avgCard = createStatCard(
            "🤖", 
            "Avg Bot Activity",
            avgBotPercentage = new Span("0%"),
            "Across all flagged items"
        );
        
        // Most manipulated item
        Div topCard = createStatCard(
            "🔥", 
            "Worst Offender",
            mostManipulatedProduct = new Span("Loading..."),
            "Highest bot percentage"
        );
        
        statsLayout.add(totalCard, avgCard, topCard);
        
        // Wrap in panel
        Div statsPanel = new Div();
        statsPanel.addClassName("s1gnal-panel");
        statsPanel.add(statsLayout);
        
        add(statsPanel);
    }

    private Div createStatCard(String icon, String label, Span value, String description) {
        Div card = new Div();
        card.addClassName("kpi-card");
        
        // Icon
        Div iconDiv = new Div();
        iconDiv.addClassName("kpi-icon");
        iconDiv.setText(icon);
        
        // Content
        Div content = new Div();
        content.addClassName("kpi-content");
        
        Div labelDiv = new Div();
        labelDiv.addClassName("kpi-label");
        labelDiv.setText(label);
        
        value.addClassName("kpi-value");
        
        Div descDiv = new Div();
        descDiv.addClassName("kpi-label");
        descDiv.setText(description);
        
        content.add(labelDiv, value, descDiv);
        card.add(iconDiv, content);
        
        return card;
    }

    private void createWallOfShameGrid() {
        // Panel header
        Div headerDiv = new Div();
        headerDiv.addClassName("panel-header");
        
        H2 title = new H2("Wall of Shame");
        title.addClassName("panel-title");
        
        Span subtitle = new Span("Products and trends with suspicious viral activity");
        subtitle.addClassName("kpi-label");
        
        VerticalLayout headerContent = new VerticalLayout();
        headerContent.setSpacing(false);
        headerContent.setPadding(false);
        headerContent.add(title, subtitle);
        
        Button viewAllButton = new Button("View All History");
        viewAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllButton.addClickListener(e -> UI.getCurrent().navigate("history"));
        
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.add(headerContent, viewAllButton);
        
        headerDiv.add(headerLayout);
        
        // Grid setup with entity binding (repository pattern)
        wallOfShameGrid = new Grid<>(WallOfShame.class, false);
        wallOfShameGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        wallOfShameGrid.setAllRowsVisible(true);
        wallOfShameGrid.addClassName("s1gnal-grid");
        
        // Product/Company column
        wallOfShameGrid.addColumn(new ComponentRenderer<>(this::createProductCell))
                .setHeader("Product / Company")
                .setFlexGrow(2)
                .setResizable(true);
        
        // Bot percentage column
        wallOfShameGrid.addColumn(new ComponentRenderer<>(this::createBotPercentageCell))
                .setHeader("Bot Activity")
                .setFlexGrow(1)
                .setResizable(true);
        
        // Reality Score column
        wallOfShameGrid.addColumn(new ComponentRenderer<>(this::createRealityScoreCell))
                .setHeader("Reality Score")
                .setFlexGrow(1)
                .setResizable(true);
        
        // Evidence column
        wallOfShameGrid.addColumn(new ComponentRenderer<>(this::createEvidenceCell))
                .setHeader("Key Evidence")
                .setFlexGrow(2)
                .setResizable(true);
        
        // Date column
        wallOfShameGrid.addColumn(wallOfShame -> 
                wallOfShame.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")))
                .setHeader("Detected")
                .setFlexGrow(1)
                .setResizable(true);
        
        // Panel wrapper
        Div gridPanel = new Div();
        gridPanel.addClassName("s1gnal-panel");
        
        Div panelBody = new Div();
        panelBody.addClassName("panel-body");
        panelBody.add(wallOfShameGrid);
        
        gridPanel.add(headerDiv, panelBody);
        add(gridPanel);
    }

    private Component createProductCell(WallOfShame wallOfShame) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        
        // Product name
        H3 productName = new H3(wallOfShame.getProductName());
        productName.addClassName("product-name");
        productName.getStyle().set("margin", "0 0 4px 0");
        
        // Company and category
        HorizontalLayout detailsLayout = new HorizontalLayout();
        detailsLayout.setSpacing(true);
        detailsLayout.setPadding(false);
        
        if (wallOfShame.getCompany() != null && !wallOfShame.getCompany().isEmpty()) {
            Span company = new Span(wallOfShame.getCompany());
            company.addClassName("kpi-label");
            detailsLayout.add(company);
        }
        
        if (wallOfShame.getCategory() != null && !wallOfShame.getCategory().isEmpty()) {
            Span category = new Span("• " + wallOfShame.getCategory());
            category.addClassName("kpi-label");
            detailsLayout.add(category);
        }
        
        layout.add(productName, detailsLayout);
        return layout;
    }

    private Component createBotPercentageCell(WallOfShame wallOfShame) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(Alignment.CENTER);
        
        // Bot percentage with color coding
        Span percentage = new Span(wallOfShame.getBotPercentage().intValue() + "%");
        percentage.addClassName("bot-percentage");
        percentage.getStyle().set("font-size", "1.5rem");
        percentage.getStyle().set("font-weight", "800");
        
        // Color based on severity
        if (wallOfShame.getBotPercentage().compareTo(BigDecimal.valueOf(70)) >= 0) {
            percentage.addClassName("score-red");
        } else if (wallOfShame.getBotPercentage().compareTo(BigDecimal.valueOf(50)) >= 0) {
            percentage.addClassName("score-yellow");
        }
        
        // Bots label
        Span label = new Span("bots detected");
        label.addClassName("kpi-label");
        
        layout.add(percentage, label);
        return layout;
    }

    private Component createRealityScoreCell(WallOfShame wallOfShame) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(Alignment.CENTER);
        
        // Reality Score
        Span score = new Span(wallOfShame.getRealityScore().intValue() + "%");
        score.getStyle().set("font-size", "1.3rem");
        score.getStyle().set("font-weight", "700");
        
        // Manipulation badge
        Span badge = new Span();
        badge.addClassName("manipulation-badge");
        
        switch (wallOfShame.getManipulationLevel()) {
            case RED:
                score.addClassName("score-red");
                badge.addClassName("manipulation-red");
                badge.setText("HEAVILY MANIPULATED");
                break;
            case YELLOW:
                score.addClassName("score-yellow");
                badge.addClassName("manipulation-yellow");
                badge.setText("MIXED SIGNALS");
                break;
            case GREEN:
                score.addClassName("score-green");
                badge.addClassName("manipulation-green");
                badge.setText("MOSTLY AUTHENTIC");
                break;
        }
        
        layout.add(score, badge);
        return layout;
    }

    private Component createEvidenceCell(WallOfShame wallOfShame) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        
        // Evidence summary
        if (wallOfShame.getEvidenceSummary() != null && !wallOfShame.getEvidenceSummary().isEmpty()) {
            Paragraph summary = new Paragraph(wallOfShame.getEvidenceSummary());
            summary.addClassName("wall-item-details");
            summary.getStyle().set("margin", "0 0 8px 0");
            layout.add(summary);
        }
        
        // Key findings tags
        if (wallOfShame.getKeyFindings() != null) {
            HorizontalLayout tagsLayout = new HorizontalLayout();
            tagsLayout.setSpacing(true);
            tagsLayout.setPadding(false);
            tagsLayout.addClassName("wall-item-tags");
            
            // Parse key findings (assuming JSON array format)
            try {
                String findings = wallOfShame.getKeyFindings().toString();
                // Simple parsing - in production you might use proper JSON parsing
                if (findings.contains("bot")) {
                    Span tag = new Span("Bot Surge");
                    tag.addClassName("score-tag");
                    tagsLayout.add(tag);
                }
                if (findings.contains("review") || findings.contains("cluster")) {
                    Span tag = new Span("Review Clusters");
                    tag.addClassName("score-tag");
                    tagsLayout.add(tag);
                }
                if (findings.contains("ad") || findings.contains("promo")) {
                    Span tag = new Span("Paid Promos");
                    tag.addClassName("score-tag");
                    tagsLayout.add(tag);
                }
                if (findings.contains("spike") || findings.contains("velocity")) {
                    Span tag = new Span("Artificial Spike");
                    tag.addClassName("score-tag");
                    tagsLayout.add(tag);
                }
                
                if (tagsLayout.getComponentCount() > 0) {
                    layout.add(tagsLayout);
                }
            } catch (Exception e) {
                // Fallback to simple display
                Span fallbackTag = new Span("Multiple Signals");
                fallbackTag.addClassName("score-tag");
                HorizontalLayout fallbackLayout = new HorizontalLayout(fallbackTag);
                fallbackLayout.addClassName("wall-item-tags");
                layout.add(fallbackLayout);
            }
        }
        
        return layout;
    }

    private void loadWallOfShameData() {
        // Load active Wall of Shame entries using repository
        List<WallOfShame> wallOfShameItems = wallOfShameRepository.findTop10ByIsActiveTrueOrderByBotPercentageDesc(true);
        wallOfShameGrid.setItems(wallOfShameItems);
        
        // Update statistics
        updateStatistics(wallOfShameItems);
    }

    private void updateStatistics(List<WallOfShame> items) {
        if (items.isEmpty()) {
            totalItemsCount.setText("0");
            avgBotPercentage.setText("0%");
            mostManipulatedProduct.setText("None found");
            return;
        }
        
        // Total count
        Long totalCount = wallOfShameRepository.countByIsActive(true);
        totalItemsCount.setText(totalCount.toString());
        
        // Average bot percentage
        double avgBot = items.stream()
                .mapToDouble(item -> item.getBotPercentage().doubleValue())
                .average()
                .orElse(0.0);
        avgBotPercentage.setText(String.format("%.0f%%", avgBot));
        
        // Most manipulated product
        WallOfShame topItem = items.get(0); // Already ordered by bot percentage desc
        mostManipulatedProduct.setText(topItem.getProductName() + " (" + topItem.getBotPercentage().intValue() + "% bots)");
    }

    private void refreshData() {
        loadWallOfShameData();
        
        // Add flash animation to indicate refresh
        getElement().getClassList().add("s1gnal-update-flash");
        UI.getCurrent().getPage().executeJs(
            "setTimeout(() => $0.classList.remove('s1gnal-update-flash'), 500)", 
            getElement()
        );
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Register for real-time updates via WebSocket
        UI ui = attachEvent.getUI();
        broadcasterRegistration = AnalysisUpdateBroadcaster.register(analysis -> {
            ui.access(() -> {
                // Check if this analysis should be added to Wall of Shame
                if (analysis.getBotPercentage() != null && 
                    analysis.getBotPercentage().compareTo(BigDecimal.valueOf(60)) > 0) {
                    refreshData();
                }
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Unregister from broadcasts when component is detached
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
