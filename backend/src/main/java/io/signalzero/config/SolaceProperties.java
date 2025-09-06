package io.signalzero.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Solace PubSub+ connection properties
 * Maps to application.properties with prefix "solace"
 */
@Component
@ConfigurationProperties(prefix = "solace")
public class SolaceProperties {
    
    private String host = "tcp://localhost:55555";
    private String username = "admin";
    private String password = "admin";
    private String vpnName = "default";
    private boolean compressionEnabled = false;
    private int reconnectRetries = 3;
    private int connectTimeoutMs = 30000;
    private int readTimeoutMs = 30000;
    private int keepAliveIntervalMs = 3000;
    private int pubWindowSize = 255;
    
    // Connection pool settings
    private int maxConnections = 10;
    private int connectionTimeout = 5000;
    
    // Message settings
    private boolean persistentMode = true;
    private long messageTimeToLiveMs = 300000; // 5 minutes
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getVpnName() {
        return vpnName;
    }
    
    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }
    
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }
    
    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }
    
    public int getReconnectRetries() {
        return reconnectRetries;
    }
    
    public void setReconnectRetries(int reconnectRetries) {
        this.reconnectRetries = reconnectRetries;
    }
    
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }
    
    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
    
    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
    
    public int getKeepAliveIntervalMs() {
        return keepAliveIntervalMs;
    }
    
    public void setKeepAliveIntervalMs(int keepAliveIntervalMs) {
        this.keepAliveIntervalMs = keepAliveIntervalMs;
    }
    
    public int getPubWindowSize() {
        return pubWindowSize;
    }
    
    public void setPubWindowSize(int pubWindowSize) {
        this.pubWindowSize = pubWindowSize;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public boolean isPersistentMode() {
        return persistentMode;
    }
    
    public void setPersistentMode(boolean persistentMode) {
        this.persistentMode = persistentMode;
    }
    
    public long getMessageTimeToLiveMs() {
        return messageTimeToLiveMs;
    }
    
    public void setMessageTimeToLiveMs(long messageTimeToLiveMs) {
        this.messageTimeToLiveMs = messageTimeToLiveMs;
    }
}
