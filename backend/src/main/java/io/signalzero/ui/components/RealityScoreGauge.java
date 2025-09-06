package io.signalzero.ui.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import io.signalzero.model.ManipulationLevel;

/**
 * S1GNAL.ZERO - Reality Score™ Gauge Component
 * AGI Ventures Canada Hackathon 3.0 (September 6-7, 2025)
 * 
 * Interactive circular gauge displaying Reality Score™ with color-coded zones:
 * - GREEN (67-100%): Authentic engagement
 * - YELLOW (34-66%): Mixed signals  
 * - RED (0-33%): Heavily manipulated
 * 
 * CRITICAL REQUIREMENTS (from CLAUDE.md):
 * - Production-ready SVG implementation
 * - Smooth animations for score updates
 * - Color coding matching ManipulationLevel enum
 * - Processing state with loading animation
 * 
 * Reference: DETAILED_DESIGN.md Section 11.1 - UI Components
 */
public class RealityScoreGauge extends Div {

    private static final int GAUGE_SIZE = 200;
    private static final int STROKE_WIDTH = 8;
    private static final int RADIUS = (GAUGE_SIZE - STROKE_WIDTH) / 2 - 10;
    private static final double CIRCUMFERENCE = 2 * Math.PI * RADIUS;

    // UI Components
    private Div svgContainer;
    private Span scoreText;
    private Span labelText;
    private Span botPercentageText;
    
    // Current state
    private int currentScore = 0;
    private int currentBotPercentage = 0;
    private ManipulationLevel currentLevel = ManipulationLevel.YELLOW;
    private boolean isProcessing = false;

    /**
     * Initialize the Reality Score gauge component.
     */
    public RealityScoreGauge() {
        addClassName("reality-score-gauge");
        setWidth(GAUGE_SIZE + "px");
        setHeight(GAUGE_SIZE + "px");
        
        createGaugeStructure();
        setInitialScore();
    }

    /**
     * Create the SVG gauge structure with all visual elements.
     */
    private void createGaugeStructure() {
        // Main container
        svgContainer = new Div();
        svgContainer.getStyle().set("position", "relative");
        svgContainer.getStyle().set("width", "100%");
        svgContainer.getStyle().set("height", "100%");
        
        // Create SVG markup as HTML string (Vaadin Flow approach)
        String svgContent = String.format("""
            <svg width="%d" height="%d" class="gauge-svg">
                <!-- Background circle -->
                <circle cx="%d" cy="%d" r="%d" 
                        class="gauge-circle" 
                        stroke="var(--lumo-contrast-20pct)" 
                        stroke-width="%d" 
                        fill="none"/>
                        
                <!-- Progress circle -->
                <circle cx="%d" cy="%d" r="%d" 
                        class="gauge-progress" 
                        stroke="var(--s1gnal-primary)" 
                        stroke-width="%d" 
                        fill="none"
                        stroke-linecap="round"
                        stroke-dasharray="%.2f"
                        stroke-dashoffset="%.2f"
                        transform="rotate(-90 %d %d)"
                        style="transition: stroke-dashoffset 1s ease-in-out, stroke 0.5s ease;"/>
                        
                <!-- Center dot -->
                <circle cx="%d" cy="%d" r="4" 
                        fill="var(--s1gnal-primary)"/>
            </svg>
            """,
            GAUGE_SIZE, GAUGE_SIZE,
            GAUGE_SIZE/2, GAUGE_SIZE/2, RADIUS, STROKE_WIDTH,
            GAUGE_SIZE/2, GAUGE_SIZE/2, RADIUS, STROKE_WIDTH,
            CIRCUMFERENCE, CIRCUMFERENCE, // Initial: no progress
            GAUGE_SIZE/2, GAUGE_SIZE/2,
            GAUGE_SIZE/2, GAUGE_SIZE/2
        );
        
        svgContainer.getElement().setProperty("innerHTML", svgContent);
        
        // Score text in center
        Div textContainer = new Div();
        textContainer.getStyle().set("position", "absolute");
        textContainer.getStyle().set("top", "50%");
        textContainer.getStyle().set("left", "50%");
        textContainer.getStyle().set("transform", "translate(-50%, -50%)");
        textContainer.getStyle().set("text-align", "center");
        textContainer.getStyle().set("pointer-events", "none");
        
        scoreText = new Span("--");
        scoreText.addClassName("gauge-text");
        scoreText.getStyle().set("display", "block");
        scoreText.getStyle().set("color", "var(--s1gnal-text)");
        scoreText.getStyle().set("font-size", "2.5rem");
        scoreText.getStyle().set("font-weight", "bold");
        scoreText.getStyle().set("line-height", "1");
        
        labelText = new Span("Reality Score™");
        labelText.addClassName("gauge-label");
        labelText.getStyle().set("display", "block");
        labelText.getStyle().set("color", "var(--s1gnal-text-secondary)");
        labelText.getStyle().set("font-size", "0.8rem");
        labelText.getStyle().set("margin-top", "0.25rem");
        
        botPercentageText = new Span("");
        botPercentageText.getStyle().set("display", "block");
        botPercentageText.getStyle().set("color", "var(--s1gnal-error)");
        botPercentageText.getStyle().set("font-size", "0.7rem");
        botPercentageText.getStyle().set("margin-top", "0.25rem");
        botPercentageText.getStyle().set("font-weight", "bold");
        
        textContainer.add(scoreText, labelText, botPercentageText);
        
        svgContainer.add(textContainer);
        add(svgContainer);
    }

    /**
     * Set initial score display.
     */
    private void setInitialScore() {
        scoreText.setText("--");
        botPercentageText.setText("Ready to analyze");
        updateGaugeColor(ManipulationLevel.YELLOW);
    }

    /**
     * Update the gauge with new analysis results.
     * 
     * @param realityScore Reality Score (0-100)
     * @param botPercentage Bot percentage (0-100)  
     * @param level Manipulation level for color coding
     */
    public void updateScore(int realityScore, int botPercentage, ManipulationLevel level) {
        this.currentScore = Math.max(0, Math.min(100, realityScore));
        this.currentBotPercentage = Math.max(0, Math.min(100, botPercentage));
        this.currentLevel = level;
        this.isProcessing = false;
        
        // Update text displays
        scoreText.setText(currentScore + "%");
        botPercentageText.setText(currentBotPercentage + "% bots detected");
        
        // Update gauge visual
        updateGaugeProgress(currentScore);
        updateGaugeColor(level);
        
        // Add completion animation
        addClassName("gauge-complete");
        getElement().executeJs("setTimeout(() => { $0.classList.remove('gauge-complete'); }, 1000)", getElement());
    }

    /**
     * Set processing state with loading animation.
     */
    public void setProcessing(boolean processing) {
        this.isProcessing = processing;
        
        if (processing) {
            scoreText.setText("••••");
            botPercentageText.setText("AI agents analyzing...");
            labelText.setText("Processing");
            
            // Add loading animation to the progress circle
            getElement().executeJs("""
                const circle = $0.querySelector('.gauge-progress');
                if (circle) {
                    circle.style.animation = 'spin 2s linear infinite';
                    circle.style.strokeDasharray = '%f';
                    circle.style.strokeDashoffset = '%f';
                }
                """, getElement(), CIRCUMFERENCE * 0.25, CIRCUMFERENCE * 0.75);
            
        } else {
            // Remove loading animation
            getElement().executeJs("""
                const circle = $0.querySelector('.gauge-progress');
                if (circle) {
                    circle.style.animation = '';
                }
                """, getElement());
            
            if (currentScore > 0) {
                scoreText.setText(currentScore + "%");
                botPercentageText.setText(currentBotPercentage + "% bots detected");
                labelText.setText("Reality Score™");
            } else {
                setInitialScore();
            }
        }
    }

    /**
     * Update the gauge progress arc based on score.
     */
    private void updateGaugeProgress(int score) {
        double progressPercentage = score / 100.0;
        double dashOffset = CIRCUMFERENCE * (1 - progressPercentage);
        
        // Update the SVG circle stroke-dashoffset via JavaScript
        getElement().executeJs("""
            const circle = $0.querySelector('.gauge-progress');
            if (circle) {
                circle.style.strokeDasharray = '%f';
                circle.style.strokeDashoffset = '%f';
            }
            """, getElement(), CIRCUMFERENCE, dashOffset);
    }

    /**
     * Update gauge color based on manipulation level.
     */
    private void updateGaugeColor(ManipulationLevel level) {
        String colorVar;
        String glowColor;
        
        switch (level) {
            case GREEN:
                colorVar = "var(--s1gnal-success)";
                glowColor = "rgba(76, 175, 80, 0.4)";
                removeClassName("gauge-yellow");
                removeClassName("gauge-red");
                addClassName("gauge-green");
                break;
            case YELLOW:
                colorVar = "var(--s1gnal-warning)";
                glowColor = "rgba(255, 152, 0, 0.4)";
                removeClassName("gauge-green");
                removeClassName("gauge-red");
                addClassName("gauge-yellow");
                break;
            case RED:
            default:
                colorVar = "var(--s1gnal-error)";
                glowColor = "rgba(244, 67, 54, 0.4)";
                removeClassName("gauge-green");
                removeClassName("gauge-yellow");
                addClassName("gauge-red");
                break;
        }
        
        // Update the SVG circle stroke color via JavaScript
        getElement().executeJs("""
            const circle = $0.querySelector('.gauge-progress');
            if (circle) {
                circle.style.stroke = '%s';
                circle.style.filter = 'drop-shadow(0 0 8px %s)';
            }
            """, getElement(), colorVar, glowColor);
    }

    /**
     * Reset gauge to initial state.
     */
    public void reset() {
        currentScore = 0;
        currentBotPercentage = 0;
        currentLevel = ManipulationLevel.YELLOW;
        isProcessing = false;
        
        setInitialScore();
        updateGaugeProgress(0);
    }

    // Getters for current state
    public int getCurrentScore() { return currentScore; }
    public int getCurrentBotPercentage() { return currentBotPercentage; }
    public ManipulationLevel getCurrentLevel() { return currentLevel; }
    public boolean isProcessing() { return isProcessing; }
}
