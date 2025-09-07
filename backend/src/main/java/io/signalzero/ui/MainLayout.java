package io.signalzero.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;

/**
 * S1GNAL.ZERO - Main Layout
 * AGI Ventures Canada Hackathon 3.0 (September 6-7, 2025)
 * 
 * Shared layout component providing consistent header and sidebar navigation
 * across all views in the application.
 * 
 * Features:
 * - Top navigation bar with logo and search
 * - Collapsible sidebar with navigation links
 * - Dark theme styling consistent with UI mockup
 * - Real-time push notification support
 * 
 * CRITICAL REQUIREMENTS (from CLAUDE.md):
 * - Repository pattern with direct JPA entity binding
 * - Production-ready UI components
 * - Consistent theming across all views
 * 
 * Reference: DETAILED_DESIGN.md Section 11 - Vaadin UI Components
 */
public class MainLayout extends AppLayout {

    /**
     * Initialize the main layout with header and drawer components.
     */
    public MainLayout() {
        // Set up CSS custom properties for theming
        setupThemeVariables();
        
        // Apply dark theme styling
        addClassName("main-layout");
        getElement().getStyle().set("background", "var(--bg)");
        getElement().getStyle().set("color", "var(--ink)");
        
        createHeader();
        createDrawer();
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
     * Create the top header bar matching the UI mockup design.
     */
    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("topbar");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setPadding(true);
        header.getStyle().set("background", "var(--panel)");
        header.getStyle().set("box-shadow", "var(--shadow)");
        header.getStyle().set("z-index", "5");
        header.setHeight("72px");
        
        // Drawer toggle
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("drawer-toggle");
        toggle.getStyle().set("color", "var(--ink)");
        
        // Logo section
        HorizontalLayout logoSection = new HorizontalLayout();
        logoSection.addClassName("logo");
        logoSection.setAlignItems(Alignment.CENTER);
        logoSection.setSpacing(true);
        
        // Logo badge
        Div logoBadge = createBinaryShieldLogo();
        logoBadge.addClassName("logo-badge");
        logoBadge.getStyle().set("width", "40px");
        logoBadge.getStyle().set("height", "40px");
        logoBadge.getStyle().set("border-radius", "12px");
        logoBadge.getStyle().set("background", "var(--accent)");
        logoBadge.getStyle().set("display", "grid");
        logoBadge.getStyle().set("place-items", "center");
        logoBadge.getStyle().set("box-shadow", "0 10px 25px rgba(102,126,234,.35)");
        
        // Logo text
        VerticalLayout logoText = new VerticalLayout();
        logoText.setSpacing(false);
        logoText.setPadding(false);
        
        Span brandName = new Span("S1GNAL.ZERO");
        brandName.getStyle().set("font-size", "14px");
        brandName.getStyle().set("color", "var(--muted)");
        brandName.getStyle().set("font-weight", "700");
        brandName.getStyle().set("letter-spacing", ".12em");
        
        Span tagline = new Span("AI-Powered Authenticity Verification");
        tagline.getStyle().set("font-size", "18px");
        tagline.getStyle().set("font-weight", "800");
        tagline.getStyle().set("line-height", "1");
        
        logoText.add(brandName, tagline);
        logoSection.add(logoBadge, logoText);
        
        // Search bar
        HorizontalLayout searchBar = new HorizontalLayout();
        searchBar.addClassName("search");
        searchBar.setAlignItems(Alignment.CENTER);
        searchBar.getStyle().set("background", "#0e1430");
        searchBar.getStyle().set("border", "1px solid #263061");
        searchBar.getStyle().set("border-radius", "12px");
        searchBar.getStyle().set("padding", "10px 12px");
        searchBar.getStyle().set("min-width", "360px");
        searchBar.getStyle().set("color", "var(--muted)");
        
        TextField searchInput = new TextField();
        searchInput.setPlaceholder("Search history, trends, influencers…");
        searchInput.addClassName("search-input");
        searchInput.getStyle().set("background", "transparent");
        searchInput.getStyle().set("border", "none");
        searchInput.getStyle().set("color", "var(--ink)");
        searchInput.getStyle().set("width", "100%");
        
        searchBar.add(searchInput);
        
        // User section
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.addClassName("user");
        userSection.setAlignItems(Alignment.CENTER);
        userSection.setSpacing(true);
        
        Span techChip = new Span("Java + Agent Mesh");
        techChip.addClassName("chip");
        techChip.getStyle().set("padding", "3px 8px");
        techChip.getStyle().set("border-radius", "999px");
        techChip.getStyle().set("font-size", "11px");
        techChip.getStyle().set("background", "#1a2149");
        techChip.getStyle().set("color", "#8aa0ff");
        techChip.getStyle().set("border", "1px solid #2b376f");
        
        Div avatar = new Div();
        avatar.addClassName("avatar");
        avatar.getStyle().set("width", "36px");
        avatar.getStyle().set("height", "36px");
        avatar.getStyle().set("border-radius", "50%");
        avatar.getStyle().set("background", "linear-gradient(135deg,#93c5fd,#a78bfa)");
        
        userSection.add(techChip, avatar);
        
        header.add(toggle, logoSection, searchBar, userSection);
        addToNavbar(header);
    }
    
    /**
     * Create sidebar drawer navigation matching the UI mockup design.
     * Optimized for desktop viewing without scrolling.
     */
    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.addClassName("sidebar");
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.getStyle().set("background", "var(--panel)");
        drawer.getStyle().set("border-right", "1px solid #1b2452");
        drawer.getStyle().set("width", "280px");
        drawer.getStyle().set("height", "100vh");
        drawer.getStyle().set("padding", "16px 12px");
        drawer.getStyle().set("box-sizing", "border-box");
        drawer.getStyle().set("overflow-y", "auto");
        
        // Navigation title
        Span navTitle = new Span("Navigation");
        navTitle.addClassName("nav-title");
        navTitle.getStyle().set("font-size", "11px");
        navTitle.getStyle().set("letter-spacing", ".12em");
        navTitle.getStyle().set("color", "var(--muted)");
        navTitle.getStyle().set("text-transform", "uppercase");
        navTitle.getStyle().set("margin", "0 8px 8px 8px");
        navTitle.getStyle().set("font-weight", "600");
        
        // Navigation items
        VerticalLayout navItems = new VerticalLayout();
        navItems.addClassName("nav");
        navItems.setSpacing(false);
        navItems.setPadding(false);
        navItems.getStyle().set("gap", "2px");
        
        // Dashboard link
        RouterLink dashboardLink = new RouterLink("", DashboardView.class);
        Div dashboardDiv = createNavLink("📊", "Dashboard", isCurrentRoute(""));
        dashboardLink.add(dashboardDiv);
        dashboardLink.getStyle().set("text-decoration", "none");
        
        // Analyze link
        RouterLink analyzeLink = new RouterLink("analyze", AnalysisView.class);
        Div analyzeDiv = createNavLink("🔍", "Analyze", isCurrentRoute("analyze"));
        analyzeLink.add(analyzeDiv);
        analyzeLink.getStyle().set("text-decoration", "none");
        
        // Other navigation items (placeholder for now)
        Div historyLink = createNavLink("🕘", "History", false);
        historyLink.addClickListener(e -> UI.getCurrent().getPage().executeJs("alert('History page coming soon!')"));
        
        Div agentsLink = createNavLink("🤖", "Agents", false);
        agentsLink.addClickListener(e -> UI.getCurrent().getPage().executeJs("alert('Agents page coming soon!')"));
        
        Div dataLink = createNavLink("🔗", "Data Sources", false);
        dataLink.addClickListener(e -> UI.getCurrent().getPage().executeJs("alert('Data Sources page coming soon!')"));
        
        Div adminLink = createNavLink("⚙️", "Admin", false);
        adminLink.addClickListener(e -> UI.getCurrent().getPage().executeJs("alert('Admin page coming soon!')"));
        
        navItems.add(dashboardLink, analyzeLink, historyLink, agentsLink, dataLink, adminLink);
        
        drawer.add(navTitle, navItems);
        addToDrawer(drawer);
    }
    
    /**
     * Create a navigation link component.
     */
    private Div createNavLink(String icon, String label, boolean isActive) {
        Div link = new Div();
        link.addClassName("nav-link");
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("gap", "12px");
        link.getStyle().set("padding", "12px 14px");
        link.getStyle().set("border-radius", "12px");
        link.getStyle().set("cursor", "pointer");
        link.getStyle().set("text-decoration", "none");
        
        if (isActive) {
            link.addClassName("active");
            link.getStyle().set("background", "linear-gradient(135deg, rgba(102,126,234,.18), rgba(118,75,162,.18))");
            link.getStyle().set("color", "#fff");
            link.getStyle().set("border", "1px solid #2b376f");
        } else {
            link.getStyle().set("color", "var(--muted)");
        }
        
        Span iconSpan = new Span(icon);
        Span labelSpan = new Span(label);
        labelSpan.addClassName("label");
        
        link.add(iconSpan, labelSpan);
        return link;
    }
    
    /**
     * Check if current route matches the given route.
     */
    private boolean isCurrentRoute(String route) {
        String currentRoute = UI.getCurrent().getInternals().getActiveViewLocation().getPath();
        if (route.isEmpty()) {
            return currentRoute.isEmpty() || currentRoute.equals("/");
        }
        return currentRoute.equals("/" + route);
    }
    
    /**
     * Create the binary shield logo component.
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
}
