package io.signalzero;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * S1GNAL.ZERO - AI-Powered Authenticity Verification System
 * 
 * Main Spring Boot application class with Vaadin Flow integration.
 * Built for AGI Ventures Canada Hackathon 3.0 (September 6-7, 2025).
 * 
 * Key Features:
 * - Real-time multi-agent analysis system
 * - Event-driven architecture with Solace PubSub+
 * - Server-side Java UI with Vaadin Flow
 * - Reality Score™ calculation with weighted algorithms
 * 
 * CRITICAL REQUIREMENTS (from CLAUDE.md):
 * - @Push annotation enables real-time WebSocket updates
 * - NO PLACEHOLDERS - all code must be production ready
 * - NO TODO COMMENTS - complete implementations only
 * - Repository pattern with direct JPA entity binding
 */
@SpringBootApplication
@EnableJpaRepositories("io.signalzero.repository")
@EnableAsync
@EnableScheduling
@Push // REQUIRED for real-time updates (CLAUDE.md requirement)
@Theme(variant = Lumo.DARK)
@PWA(
    name = "S1GNAL.ZERO",
    shortName = "S1GNAL.ZERO",
    description = "AI-Powered Authenticity Verification System - Detect manufactured viral trends and bot manipulation in real-time",
    iconPath = "images/logo.png",
    backgroundColor = "#1a1a1a",
    themeColor = "#ff6b35"
)
public class SignalZeroApplication implements AppShellConfigurator {

    /**
     * Main entry point for the S1GNAL.ZERO application.
     * 
     * Starts the Spring Boot application with embedded Tomcat server
     * on port 8081 (configured in application.properties).
     * 
     * The application includes:
     * - Vaadin Flow server-side UI with real-time push updates
     * - Solace PubSub+ event broker integration
     * - Multi-agent AI analysis system
     * - Supabase database with comprehensive schema
     * - Stripe payment processing
     * - Reality Score™ calculation engine
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system properties for optimal performance
        System.setProperty("vaadin.launch-browser", "false");
        System.setProperty("spring.devtools.restart.enabled", "false");
        
        // Start the Spring Boot application
        SpringApplication application = new SpringApplication(SignalZeroApplication.class);
        
        // Set default profile if none specified
        application.setDefaultProperties(java.util.Map.of(
            "spring.profiles.default", "dev"
        ));
        
        application.run(args);
        
        // Log startup completion
        System.out.println("\n" +
            "=================================================================\n" +
            "   S1GNAL.ZERO - AI-Powered Authenticity Verification System    \n" +
            "   AGI Ventures Canada Hackathon 3.0 | September 6-7, 2025     \n" +
            "=================================================================\n" +
            "   🌐 Application: http://localhost:8081                        \n" +
            "   📊 Solace Admin: http://localhost:8080                       \n" +
            "   🤖 Multi-Agent System: Ready for real-time analysis          \n" +
            "   💾 Database: Supabase with 8 tables, 3 views, 2 functions   \n" +
            "   ⚡ WebSocket: Push notifications enabled                      \n" +
            "   🎯 Demo Values: Stanley Cup = 62% bots, 34% Reality Score    \n" +
            "=================================================================\n"
        );
    }
}
