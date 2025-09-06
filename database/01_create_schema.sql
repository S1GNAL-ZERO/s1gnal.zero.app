-- S1GNAL.ZERO Database Schema Creation Script
-- Based on DETAILED_DESIGN.md Section 6.1
-- This script creates all tables, indexes, and constraints for the S1GNAL.ZERO system

-- Enable UUID extension (gen_random_uuid is built into modern PostgreSQL)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  -- Not needed for gen_random_uuid()

-- =============================================================================
-- TABLE CREATION
-- =============================================================================

-- Core user table with authentication and subscription tracking
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    
    -- Subscription management
    subscription_tier VARCHAR(20) DEFAULT 'FREE' CHECK (subscription_tier IN ('FREE', 'PRO', 'BUSINESS', 'ENTERPRISE')),
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    stripe_customer_id VARCHAR(255) UNIQUE,
    stripe_subscription_id VARCHAR(255),
    
    -- Usage tracking
    analyses_used_this_month INT DEFAULT 0,
    analyses_used_total INT DEFAULT 0,
    last_usage_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Referral system
    referral_code VARCHAR(20) UNIQUE,
    referred_by UUID REFERENCES users(id),
    referral_count INT DEFAULT 0,
    
    -- Account status
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    verification_token VARCHAR(255),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Analysis requests and results
CREATE TABLE IF NOT EXISTS analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    
    -- Query details
    query TEXT NOT NULL,
    query_type VARCHAR(50), -- 'product', 'influencer', 'stock', 'trend', 'event'
    platform VARCHAR(50), -- 'twitter', 'instagram', 'tiktok', 'reddit', 'amazon', 'all'
    
    -- Analysis results
    reality_score DECIMAL(5,2) CHECK (reality_score >= 0 AND reality_score <= 100),
    bot_percentage DECIMAL(5,2) CHECK (bot_percentage >= 0 AND bot_percentage <= 100),
    trend_score DECIMAL(5,2) CHECK (trend_score >= 0 AND trend_score <= 100),
    review_score DECIMAL(5,2) CHECK (review_score >= 0 AND review_score <= 100),
    promotion_score DECIMAL(5,2) CHECK (promotion_score >= 0 AND promotion_score <= 100),
    
    -- Manipulation classification
    manipulation_level VARCHAR(20) CHECK (manipulation_level IN ('GREEN', 'YELLOW', 'RED')),
    confidence_score DECIMAL(5,2) CHECK (confidence_score >= 0 AND confidence_score <= 100),
    
    -- Processing details
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETE', 'FAILED', 'TIMEOUT')),
    error_message TEXT,
    processing_time_ms INT,
    
    -- Solace messaging
    solace_correlation_id VARCHAR(255) UNIQUE,
    solace_request_topic VARCHAR(255),
    
    -- Visibility
    is_public BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false, -- For Wall of Shame
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Individual agent processing results
CREATE TABLE IF NOT EXISTS agent_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID REFERENCES analyses(id) ON DELETE CASCADE,
    
    -- Agent identification
    agent_type VARCHAR(50) NOT NULL CHECK (agent_type IN ('bot-detector', 'trend-analyzer', 'review-validator', 'paid-promotion', 'score-aggregator')),
    agent_version VARCHAR(20) DEFAULT '1.0.0',
    
    -- Results
    score DECIMAL(5,2),
    confidence DECIMAL(5,2),
    processing_time_ms INT,
    
    -- Evidence and details (JSON)
    evidence JSONB,
    data_sources JSONB, -- ["twitter_api", "mock_fallback"]
    raw_data JSONB, -- Store raw API responses for debugging
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETE', 'FAILED', 'TIMEOUT')),
    error_message TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Ensure one result per agent per analysis
    UNIQUE(analysis_id, agent_type)
);

-- Featured manipulated products/trends
CREATE TABLE IF NOT EXISTS wall_of_shame (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID REFERENCES analyses(id) ON DELETE CASCADE,
    
    -- Display information
    product_name VARCHAR(255) NOT NULL,
    company VARCHAR(255),
    category VARCHAR(100),
    
    -- Metrics
    bot_percentage DECIMAL(5,2) NOT NULL,
    reality_score DECIMAL(5,2) NOT NULL,
    manipulation_level VARCHAR(20) NOT NULL,
    
    -- Evidence summary
    evidence_summary TEXT,
    key_findings JSONB, -- ["62% bots", "127 undisclosed ads", "73% reviews in 3 days"]
    
    -- Engagement metrics
    views INT DEFAULT 0,
    shares INT DEFAULT 0,
    reports INT DEFAULT 0,
    
    -- Display control
    is_active BOOLEAN DEFAULT true,
    featured_until TIMESTAMP,
    display_order INT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment transactions
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    
    -- Payment details
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_type VARCHAR(50) NOT NULL, -- 'lifetime_founder', 'monthly', 'annual', 'one_time'
    
    -- Stripe integration
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_invoice_id VARCHAR(255),
    stripe_charge_id VARCHAR(255),
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'REFUNDED')),
    failure_reason TEXT,
    
    -- Metadata
    description TEXT,
    metadata JSONB,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    refunded_at TIMESTAMP
);

-- Email waitlist for launch
CREATE TABLE IF NOT EXISTS waitlist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    
    -- Referral tracking
    referral_code VARCHAR(20) UNIQUE,
    referred_by VARCHAR(20), -- referral_code of referrer
    referrals_count INT DEFAULT 0,
    
    -- Position and priority
    position INT,
    priority_access BOOLEAN DEFAULT false,
    skip_ahead_count INT DEFAULT 0, -- How many spots they've jumped
    
    -- Source tracking
    source VARCHAR(100), -- 'reddit', 'twitter', 'producthunt', 'direct', etc.
    utm_source VARCHAR(100),
    utm_medium VARCHAR(100),
    utm_campaign VARCHAR(100),
    
    -- Engagement
    email_verified BOOLEAN DEFAULT false,
    verification_token VARCHAR(255),
    emails_sent INT DEFAULT 0,
    emails_opened INT DEFAULT 0,
    
    -- Conversion
    converted_to_user BOOLEAN DEFAULT false,
    converted_at TIMESTAMP,
    user_id UUID REFERENCES users(id),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP
);

-- Track automated marketing activities
CREATE TABLE IF NOT EXISTS marketing_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Event details
    event_type VARCHAR(50) NOT NULL, -- 'tweet', 'reddit_post', 'email', 'discord_message'
    platform VARCHAR(50) NOT NULL,
    action VARCHAR(100), -- 'exposure_posted', 'waitlist_invite', 'conversion_email'
    
    -- Content
    content TEXT,
    url VARCHAR(500),
    hashtags JSONB, -- ["#NoMoreFOMO", "#AIDetection"]
    
    -- Related analysis
    analysis_id UUID REFERENCES analyses(id),
    product_exposed VARCHAR(255),
    
    -- Engagement metrics
    views INT DEFAULT 0,
    clicks INT DEFAULT 0,
    engagements INT DEFAULT 0,
    conversions INT DEFAULT 0,
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING',
    error_message TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    posted_at TIMESTAMP,
    
    -- Metadata
    metadata JSONB
);

-- User API keys for programmatic access
CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    
    -- Key details
    key_hash VARCHAR(255) UNIQUE NOT NULL, -- Store hashed version
    key_prefix VARCHAR(10) NOT NULL, -- First 10 chars for identification
    name VARCHAR(100),
    description TEXT,
    
    -- Permissions
    scopes JSONB, -- ["read:analyses", "write:analyses", "read:wall_of_shame"]
    rate_limit INT DEFAULT 100, -- Requests per hour
    
    -- Usage tracking
    last_used_at TIMESTAMP,
    usage_count INT DEFAULT 0,
    
    -- Status
    is_active BOOLEAN DEFAULT true,
    expires_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP
);

-- =============================================================================
-- INDEXES
-- =============================================================================

-- Indexes for users table
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_stripe_customer ON users(stripe_customer_id);
CREATE INDEX IF NOT EXISTS idx_users_referral_code ON users(referral_code);
CREATE INDEX IF NOT EXISTS idx_users_subscription_tier ON users(subscription_tier);

-- Indexes for analyses table
CREATE INDEX IF NOT EXISTS idx_analyses_user_id ON analyses(user_id);
CREATE INDEX IF NOT EXISTS idx_analyses_status ON analyses(status);
CREATE INDEX IF NOT EXISTS idx_analyses_created_at ON analyses(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_analyses_reality_score ON analyses(reality_score);
CREATE INDEX IF NOT EXISTS idx_analyses_bot_percentage ON analyses(bot_percentage);
CREATE INDEX IF NOT EXISTS idx_analyses_is_public ON analyses(is_public);
CREATE INDEX IF NOT EXISTS idx_analyses_correlation_id ON analyses(solace_correlation_id);

-- Indexes for agent_results table
CREATE INDEX IF NOT EXISTS idx_agent_results_analysis_id ON agent_results(analysis_id);
CREATE INDEX IF NOT EXISTS idx_agent_results_agent_type ON agent_results(agent_type);
CREATE INDEX IF NOT EXISTS idx_agent_results_status ON agent_results(status);

-- Indexes for wall_of_shame table
CREATE INDEX IF NOT EXISTS idx_wall_of_shame_active ON wall_of_shame(is_active);
CREATE INDEX IF NOT EXISTS idx_wall_of_shame_bot_percentage ON wall_of_shame(bot_percentage DESC);
CREATE INDEX IF NOT EXISTS idx_wall_of_shame_created_at ON wall_of_shame(created_at DESC);

-- Indexes for payments table
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_stripe_intent ON payments(stripe_payment_intent_id);

-- Indexes for waitlist table
CREATE INDEX IF NOT EXISTS idx_waitlist_email ON waitlist(email);
CREATE INDEX IF NOT EXISTS idx_waitlist_referral_code ON waitlist(referral_code);
CREATE INDEX IF NOT EXISTS idx_waitlist_position ON waitlist(position);
CREATE INDEX IF NOT EXISTS idx_waitlist_source ON waitlist(source);

-- Indexes for marketing_events table
CREATE INDEX IF NOT EXISTS idx_marketing_events_type ON marketing_events(event_type);
CREATE INDEX IF NOT EXISTS idx_marketing_events_platform ON marketing_events(platform);
CREATE INDEX IF NOT EXISTS idx_marketing_events_analysis ON marketing_events(analysis_id);

-- Indexes for api_keys table
CREATE INDEX IF NOT EXISTS idx_api_keys_user_id ON api_keys(user_id);
CREATE INDEX IF NOT EXISTS idx_api_keys_key_hash ON api_keys(key_hash);
CREATE INDEX IF NOT EXISTS idx_api_keys_is_active ON api_keys(is_active);

-- =============================================================================
-- ADDITIONAL CONSTRAINTS
-- =============================================================================

-- Check constraints
ALTER TABLE users 
    ADD CONSTRAINT IF NOT EXISTS check_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$');

ALTER TABLE analyses
    ADD CONSTRAINT IF NOT EXISTS check_processing_time 
    CHECK (processing_time_ms >= 0 AND processing_time_ms <= 60000); -- Max 60 seconds

-- Ensure scores are within valid range
ALTER TABLE agent_results
    ADD CONSTRAINT IF NOT EXISTS check_scores 
    CHECK (score >= 0 AND score <= 100 AND confidence >= 0 AND confidence <= 100);

-- =============================================================================
-- PERFORMANCE OPTIMIZATION
-- =============================================================================

-- Partial indexes for common queries
CREATE INDEX IF NOT EXISTS idx_analyses_pending ON analyses(id, user_id) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_analyses_high_bot ON analyses(bot_percentage) WHERE bot_percentage > 60;
CREATE INDEX IF NOT EXISTS idx_users_paid ON users(id) WHERE subscription_tier != 'FREE';

-- Schema creation complete
COMMENT ON DATABASE CURRENT_DATABASE() IS 'S1GNAL.ZERO - AI-Powered Authenticity Verification System';
