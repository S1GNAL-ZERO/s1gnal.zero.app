package io.signalzero.ui;

import com.vaadin.flow.shared.Registration;
import io.signalzero.model.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * S1GNAL.ZERO - Analysis Update Broadcaster
 * AGI Ventures Canada Hackathon 3.0 (September 6-7, 2025)
 * 
 * Real-time WebSocket broadcaster for pushing analysis updates to all connected UI clients.
 * Uses Vaadin @Push functionality to deliver instant notifications when:
 * - New analysis is completed
 * - Analysis status changes
 * - Reality Score™ updates are available
 * 
 * CRITICAL REQUIREMENTS (from CLAUDE.md):
 * - Production-ready implementation with proper error handling
 * - Thread-safe listener management
 * - Memory leak prevention with proper registration cleanup
 * - Works with @Push annotation in main application
 * 
 * Reference: DETAILED_DESIGN.md Section 11.3 - Real-time Updates
 */
public class AnalysisUpdateBroadcaster {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisUpdateBroadcaster.class);
    
    // Thread-safe set of listeners
    private static final LinkedHashSet<Consumer<Analysis>> listeners = new LinkedHashSet<>();
    
    // Executor for async broadcasting
    private static final Executor executor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "AnalysisUpdateBroadcaster");
        thread.setDaemon(true); // Don't prevent JVM shutdown
        return thread;
    });
    
    // Statistics
    private static volatile long totalBroadcasts = 0;
    private static volatile long totalListeners = 0;

    /**
     * Register a listener for analysis updates.
     * 
     * @param listener Consumer that will receive Analysis entities when updates occur
     * @return Registration object for cleanup - MUST be removed in onDetach()
     */
    public static synchronized Registration register(Consumer<Analysis> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        listeners.add(listener);
        totalListeners++;
        
        logger.debug("Registered analysis update listener. Total listeners: {}", listeners.size());
        
        // Return registration for cleanup
        return () -> {
            synchronized (AnalysisUpdateBroadcaster.class) {
                boolean removed = listeners.remove(listener);
                if (removed) {
                    logger.debug("Unregistered analysis update listener. Total listeners: {}", listeners.size());
                } else {
                    logger.warn("Attempted to unregister listener that was not found");
                }
            }
        };
    }

    /**
     * Broadcast analysis update to all registered listeners.
     * Called by AnalysisService when analysis completes or status changes.
     * 
     * @param analysis The updated Analysis entity
     */
    public static void broadcast(Analysis analysis) {
        if (analysis == null) {
            logger.warn("Attempted to broadcast null analysis - ignoring");
            return;
        }
        
        // Create snapshot of listeners to avoid ConcurrentModificationException
        Consumer<Analysis>[] currentListeners;
        synchronized (AnalysisUpdateBroadcaster.class) {
            if (listeners.isEmpty()) {
                logger.debug("No listeners registered for analysis broadcast: {}", analysis.getQuery());
                return;
            }
            
            currentListeners = listeners.toArray(new Consumer[0]);
            totalBroadcasts++;
        }
        
        logger.info("Broadcasting analysis update to {} listeners: {} (Reality Score: {}%)", 
                   currentListeners.length, analysis.getQuery(), analysis.getRealityScore());
        
        // Broadcast asynchronously to prevent blocking
        executor.execute(() -> {
            for (Consumer<Analysis> listener : currentListeners) {
                try {
                    listener.accept(analysis);
                } catch (Exception e) {
                    logger.error("Error broadcasting analysis update to listener", e);
                    // Continue with other listeners even if one fails
                }
            }
        });
    }

    /**
     * Broadcast analysis completion with detailed logging.
     * Special method for completed analyses with full results.
     * 
     * @param analysis Completed analysis entity
     */
    public static void broadcastCompletion(Analysis analysis) {
        if (analysis == null) {
            logger.warn("Attempted to broadcast null completed analysis");
            return;
        }
        
        logger.info("🎯 Broadcasting completed analysis: '{}' - {}% Reality Score, {}% bots, {} classification",
                   analysis.getQuery(), 
                   analysis.getRealityScore(),
                   analysis.getBotPercentage(),
                   analysis.getManipulationLevel().getDescription());
        
        broadcast(analysis);
    }

    /**
     * Broadcast analysis status change (e.g., PENDING -> PROCESSING -> COMPLETE).
     * 
     * @param analysis Analysis with updated status
     */
    public static void broadcastStatusChange(Analysis analysis) {
        if (analysis == null) {
            logger.warn("Attempted to broadcast null status change");
            return;
        }
        
        logger.debug("📡 Broadcasting status change: '{}' -> {}", 
                    analysis.getQuery(), analysis.getStatus());
        
        broadcast(analysis);
    }

    /**
     * Get current number of registered listeners.
     * Useful for monitoring and debugging.
     * 
     * @return Current listener count
     */
    public static synchronized int getListenerCount() {
        return listeners.size();
    }

    /**
     * Get total number of broadcasts sent since application start.
     * 
     * @return Total broadcast count
     */
    public static long getTotalBroadcasts() {
        return totalBroadcasts;
    }

    /**
     * Get total number of listeners that have been registered.
     * 
     * @return Total listener registrations
     */
    public static long getTotalListeners() {
        return totalListeners;
    }

    /**
     * Clear all listeners - used for testing or emergency cleanup.
     * WARNING: This will break real-time updates for all connected clients.
     */
    public static synchronized void clearAllListeners() {
        int count = listeners.size();
        listeners.clear();
        logger.warn("Cleared all {} analysis update listeners", count);
    }

    /**
     * Get broadcaster statistics for monitoring dashboard.
     * 
     * @return Statistics object with current metrics
     */
    public static BroadcasterStats getStats() {
        return new BroadcasterStats(
            getListenerCount(),
            getTotalBroadcasts(), 
            getTotalListeners()
        );
    }

    /**
     * Statistics holder class for monitoring.
     */
    public static class BroadcasterStats {
        public final int activeListeners;
        public final long totalBroadcasts;
        public final long totalListeners;
        
        public BroadcasterStats(int activeListeners, long totalBroadcasts, long totalListeners) {
            this.activeListeners = activeListeners;
            this.totalBroadcasts = totalBroadcasts;
            this.totalListeners = totalListeners;
        }
        
        @Override
        public String toString() {
            return String.format("BroadcasterStats{active=%d, broadcasts=%d, total=%d}", 
                                activeListeners, totalBroadcasts, totalListeners);
        }
    }
}
