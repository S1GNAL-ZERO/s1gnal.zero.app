-- S1GNAL.ZERO Database Views
-- Based on DETAILED_DESIGN.md Section 6.2
-- This script creates all database views for reporting and dashboard queries

-- =============================================================================
-- WALL OF SHAME VIEWS
-- =============================================================================

-- View for public Wall of Shame display
CREATE OR REPLACE VIEW v_wall_of_shame AS
SELECT 
    w.id,
    w.product_name,
    w.company,
    w.category,
    w.bot_percentage,
    w.reality_score,
    w.manipulation_level,
    w.evidence_summary,
    w.key_findings,
    w.views,
    w.shares,
    w.reports,
    w.display_order,
    w.created_at,
    w.updated_at,
    a.query,
    a.platform,
    a.query_type,
    a.processing_time_ms
FROM wall_of_shame w
JOIN analyses a ON w.analysis_id = a.id
WHERE w.is_active = true 
  AND (w.featured_until IS NULL OR w.featured_until > NOW())
ORDER BY 
    COALESCE(w.display_order, 999), 
    w.bot_percentage DESC, 
    w.created_at DESC;

-- View for Wall of Shame with engagement metrics
CREATE OR REPLACE VIEW v_wall_of_shame_trending AS
SELECT 
    w.*,
    a.query,
    a.platform,
    a.processing_time_ms,
    (w.views + w.shares * 3 + w.reports * 5) as engagement_score,
    CASE 
        WHEN w.created_at > NOW() - INTERVAL '24 hours' THEN 'NEW'
        WHEN (w.views + w.shares + w.reports) > 100 THEN 'TRENDING'
        ELSE 'ACTIVE'
    END as trending_status
FROM wall_of_shame w
JOIN analyses a ON w.analysis_id = a.id
WHERE w.is_active = true 
  AND (w.featured_until IS NULL OR w.featured_until > NOW())
ORDER BY engagement_score DESC, w.created_at DESC;

-- =============================================================================
-- USER ANALYTICS VIEWS
-- =============================================================================

-- View for user dashboard analytics
CREATE OR REPLACE VIEW v_user_analyses AS
SELECT 
    a.id,
    a.user_id,
    a.query,
    a.query_type,
    a.platform,
    a.reality_score,
    a.bot_percentage,
    a.trend_score,
    a.review_score,
    a.promotion_score,
    a.manipulation_level,
    a.confidence_score,
    a.status,
    a.processing_time_ms,
    a.is_public,
    a.is_featured,
    a.created_at,
    a.completed_at,
    u.email as user_email,
    u.full_name as user_name,
    u.subscription_tier,
    -- Calculate time to complete
    CASE 
        WHEN a.completed_at IS NOT NULL AND a.started_at IS NOT NULL 
        THEN EXTRACT(EPOCH FROM (a.completed_at - a.started_at)) * 1000
        ELSE a.processing_time_ms 
    END as actual_processing_time_ms,
    -- Check if in Wall of Shame
    CASE WHEN w.id IS NOT NULL THEN true ELSE false END as in_wall_of_shame
FROM analyses a
JOIN users u ON a.user_id = u.id
LEFT JOIN wall_of_shame w ON a.id = w.analysis_id AND w.is_active = true
WHERE a.status = 'COMPLETE'
ORDER BY a.created_at DESC;

-- View for user subscription analytics
CREATE OR REPLACE VIEW v_user_subscription_metrics AS
SELECT 
    u.id,
    u.email,
    u.full_name,
    u.subscription_tier,
    u.subscription_start_date,
    u.subscription_end_date,
    u.analyses_used_this_month,
    u.analyses_used_total,
    u.referral_count,
    u.created_at as user_created_at,
    u.last_login_at,
    -- Usage limits based on tier
    CASE u.subscription_tier
        WHEN 'PUBLIC' THEN 0
        WHEN 'FREE' THEN 3
        WHEN 'PRO' THEN 100
        WHEN 'BUSINESS' THEN 1000
        WHEN 'ENTERPRISE' THEN 999999
    END as monthly_limit,
    -- Usage percentage
    CASE 
        WHEN u.subscription_tier = 'ENTERPRISE' THEN 0
        WHEN u.subscription_tier = 'PUBLIC' THEN 100
        ELSE ROUND(
            (u.analyses_used_this_month::DECIMAL / 
            CASE u.subscription_tier
                WHEN 'FREE' THEN 3
                WHEN 'PRO' THEN 100
                WHEN 'BUSINESS' THEN 1000
                ELSE 1
            END * 100), 1
        )
    END as usage_percentage,
    -- Account status
    CASE 
        WHEN u.subscription_end_date IS NOT NULL AND u.subscription_end_date < NOW() THEN 'EXPIRED'
        WHEN NOT u.is_active THEN 'INACTIVE'
        WHEN NOT u.is_verified THEN 'UNVERIFIED'
        ELSE 'ACTIVE'
    END as account_status,
    -- Analysis counts
    COUNT(a.id) as total_analyses_completed,
    COUNT(CASE WHEN a.created_at > NOW() - INTERVAL '30 days' THEN 1 END) as analyses_last_30_days,
    COUNT(CASE WHEN a.manipulation_level = 'RED' THEN 1 END) as red_zone_analyses,
    AVG(a.bot_percentage) as avg_bot_percentage_found,
    AVG(a.reality_score) as avg_reality_score
FROM users u
LEFT JOIN analyses a ON u.id = a.user_id AND a.status = 'COMPLETE'
GROUP BY u.id, u.email, u.full_name, u.subscription_tier, u.subscription_start_date, 
         u.subscription_end_date, u.analyses_used_this_month, u.analyses_used_total,
         u.referral_count, u.created_at, u.last_login_at
ORDER BY u.created_at DESC;

-- =============================================================================
-- SYSTEM METRICS VIEWS
-- =============================================================================

-- View for system-wide metrics and KPIs
CREATE OR REPLACE VIEW v_system_metrics AS
SELECT 
    -- User metrics
    COUNT(DISTINCT u.id) as total_users,
    COUNT(DISTINCT CASE WHEN u.subscription_tier = 'FREE' THEN u.id END) as free_users,
    COUNT(DISTINCT CASE WHEN u.subscription_tier = 'PRO' THEN u.id END) as pro_users,
    COUNT(DISTINCT CASE WHEN u.subscription_tier = 'BUSINESS' THEN u.id END) as business_users,
    COUNT(DISTINCT CASE WHEN u.subscription_tier = 'ENTERPRISE' THEN u.id END) as enterprise_users,
    COUNT(DISTINCT CASE WHEN u.subscription_tier != 'FREE' AND u.subscription_tier != 'PUBLIC' THEN u.id END) as paid_users,
    
    -- Analysis metrics
    COUNT(a.id) as total_analyses,
    COUNT(CASE WHEN a.status = 'COMPLETE' THEN 1 END) as completed_analyses,
    COUNT(CASE WHEN a.status = 'PENDING' THEN 1 END) as pending_analyses,
    COUNT(CASE WHEN a.status = 'FAILED' THEN 1 END) as failed_analyses,
    
    -- Performance metrics
    ROUND(AVG(a.bot_percentage), 2) as avg_bot_percentage,
    ROUND(AVG(a.reality_score), 2) as avg_reality_score,
    ROUND(AVG(a.processing_time_ms), 0) as avg_processing_time_ms,
    ROUND(AVG(a.confidence_score), 2) as avg_confidence_score,
    
    -- Time-based metrics
    COUNT(CASE WHEN a.created_at > NOW() - INTERVAL '24 hours' THEN 1 END) as analyses_last_24h,
    COUNT(CASE WHEN a.created_at > NOW() - INTERVAL '1 hour' THEN 1 END) as analyses_last_hour,
    COUNT(CASE WHEN u.created_at > NOW() - INTERVAL '24 hours' THEN 1 END) as new_users_last_24h,
    
    -- Wall of Shame metrics
    COUNT(DISTINCT w.id) as wall_of_shame_entries,
    COUNT(CASE WHEN a.manipulation_level = 'RED' THEN 1 END) as red_zone_analyses,
    COUNT(CASE WHEN a.manipulation_level = 'YELLOW' THEN 1 END) as yellow_zone_analyses,
    COUNT(CASE WHEN a.manipulation_level = 'GREEN' THEN 1 END) as green_zone_analyses,
    
    -- Success rate
    ROUND(
        COUNT(CASE WHEN a.status = 'COMPLETE' THEN 1 END)::DECIMAL / 
        NULLIF(COUNT(CASE WHEN a.status != 'PENDING' THEN 1 END), 0) * 100, 1
    ) as success_rate_percentage,
    
    -- Current timestamp for report generation
    NOW() as report_generated_at
FROM users u
LEFT JOIN analyses a ON u.id = a.user_id
LEFT JOIN wall_of_shame w ON a.id = w.analysis_id AND w.is_active = true;

-- =============================================================================
-- AGENT PERFORMANCE VIEWS
-- =============================================================================

-- View for agent performance analytics
CREATE OR REPLACE VIEW v_agent_performance AS
SELECT 
    ar.agent_type,
    ar.agent_version,
    COUNT(*) as total_executions,
    COUNT(CASE WHEN ar.status = 'COMPLETE' THEN 1 END) as successful_executions,
    COUNT(CASE WHEN ar.status = 'FAILED' THEN 1 END) as failed_executions,
    COUNT(CASE WHEN ar.status = 'TIMEOUT' THEN 1 END) as timeout_executions,
    
    -- Performance metrics
    ROUND(AVG(ar.processing_time_ms), 0) as avg_processing_time_ms,
    ROUND(MIN(ar.processing_time_ms), 0) as min_processing_time_ms,
    ROUND(MAX(ar.processing_time_ms), 0) as max_processing_time_ms,
    ROUND(AVG(ar.score), 2) as avg_score,
    ROUND(AVG(ar.confidence), 2) as avg_confidence,
    
    -- Success rate
    ROUND(
        COUNT(CASE WHEN ar.status = 'COMPLETE' THEN 1 END)::DECIMAL / 
        NULLIF(COUNT(*), 0) * 100, 1
    ) as success_rate_percentage,
    
    -- Data source usage
    COUNT(CASE WHEN ar.data_sources ? 'twitter_api' THEN 1 END) as used_twitter_api,
    COUNT(CASE WHEN ar.data_sources ? 'reddit_api' THEN 1 END) as used_reddit_api,
    COUNT(CASE WHEN ar.data_sources ? 'mock_fallback' THEN 1 END) as used_mock_fallback,
    COUNT(CASE WHEN ar.data_sources ? 'cache' THEN 1 END) as used_cache,
    
    -- Time-based metrics
    COUNT(CASE WHEN ar.created_at > NOW() - INTERVAL '24 hours' THEN 1 END) as executions_last_24h,
    COUNT(CASE WHEN ar.created_at > NOW() - INTERVAL '1 hour' THEN 1 END) as executions_last_hour,
    
    MIN(ar.created_at) as first_execution,
    MAX(ar.completed_at) as last_execution
FROM agent_results ar
WHERE ar.created_at > NOW() - INTERVAL '30 days' -- Focus on recent data
GROUP BY ar.agent_type, ar.agent_version
ORDER BY ar.agent_type, ar.agent_version DESC;

-- =============================================================================
-- FINANCIAL & PAYMENT VIEWS
-- =============================================================================

-- View for payment and revenue analytics
CREATE OR REPLACE VIEW v_payment_metrics AS
SELECT 
    p.payment_type,
    p.currency,
    COUNT(*) as total_transactions,
    COUNT(CASE WHEN p.status = 'SUCCEEDED' THEN 1 END) as successful_payments,
    COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END) as failed_payments,
    COUNT(CASE WHEN p.status = 'REFUNDED' THEN 1 END) as refunded_payments,
    
    -- Revenue metrics
    SUM(CASE WHEN p.status = 'SUCCEEDED' THEN p.amount ELSE 0 END) as total_revenue,
    AVG(CASE WHEN p.status = 'SUCCEEDED' THEN p.amount ELSE NULL END) as avg_transaction_amount,
    
    -- Time-based metrics
    SUM(CASE WHEN p.status = 'SUCCEEDED' AND p.processed_at > NOW() - INTERVAL '30 days' THEN p.amount ELSE 0 END) as revenue_last_30_days,
    COUNT(CASE WHEN p.created_at > NOW() - INTERVAL '24 hours' THEN 1 END) as transactions_last_24h,
    
    -- Success rate
    ROUND(
        COUNT(CASE WHEN p.status = 'SUCCEEDED' THEN 1 END)::DECIMAL / 
        NULLIF(COUNT(*), 0) * 100, 1
    ) as success_rate_percentage,
    
    MIN(p.created_at) as first_payment,
    MAX(p.processed_at) as last_payment
FROM payments p
GROUP BY p.payment_type, p.currency
ORDER BY total_revenue DESC;

-- =============================================================================
-- MARKETING & GROWTH VIEWS
-- =============================================================================

-- View for waitlist and marketing metrics
CREATE OR REPLACE VIEW v_marketing_metrics AS
SELECT 
    w.source,
    COUNT(*) as total_signups,
    COUNT(CASE WHEN w.email_verified = true THEN 1 END) as verified_signups,
    COUNT(CASE WHEN w.converted_to_user = true THEN 1 END) as converted_signups,
    SUM(w.referrals_count) as total_referrals_generated,
    
    -- Conversion rates
    ROUND(
        COUNT(CASE WHEN w.converted_to_user = true THEN 1 END)::DECIMAL / 
        NULLIF(COUNT(*), 0) * 100, 1
    ) as conversion_rate_percentage,
    
    ROUND(
        COUNT(CASE WHEN w.email_verified = true THEN 1 END)::DECIMAL / 
        NULLIF(COUNT(*), 0) * 100, 1
    ) as verification_rate_percentage,
    
    -- Engagement metrics
    AVG(w.emails_sent) as avg_emails_sent,
    AVG(w.emails_opened) as avg_emails_opened,
    
    ROUND(
        AVG(CASE WHEN w.emails_sent > 0 THEN w.emails_opened::DECIMAL / w.emails_sent * 100 ELSE 0 END), 1
    ) as avg_open_rate_percentage,
    
    -- Time-based metrics
    COUNT(CASE WHEN w.created_at > NOW() - INTERVAL '24 hours' THEN 1 END) as signups_last_24h,
    COUNT(CASE WHEN w.created_at > NOW() - INTERVAL '7 days' THEN 1 END) as signups_last_7_days,
    
    MIN(w.created_at) as first_signup,
    MAX(w.created_at) as last_signup
FROM waitlist w
GROUP BY w.source
ORDER BY total_signups DESC;

-- =============================================================================
-- REAL-TIME DASHBOARD VIEWS
-- =============================================================================

-- View for real-time dashboard (last 24 hours activity)
CREATE OR REPLACE VIEW v_dashboard_realtime AS
SELECT 
    'analyses' as metric_type,
    COUNT(*) as count,
    EXTRACT(HOUR FROM created_at) as hour_of_day
FROM analyses 
WHERE created_at > NOW() - INTERVAL '24 hours'
GROUP BY EXTRACT(HOUR FROM created_at)

UNION ALL

SELECT 
    'users' as metric_type,
    COUNT(*) as count,
    EXTRACT(HOUR FROM created_at) as hour_of_day
FROM users 
WHERE created_at > NOW() - INTERVAL '24 hours'
GROUP BY EXTRACT(HOUR FROM created_at)

UNION ALL

SELECT 
    'payments' as metric_type,
    COUNT(*) as count,
    EXTRACT(HOUR FROM created_at) as hour_of_day
FROM payments 
WHERE created_at > NOW() - INTERVAL '24 hours'
  AND status = 'SUCCEEDED'
GROUP BY EXTRACT(HOUR FROM created_at)

ORDER BY metric_type, hour_of_day;

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON VIEW v_wall_of_shame IS 'Public Wall of Shame display with product details and evidence';
COMMENT ON VIEW v_user_analyses IS 'User dashboard analytics with processing times and manipulation levels';
COMMENT ON VIEW v_system_metrics IS 'System-wide KPIs and performance metrics for admin dashboard';
COMMENT ON VIEW v_agent_performance IS 'Agent execution metrics and success rates for monitoring';
COMMENT ON VIEW v_payment_metrics IS 'Revenue and payment analytics for financial reporting';
COMMENT ON VIEW v_marketing_metrics IS 'Waitlist and conversion metrics for growth tracking';

-- Views creation complete
