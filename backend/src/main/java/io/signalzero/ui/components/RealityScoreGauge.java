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
        
        // Set up component-specific CSS variables for self-contained styling
        setupGaugeThemeVariables();
        
        createGaugeStructure();
        setInitialScore();
    }

    /**
     * Set up CSS custom properties specific to this gauge component.
     */
    private void setupGaugeThemeVariables() {
        getElement().getStyle().set("--ink", "#e8ecff");
        getElement().getStyle().set("--muted", "#a6b0d8");
        getElement().getStyle().set("--ok", "#10b981");
        getElement().getStyle().set("--warn", "#f59e0b");
        getElement().getStyle().set("--bad", "#ef4444");
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
        
        // Create enhanced SVG markup with advanced graphics and animations
        String svgContent = String.format("""
            <svg width="%d" height="%d" class="gauge-svg" style="overflow: visible;">
                <defs>
                    <!-- Gradient definitions -->
                    <linearGradient id="gaugeGradient" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                        <stop offset="0%%" style="stop-color:#667eea;stop-opacity:0.8" />
                        <stop offset="50%%" style="stop-color:#764ba2;stop-opacity:0.6" />
                        <stop offset="100%%" style="stop-color:#667eea;stop-opacity:0.4" />
                    </linearGradient>
                    
                    <!-- Progress gradient -->
                    <linearGradient id="progressGradient" x1="0%%" y1="0%%" x2="100%%" y2="0%%">
                        <stop offset="0%%" style="stop-color:#10b981;stop-opacity:1" />
                        <stop offset="50%%" style="stop-color:#f59e0b;stop-opacity:1" />
                        <stop offset="100%%" style="stop-color:#ef4444;stop-opacity:1" />
                    </linearGradient>
                    
                    <!-- Animated gradient for processing state -->
                    <linearGradient id="processingGradient" x1="0%%" y1="0%%" x2="100%%" y2="0%%">
                        <stop offset="0%%" style="stop-color:#667eea;stop-opacity:0.3">
                            <animate attributeName="stop-opacity" values="0.3;0.8;0.3" dur="2s" repeatCount="indefinite"/>
                        </stop>
                        <stop offset="50%%" style="stop-color:#764ba2;stop-opacity:0.5">
                            <animate attributeName="stop-opacity" values="0.5;1;0.5" dur="2s" repeatCount="indefinite"/>
                        </stop>
                        <stop offset="100%%" style="stop-color:#667eea;stop-opacity:0.3">
                            <animate attributeName="stop-opacity" values="0.3;0.8;0.3" dur="2s" repeatCount="indefinite"/>
                        </stop>
                    </linearGradient>
                    
                    <!-- Glow filter -->
                    <filter id="glow" x="-50%%" y="-50%%" width="200%%" height="200%%">
                        <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                        <feMerge> 
                            <feMergeNode in="coloredBlur"/>
                            <feMergeNode in="SourceGraphic"/>
                        </feMerge>
                    </filter>
                    
                    <!-- Drop shadow filter -->
                    <filter id="dropshadow" x="-50%%" y="-50%%" width="200%%" height="200%%">
                        <feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="#000" flood-opacity="0.3"/>
                    </filter>
                </defs>
                
                <!-- Animated background rings -->
                <circle cx="%d" cy="%d" r="%d" 
                        stroke="url(#gaugeGradient)" 
                        stroke-width="1" 
                        fill="none"
                        opacity="0.3">
                    <animate attributeName="r" values="%d;%d;%d" dur="4s" repeatCount="indefinite"/>
                    <animate attributeName="opacity" values="0.3;0.1;0.3" dur="4s" repeatCount="indefinite"/>
                </circle>
                
                <circle cx="%d" cy="%d" r="%d" 
                        stroke="url(#gaugeGradient)" 
                        stroke-width="1" 
                        fill="none"
                        opacity="0.2">
                    <animate attributeName="r" values="%d;%d;%d" dur="6s" repeatCount="indefinite"/>
                    <animate attributeName="opacity" values="0.2;0.05;0.2" dur="6s" repeatCount="indefinite"/>
                </circle>
                
                <!-- Main background circle with enhanced styling -->
                <circle cx="%d" cy="%d" r="%d" 
                        class="gauge-circle" 
                        stroke="rgba(232, 236, 255, 0.15)" 
                        stroke-width="%d" 
                        fill="none"
                        filter="url(#dropshadow)"/>
                        
                <!-- Secondary background circle for depth -->
                <circle cx="%d" cy="%d" r="%d" 
                        stroke="rgba(232, 236, 255, 0.05)" 
                        stroke-width="2" 
                        fill="none"/>
                        
                <!-- Progress circle with enhanced styling -->
                <circle cx="%d" cy="%d" r="%d" 
                        id="progress-circle"
                        stroke="url(#progressGradient)" 
                        stroke-width="%d" 
                        fill="none"
                        stroke-dasharray="%.1f"
                        stroke-dashoffset="%.1f"
                        stroke-linecap="round"
                        transform="rotate(-90 %d %d)"
                        filter="url(#glow)"
                        opacity="0.9">
                    <!-- Smooth transition animation -->
                    <animate attributeName="stroke-dashoffset" 
                             id="progress-animation"
                             dur="1.5s" 
                             fill="freeze"
                             calcMode="spline"
                             keySplines="0.4 0 0.2 1"
                             keyTimes="0;1"/>
                </circle>
                
                <!-- Animated tick marks around the gauge -->
                <g id="tick-marks" stroke="rgba(232, 236, 255, 0.3)" stroke-width="2">
                    <!-- Major ticks at 0, 25, 50, 75, 100 -->
                    <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(0 %d %d)"/>
                    <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(90 %d %d)"/>
                    <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(180 %d %d)"/>
                    <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(270 %d %d)"/>
                    
                    <!-- Minor ticks -->
                    <g stroke-width="1" opacity="0.5">
                        <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(45 %d %d)"/>
                        <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(135 %d %d)"/>
                        <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(225 %d %d)"/>
                        <line x1="%d" y1="%d" x2="%d" y2="%d" transform="rotate(315 %d %d)"/>
                    </g>
                </g>
            </svg>
            """,
            GAUGE_SIZE, GAUGE_SIZE,
            
            // Animated background rings
            GAUGE_SIZE/2, GAUGE_SIZE/2, RADIUS + 20, RADIUS + 20, RADIUS + 35, RADIUS + 20,
            GAUGE_SIZE/2, GAUGE_SIZE/2, RADIUS + 30, RADIUS + 30, RADIUS + 50, RADIUS + 30,
            
            // Main circles
            GAUGE_SIZE/2, GAUGE_SIZE/2, RADIUS, STROKE_WIDTH,
            GAUGE_SIZE/2, GAUGE_SIZE/2, RADIUS - 4,
            GAUGE_SIZE/2, GAUGE_SIZE/2, RADIUS, STROKE_WIDTH,
            CIRCUMFERENCE, CIRCUMFERENCE, // Initial state: fully hidden
            GAUGE_SIZE/2, GAUGE_SIZE/2,
            
            // Major tick marks (outer edge)
            GAUGE_SIZE/2, 20, GAUGE_SIZE/2, 30, GAUGE_SIZE/2, GAUGE_SIZE/2,
            GAUGE_SIZE - 20, GAUGE_SIZE/2, GAUGE_SIZE - 30, GAUGE_SIZE/2, GAUGE_SIZE/2, GAUGE_SIZE/2,
            GAUGE_SIZE/2, GAUGE_SIZE - 20, GAUGE_SIZE/2, GAUGE_SIZE - 30, GAUGE_SIZE/2, GAUGE_SIZE/2,
            20, GAUGE_SIZE/2, 30, GAUGE_SIZE/2, GAUGE_SIZE/2, GAUGE_SIZE/2,
            
            // Minor tick marks (shorter)
            GAUGE_SIZE/2, 25, GAUGE_SIZE/2, 32, GAUGE_SIZE/2, GAUGE_SIZE/2,
            GAUGE_SIZE - 25, GAUGE_SIZE/2, GAUGE_SIZE - 32, GAUGE_SIZE/2, GAUGE_SIZE/2, GAUGE_SIZE/2,
            GAUGE_SIZE/2, GAUGE_SIZE - 25, GAUGE_SIZE/2, GAUGE_SIZE - 32, GAUGE_SIZE/2, GAUGE_SIZE/2,
            25, GAUGE_SIZE/2, 32, GAUGE_SIZE/2, GAUGE_SIZE/2, GAUGE_SIZE/2
        );
        
        svgContainer.getElement().setProperty("innerHTML", svgContent);
        
        // Add advanced CSS animations
        svgContainer.getElement().executeJs("""
            if (!document.querySelector('#advanced-gauge-animations')) {
                const style = document.createElement('style');
                style.id = 'advanced-gauge-animations';
                style.textContent = `
                    @keyframes gaugeGlow {
                        0%, 100% { 
                            filter: drop-shadow(0 0 5px rgba(102, 126, 234, 0.3)); 
                        }
                        50% { 
                            filter: drop-shadow(0 0 15px rgba(102, 126, 234, 0.6)); 
                        }
                    }
                    
                    @keyframes tickFade {
                        0%, 100% { opacity: 0.3; }
                        50% { opacity: 0.8; }
                    }
                    
                    .gauge-svg {
                        animation: gaugeGlow 3s ease-in-out infinite;
                    }
                    
                    #tick-marks {
                        animation: tickFade 4s ease-in-out infinite;
                    }
                    
                    .reality-score-gauge {
                        transition: transform 0.3s ease;
                    }
                    
                    .reality-score-gauge:hover {
                        transform: scale(1.02);
                    }
                `;
                document.head.appendChild(style);
            }
            """);
        
        // Score text in center - Enhanced visibility
        Div textContainer = new Div();
        textContainer.getStyle().set("position", "absolute");
        textContainer.getStyle().set("top", "50%");
        textContainer.getStyle().set("left", "50%");
        textContainer.getStyle().set("transform", "translate(-50%, -50%)");
        textContainer.getStyle().set("text-align", "center");
        textContainer.getStyle().set("pointer-events", "none");
        textContainer.getStyle().set("z-index", "100"); // Much higher z-index
        textContainer.getStyle().set("width", "120px"); // Fixed width for better centering
        textContainer.getStyle().set("height", "120px"); // Fixed height
        textContainer.getStyle().set("display", "flex");
        textContainer.getStyle().set("flex-direction", "column");
        textContainer.getStyle().set("justify-content", "center");
        textContainer.getStyle().set("align-items", "center");
        
        // Add a semi-transparent background circle for better text visibility
        Div backgroundCircle = new Div();
        backgroundCircle.getStyle().set("position", "absolute");
        backgroundCircle.getStyle().set("width", "100px");
        backgroundCircle.getStyle().set("height", "100px");
        backgroundCircle.getStyle().set("border-radius", "50%");
        backgroundCircle.getStyle().set("background", "rgba(0, 0, 0, 0.4)");
        backgroundCircle.getStyle().set("backdrop-filter", "blur(4px)");
        backgroundCircle.getStyle().set("z-index", "-1");
        textContainer.add(backgroundCircle);
        
        scoreText = new Span("--");
        scoreText.addClassName("gauge-text");
        scoreText.getStyle().set("display", "block");
        scoreText.getStyle().set("color", "#ffffff");  // Pure white for maximum contrast
        scoreText.getStyle().set("font-size", "2.2rem");  // Slightly larger
        scoreText.getStyle().set("font-weight", "900");  
        scoreText.getStyle().set("line-height", "1");
        scoreText.getStyle().set("text-shadow", "0 0 20px rgba(255,255,255,0.5), 0 2px 8px rgba(0,0,0,1)"); // Glowing effect
        scoreText.getStyle().set("letter-spacing", "-0.02em");
        scoreText.getStyle().set("margin-bottom", "2px");
        
        labelText = new Span("Reality Score™");
        labelText.addClassName("gauge-label");
        labelText.getStyle().set("display", "block");
        labelText.getStyle().set("color", "#e8ecff");  // High contrast color
        labelText.getStyle().set("font-size", "0.7rem");
        labelText.getStyle().set("margin-top", "0px");
        labelText.getStyle().set("font-weight", "600"); // Bolder
        labelText.getStyle().set("text-shadow", "0 0 10px rgba(232,236,255,0.5), 0 1px 4px rgba(0,0,0,1)");
        labelText.getStyle().set("text-transform", "uppercase");
        labelText.getStyle().set("letter-spacing", "0.05em");
        
        botPercentageText = new Span("");
        botPercentageText.getStyle().set("display", "block");
        botPercentageText.getStyle().set("color", "#ff6b6b");  // Brighter red
        botPercentageText.getStyle().set("font-size", "0.6rem");
        botPercentageText.getStyle().set("margin-top", "2px");
        botPercentageText.getStyle().set("font-weight", "700"); // Bolder
        botPercentageText.getStyle().set("text-shadow", "0 0 8px rgba(255,107,107,0.5), 0 1px 3px rgba(0,0,0,0.8)");
        botPercentageText.getStyle().set("text-transform", "uppercase");
        botPercentageText.getStyle().set("letter-spacing", "0.03em");
        
        textContainer.add(scoreText, labelText, botPercentageText);
        
        svgContainer.add(textContainer);
        add(svgContainer);
    }

    /**
     * Set initial score display.
     */
    private void setInitialScore() {
        scoreText.setText("--");
        botPercentageText.setText("READY");
        labelText.setText("Reality Score™");
        
        // Set initial colors
        scoreText.getStyle().set("color", "#a6b0d8"); // Muted for inactive state
        botPercentageText.getStyle().set("color", "#667eea"); // Themed color for ready state
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
        
        // Update gauge color and progress arc
        updateGaugeColor(level);
        updateProgressArc(currentScore, level);
    }

    /**
     * Set processing state with enhanced visual feedback.
     */
    public void setProcessing(boolean processing) {
        this.isProcessing = processing;
        
        if (processing) {
            scoreText.setText("••••");
            scoreText.getStyle().set("color", "#667eea"); // Processing color
            botPercentageText.setText("ANALYZING");
            botPercentageText.getStyle().set("color", "#667eea");
            labelText.setText("PROCESSING");
            
            // Add pulsing animation for processing state
            scoreText.getElement().executeJs("""
                this.style.animation = 'pulse 1.5s ease-in-out infinite';
                if (!document.querySelector('#processing-pulse-animation')) {
                    const style = document.createElement('style');
                    style.id = 'processing-pulse-animation';
                    style.textContent = `
                        @keyframes pulse {
                            0%, 100% { opacity: 0.6; transform: scale(1); }
                            50% { opacity: 1; transform: scale(1.05); }
                        }
                    `;
                    document.head.appendChild(style);
                }
                """);
        } else {
            // Remove pulsing animation
            scoreText.getElement().executeJs("this.style.animation = '';");
            
            if (currentScore > 0) {
                scoreText.setText(currentScore + "%");
                scoreText.getStyle().set("color", "#ffffff"); // Back to white
                botPercentageText.setText(currentBotPercentage + "% BOTS");
                botPercentageText.getStyle().set("color", "#ff6b6b");
                labelText.setText("Reality Score™");
            } else {
                setInitialScore();
            }
        }
    }


    /**
     * Update gauge color based on manipulation level.
     */
    private void updateGaugeColor(ManipulationLevel level) {
        switch (level) {
            case GREEN:
                removeClassName("gauge-yellow");
                removeClassName("gauge-red");
                addClassName("gauge-green");
                break;
            case YELLOW:
                removeClassName("gauge-green");
                removeClassName("gauge-red");
                addClassName("gauge-yellow");
                break;
            case RED:
            default:
                removeClassName("gauge-green");
                removeClassName("gauge-yellow");
                addClassName("gauge-red");
                break;
        }
    }

    /**
     * Update the progress arc to show the current score.
     */
    private void updateProgressArc(int score, ManipulationLevel level) {
        // Calculate the stroke-dashoffset based on the score percentage
        double progress = score / 100.0;
        double offset = CIRCUMFERENCE - (progress * CIRCUMFERENCE);
        
        // Determine color based on manipulation level
        String strokeColor;
        switch (level) {
            case GREEN:
                strokeColor = "#10b981"; // Green
                break;
            case YELLOW:
                strokeColor = "#f59e0b"; // Yellow
                break;
            case RED:
            default:
                strokeColor = "#ef4444"; // Red
                break;
        }
        
        // Use JavaScript to update the SVG progress circle
        String jsCommand = String.format("""
            const progressCircle = this.querySelector('#progress-circle');
            if (progressCircle) {
                progressCircle.setAttribute('stroke-dashoffset', '%.1f');
                progressCircle.setAttribute('stroke', '%s');
            }
            """, offset, strokeColor);
            
        getElement().executeJs(jsCommand);
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
    }

    // Getters for current state
    public int getCurrentScore() { return currentScore; }
    public int getCurrentBotPercentage() { return currentBotPercentage; }
    public ManipulationLevel getCurrentLevel() { return currentLevel; }
    public boolean isProcessing() { return isProcessing; }
}
