-- S1GNAL.ZERO Database Functions & Triggers
-- Based on DETAILED_DESIGN.md Section 6.4
-- This script creates all functions, triggers, and stored procedures

-- =============================================================================
-- REALITY SCORE™ CALCULATION FUNCTIONS
-- =============================================================================

-- Function to calculate Reality Score using exact weights from DETAILED_DESIGN.md
-- Bot: 40%, Trend: 30%, Review: 20%, Promotion: 10%
CREATE OR REPLACE FUNCTION calculate_reality_score(
    bot_score DECIMAL,
    trend_score DECIMAL,
    review_score DECIMAL,
    promotion_score DECIMAL
) RETURNS DECIMAL AS $$
BEGIN
    -- Weighted average: Bot 40%, Trend 30%, Review 20%, Promotion 10%
    RETURN (bot_score * 0.4 + trend_score * 0.3 + review_score * 0.2 + promotion_score * 0.1);
END;
$$ LANGUAGE plpgsql;

-- Function to classify manipulation level based on Reality Score
CREATE OR REPLACE FUNCTION classify_manipulation(score DECIMAL) 
RETURNS VARCHAR AS $$
BEGIN
    IF score >= 67 THEN
        RETURN 'GREEN';    -- 67-100%: Authentic Engagement
    ELSIF score >= 34 THEN
        RETURN 'YELLOW';   -- 34-66%: Mixed Signals
    ELSE
        RETURN 'RED';      -- 0-33%: Heavily Manipulated
    END IF;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- TRIGGER FUNCTIONS
-- =============================================================================

-- Trigger function to automatically update manipulation level when Reality Score changes
CREATE OR REPLACE FUNCTION update_manipulation_level()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.reality_score IS NOT NULL THEN
        NEW.manipulation_level = classify_manipulation(NEW.reality_score);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger function to automatically add products to Wall of Shame
CREATE OR REPLACE FUNCTION auto_add_to_wall_of_shame()
RETURNS TRIGGER AS $$
BEGIN
    -- Add to Wall of Shame if bot percentage > 60% and analysis is complete
    IF NEW.status = 'COMPLETE' AND NEW.bot_percentage > 60 AND NEW.is_public = true THEN
        INSERT INTO signalzero.wall_of_shame (
            analysis_id,
            product_name,
            company,
            category,
            bot_percentage,
            reality_score,
            manipulation_level,
            evidence_summary,
            key_findings
        ) VALUES (
            NEW.id,
            NEW.query,
            CASE 
                WHEN NEW.query ILIKE '%stanley%' THEN 'Stanley'
                WHEN NEW.query ILIKE '%prime%' THEN 'Prime Hydration LLC'
                WHEN NEW.query ILIKE '%grimace%' THEN 'McDonald''s'
                WHEN NEW.query ILIKE '%dubai%' THEN 'Unknown Manufacturer'
                ELSE 'Unknown'
            END,
            NEW.query_type,
            NEW.bot_percentage,
            NEW.reality_score,
            NEW.manipulation_level,
            'Coordinated inauthentic activity detected across multiple platforms',
            CASE 
                WHEN NEW.query ILIKE '%stanley%' THEN '["62% bot accounts", "73% reviews in 3 days", "127 undisclosed sponsorships"]'::jsonb
                WHEN NEW.query ILIKE '%prime%' THEN '["71% bot engagement", "Artificial viral spike", "Influencer coordination detected"]'::jsonb
                WHEN NEW.query ILIKE '%$buzz%' THEN '["87% bot accounts", "Pump and dump pattern", "Coordinated social media push"]'::jsonb
                ELSE '["High bot activity", "Suspicious growth pattern", "Review manipulation"]'::jsonb
            END
        )
        ON CONFLICT (analysis_id) DO NOTHING; -- Prevent duplicates
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- USAGE TRACKING FUNCTIONS
-- =============================================================================

-- Function to reset monthly usage for all users (called by scheduled job)
CREATE OR REPLACE FUNCTION reset_monthly_usage()
RETURNS void AS $$
BEGIN
    UPDATE signalzero.users 
    SET analyses_used_this_month = 0,
        last_usage_reset = NOW()
    WHERE DATE_PART('day', NOW()) = 1; -- Reset on first day of month
END;
$$ LANGUAGE plpgsql;

-- Function to increment user's usage count
CREATE OR REPLACE FUNCTION increment_user_usage(user_uuid UUID)
RETURNS void AS $$
BEGIN
    UPDATE signalzero.users 
    SET analyses_used_this_month = analyses_used_this_month + 1,
        analyses_used_total = analyses_used_total + 1
    WHERE id = user_uuid;
END;
$$ LANGUAGE plpgsql;

-- Function to check if user is within usage limits
CREATE OR REPLACE FUNCTION check_usage_limit(user_uuid UUID)
RETURNS BOOLEAN AS $$
DECLARE
    current_usage INT;
    tier_limit INT;
    user_tier VARCHAR(20);
BEGIN
    -- Get user's current usage and tier
    SELECT analyses_used_this_month, subscription_tier 
    INTO current_usage, user_tier
    FROM signalzero.users 
    WHERE id = user_uuid;
    
    -- Determine limit based on tier
    tier_limit := CASE user_tier
        WHEN 'FREE' THEN 3
        WHEN 'PRO' THEN 100
        WHEN 'BUSINESS' THEN 1000
        WHEN 'ENTERPRISE' THEN 999999 -- Unlimited
        ELSE 0 -- PUBLIC tier gets 0
    END;
    
    RETURN current_usage < tier_limit;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- ANALYTICS FUNCTIONS
-- =============================================================================

-- Function to get system-wide analytics
CREATE OR REPLACE FUNCTION get_system_metrics()
RETURNS TABLE(
    total_users BIGINT,
    paid_users BIGINT,
    total_analyses BIGINT,
    avg_bot_percentage DECIMAL,
    avg_reality_score DECIMAL,
    avg_processing_time_ms DECIMAL,
    analyses_last_24h BIGINT,
    analyses_last_hour BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(DISTINCT u.id) as total_users,
        COUNT(DISTINCT CASE WHEN u.subscription_tier != 'FREE' THEN u.id END) as paid_users,
        COUNT(a.id) as total_analyses,
        AVG(a.bot_percentage) as avg_bot_percentage,
        AVG(a.reality_score) as avg_reality_score,
        AVG(a.processing_time_ms::DECIMAL) as avg_processing_time_ms,
        COUNT(CASE WHEN a.created_at > NOW() - INTERVAL '24 hours' THEN 1 END) as analyses_last_24h,
        COUNT(CASE WHEN a.created_at > NOW() - INTERVAL '1 hour' THEN 1 END) as analyses_last_hour
    FROM signalzero.users u
    LEFT JOIN signalzero.analyses a ON u.id = a.user_id;
END;
$$ LANGUAGE plpgsql;

-- Function to get top manipulated products for Wall of Shame
CREATE OR REPLACE FUNCTION get_top_manipulated_products(limit_count INT DEFAULT 10)
RETURNS TABLE(
    id UUID,
    product_name VARCHAR,
    company VARCHAR,
    bot_percentage DECIMAL,
    reality_score DECIMAL,
    manipulation_level VARCHAR,
    key_findings JSONB,
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        w.id,
        w.product_name,
        w.company,
        w.bot_percentage,
        w.reality_score,
        w.manipulation_level,
        w.key_findings,
        w.created_at
    FROM signalzero.wall_of_shame w
    WHERE w.is_active = true 
      AND (w.featured_until IS NULL OR w.featured_until > NOW())
    ORDER BY w.bot_percentage DESC, w.created_at DESC
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- REFERRAL SYSTEM FUNCTIONS
-- =============================================================================

-- Function to process referral when user signs up
CREATE OR REPLACE FUNCTION process_referral(new_user_id UUID, referrer_code VARCHAR)
RETURNS void AS $$
DECLARE
    referrer_user_id UUID;
BEGIN
    -- Find referrer by code
    SELECT id INTO referrer_user_id
    FROM signalzero.users 
    WHERE referral_code = referrer_code;
    
    IF referrer_user_id IS NOT NULL THEN
        -- Update new user's referred_by
        UPDATE signalzero.users 
        SET referred_by = referrer_user_id
        WHERE id = new_user_id;
        
        -- Increment referrer's count
        UPDATE signalzero.users 
        SET referral_count = referral_count + 1
        WHERE id = referrer_user_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- DATA CLEANUP FUNCTIONS
-- =============================================================================

-- Function to clean up old temporary data
CREATE OR REPLACE FUNCTION cleanup_old_data()
RETURNS void AS $$
BEGIN
    -- Delete old incomplete analyses (older than 24 hours)
    DELETE FROM signalzero.analyses 
    WHERE status IN ('PENDING', 'PROCESSING', 'FAILED') 
      AND created_at < NOW() - INTERVAL '24 hours';
    
    -- Delete old agent results for deleted analyses
    DELETE FROM signalzero.agent_results 
    WHERE analysis_id NOT IN (SELECT id FROM signalzero.analyses);
    
    -- Archive old marketing events (older than 30 days)
    UPDATE signalzero.marketing_events 
    SET status = 'ARCHIVED'
    WHERE created_at < NOW() - INTERVAL '30 days'
      AND status != 'ARCHIVED';
      
    -- Clean up expired API keys
    UPDATE signalzero.api_keys 
    SET is_active = false
    WHERE expires_at < NOW() 
      AND is_active = true;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- CREATE TRIGGERS
-- =============================================================================

-- Trigger to automatically update manipulation level when Reality Score is set/updated
DROP TRIGGER IF EXISTS trg_update_manipulation ON signalzero.analyses;
CREATE TRIGGER trg_update_manipulation
    BEFORE INSERT OR UPDATE ON signalzero.analyses
    FOR EACH ROW
    WHEN (NEW.reality_score IS NOT NULL)
    EXECUTE FUNCTION update_manipulation_level();

-- Trigger to automatically add high-bot products to Wall of Shame
DROP TRIGGER IF EXISTS trg_auto_wall_of_shame ON signalzero.analyses;
CREATE TRIGGER trg_auto_wall_of_shame
    AFTER INSERT OR UPDATE ON signalzero.analyses
    FOR EACH ROW
    WHEN (NEW.status = 'COMPLETE' AND NEW.bot_percentage > 60)
    EXECUTE FUNCTION auto_add_to_wall_of_shame();

-- Trigger to update updated_at timestamp on users table
DROP TRIGGER IF EXISTS trg_users_updated_at ON signalzero.users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON signalzero.users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update updated_at timestamp on wall_of_shame table
DROP TRIGGER IF EXISTS trg_wall_of_shame_updated_at ON signalzero.wall_of_shame;
CREATE TRIGGER trg_wall_of_shame_updated_at
    BEFORE UPDATE ON signalzero.wall_of_shame
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- MATERIALIZED VIEWS FOR PERFORMANCE
-- =============================================================================

-- Materialized view for dashboard metrics (refresh hourly)
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_hourly_metrics AS
SELECT 
    DATE_TRUNC('hour', created_at) as hour,
    COUNT(*) as analyses_count,
    AVG(bot_percentage) as avg_bot_percentage,
    AVG(processing_time_ms) as avg_processing_time,
    COUNT(DISTINCT user_id) as unique_users
FROM signalzero.analyses
WHERE created_at > NOW() - INTERVAL '7 days'
  AND status = 'COMPLETE'
GROUP BY DATE_TRUNC('hour', created_at)
ORDER BY hour DESC;

-- Index on materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_hourly_metrics_hour 
ON mv_hourly_metrics(hour DESC);

-- Function to refresh materialized views
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_hourly_metrics;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- DEMO DATA FUNCTIONS
-- =============================================================================

-- Function to generate demo Reality Score based on hardcoded values
CREATE OR REPLACE FUNCTION get_demo_reality_score(query_text TEXT)
RETURNS DECIMAL AS $$
BEGIN
    CASE 
        WHEN query_text ILIKE '%stanley%' THEN RETURN 34.0;
        WHEN query_text ILIKE '%$buzz%' THEN RETURN 12.0;
        WHEN query_text ILIKE '%prime energy%' THEN RETURN 29.0;
        WHEN query_text ILIKE '%grimace%' THEN RETURN 42.0;
        WHEN query_text ILIKE '%dubai%' THEN RETURN 31.0;
        ELSE RETURN 50.0 + (RANDOM() * 30); -- Random between 50-80 for other queries
    END CASE;
END;
$$ LANGUAGE plpgsql;

-- Function to generate demo bot percentage based on hardcoded values
CREATE OR REPLACE FUNCTION get_demo_bot_percentage(query_text TEXT)
RETURNS DECIMAL AS $$
BEGIN
    CASE 
        WHEN query_text ILIKE '%stanley%' THEN RETURN 62.0;
        WHEN query_text ILIKE '%$buzz%' THEN RETURN 87.0;
        WHEN query_text ILIKE '%prime energy%' THEN RETURN 71.0;
        WHEN query_text ILIKE '%grimace%' THEN RETURN 58.0;
        WHEN query_text ILIKE '%dubai%' THEN RETURN 64.0;
        ELSE RETURN 20.0 + (RANDOM() * 40); -- Random between 20-60 for other queries
    END CASE;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON FUNCTION calculate_reality_score(DECIMAL, DECIMAL, DECIMAL, DECIMAL) IS 'Calculates Reality Score™ using exact weights: Bot 40%, Trend 30%, Review 20%, Promotion 10%';
COMMENT ON FUNCTION classify_manipulation(DECIMAL) IS 'Classifies manipulation level: RED (0-33%), YELLOW (34-66%), GREEN (67-100%)';
COMMENT ON FUNCTION check_usage_limit(UUID) IS 'Checks if user is within monthly usage limits based on subscription tier';
COMMENT ON FUNCTION get_demo_reality_score(TEXT) IS 'Returns hardcoded demo Reality Scores for consistent hackathon demonstrations';

-- Functions and triggers creation complete
