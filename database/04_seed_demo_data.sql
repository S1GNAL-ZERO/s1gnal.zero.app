-- S1GNAL.ZERO Demo Data Seeding Script
-- Based on DETAILED_DESIGN.md Section 6.3
-- This script seeds the database with demo users, analyses, and Wall of Shame entries

-- =============================================================================
-- DEMO USERS WITH DIFFERENT SUBSCRIPTION TIERS
-- =============================================================================

-- Insert demo users with bcrypt hashed passwords (password: "password123")
INSERT INTO signalzero.users (email, password_hash, full_name, subscription_tier, referral_code, is_active, is_verified) VALUES
('demo@s1gnalzero.com', '$2b$10$YGq3Uu7I9ftRTpm.OuUokuW.VGeKswLwX6asfOmGrJXxAotGr0YOu', 'Demo User', 'PRO', 'DEMO2024', true, true),
('founder@s1gnalzero.com', '$2b$10$YGq3Uu7I9ftRTpm.OuUokuW.VGeKswLwX6asfOmGrJXxAotGr0YOu', 'Founder Account', 'ENTERPRISE', 'FOUNDER', true, true),
('free@example.com', '$2b$10$YGq3Uu7I9ftRTpm.OuUokuW.VGeKswLwX6asfOmGrJXxAotGr0YOu', 'Free User', 'FREE', 'FREE123', true, true),
('business@company.com', '$2b$10$YGq3Uu7I9ftRTpm.OuUokuW.VGeKswLwX6asfOmGrJXxAotGr0YOu', 'Business User', 'BUSINESS', 'BIZ2024', true, true),
('viewer@example.com', '$2b$10$YGq3Uu7I9ftRTpm.OuUokuW.VGeKswLwX6asfOmGrJXxAotGr0YOu', 'Public Viewer', 'FREE', 'VIEWER01', true, true)
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- DEMO ANALYSES FOR WALL OF SHAME (HARDCODED VALUES)
-- =============================================================================

-- Insert demo analyses with exact values from DETAILED_DESIGN.md
INSERT INTO signalzero.analyses (
    user_id, 
    query, 
    query_type, 
    platform, 
    reality_score, 
    bot_percentage, 
    trend_score, 
    review_score, 
    promotion_score, 
    manipulation_level, 
    confidence_score,
    status, 
    processing_time_ms,
    is_public,
    is_featured,
    completed_at
) VALUES
-- Stanley Cup - 62% bots, 34% Reality Score (RED)
((SELECT id FROM signalzero.users WHERE email = 'demo@s1gnalzero.com'), 
 'Stanley Cup Tumbler', 'product', 'all', 34.00, 62.00, 22.00, 27.00, 18.00, 
 'RED', 94.5, 'COMPLETE', 3240, true, true, NOW()),

-- Prime Energy - 71% bots, 29% Reality Score (RED)  
((SELECT id FROM signalzero.users WHERE email = 'demo@s1gnalzero.com'), 
 'Prime Energy Drink', 'product', 'all', 29.00, 71.00, 18.00, 31.00, 15.00, 
 'RED', 91.2, 'COMPLETE', 2890, true, true, NOW()),

-- $BUZZ Meme Stock - 87% bots, 12% Reality Score (RED)
((SELECT id FROM signalzero.users WHERE email = 'demo@s1gnalzero.com'), 
 '$BUZZ Meme Stock', 'stock', 'reddit', 12.00, 87.00, 8.00, 15.00, 10.00, 
 'RED', 96.8, 'COMPLETE', 2340, true, true, NOW()),

-- Grimace Shake - 58% bots, 42% Reality Score (YELLOW)
((SELECT id FROM signalzero.users WHERE email = 'demo@s1gnalzero.com'), 
 'Grimace Shake', 'trend', 'twitter', 42.00, 58.00, 35.00, 42.00, 48.00, 
 'YELLOW', 88.3, 'COMPLETE', 3560, true, false, NOW()),

-- Dubai Chocolate - 64% bots, 31% Reality Score (RED)
((SELECT id FROM signalzero.users WHERE email = 'demo@s1gnalzero.com'), 
 'Dubai Chocolate', 'product', 'instagram', 31.00, 64.00, 24.00, 29.00, 22.00, 
 'RED', 89.7, 'COMPLETE', 4120, true, true, NOW()),

-- Some authentic products for contrast (GREEN zone)
((SELECT id FROM signalzero.users WHERE email = 'founder@s1gnalzero.com'), 
 'Local Artisan Coffee', 'product', 'instagram', 78.00, 15.00, 82.00, 85.00, 88.00, 
 'GREEN', 92.1, 'COMPLETE', 2100, true, false, NOW()),

((SELECT id FROM signalzero.users WHERE email = 'business@company.com'), 
 'Community Book Club', 'trend', 'twitter', 82.00, 8.00, 89.00, 78.00, 91.00, 
 'GREEN', 87.5, 'COMPLETE', 1890, true, false, NOW())

ON CONFLICT (query) DO NOTHING;

-- =============================================================================
-- AGENT RESULTS FOR DEMO ANALYSES
-- =============================================================================

-- Insert agent results for each analysis
INSERT INTO signalzero.agent_results (analysis_id, agent_type, score, confidence, status, evidence, data_sources, processing_time_ms)
SELECT 
    a.id,
    'bot-detector',
    100 - a.bot_percentage, -- Invert bot percentage to get authenticity score
    94.5,
    'COMPLETE',
    CASE 
        WHEN a.query ILIKE '%stanley%' THEN 
            '{"bot_accounts": 6234, "new_accounts": 4821, "burst_creation": true, "suspicious_patterns": ["Default avatars", "Similar usernames", "Coordinated posting"], "sample_bots": ["@user84726", "@acc92847", "@bot_stanley91"]}'::jsonb
        WHEN a.query ILIKE '%prime%' THEN
            '{"bot_accounts": 4521, "new_accounts": 3892, "burst_creation": true, "suspicious_patterns": ["Identical engagement timing", "Copy-paste comments"], "sample_bots": ["@prime_lover88", "@energy_fan42"]}'::jsonb
        WHEN a.query ILIKE '%$buzz%' THEN
            '{"bot_accounts": 8734, "new_accounts": 7421, "burst_creation": true, "suspicious_patterns": ["Pump and dump coordination", "Fake trading signals"], "sample_bots": ["@crypto_bull91", "@moon_rocket"]}'::jsonb
        ELSE
            '{"bot_accounts": 200, "new_accounts": 150, "burst_creation": false, "suspicious_patterns": [], "sample_bots": []}'::jsonb
    END,
    '["twitter_api", "reddit_api", "mock_fallback"]'::jsonb,
    FLOOR(RANDOM() * 1000 + 500)
FROM signalzero.analyses a 
WHERE a.status = 'COMPLETE'
ON CONFLICT (analysis_id, agent_type) DO NOTHING;

-- Insert trend analysis results
INSERT INTO signalzero.agent_results (analysis_id, agent_type, score, confidence, status, evidence, data_sources, processing_time_ms)
SELECT 
    a.id,
    'trend-analyzer',
    a.trend_score,
    91.3,
    'COMPLETE',
    CASE 
        WHEN a.query ILIKE '%stanley%' THEN 
            '{"velocity_score": 22, "spike_detected": true, "growth_rate": "9500%", "time_to_spike": "48 hours", "platforms_affected": 5, "organic_probability": 0.23}'::jsonb
        WHEN a.query ILIKE '%prime%' THEN
            '{"velocity_score": 18, "spike_detected": true, "growth_rate": "7800%", "time_to_spike": "36 hours", "platforms_affected": 4, "organic_probability": 0.18}'::jsonb
        ELSE
            '{"velocity_score": 75, "spike_detected": false, "growth_rate": "150%", "time_to_spike": "7 days", "platforms_affected": 2, "organic_probability": 0.85}'::jsonb
    END,
    '["google_trends", "reddit_api", "newsapi"]'::jsonb,
    FLOOR(RANDOM() * 1200 + 800)
FROM signalzero.analyses a 
WHERE a.status = 'COMPLETE'
ON CONFLICT (analysis_id, agent_type) DO NOTHING;

-- Insert review analysis results
INSERT INTO signalzero.agent_results (analysis_id, agent_type, score, confidence, status, evidence, data_sources, processing_time_ms)
SELECT 
    a.id,
    'review-validator',
    a.review_score,
    88.7,
    'COMPLETE',
    CASE 
        WHEN a.query ILIKE '%stanley%' THEN 
            '{"total_reviews": 8453, "verified_purchases": 1876, "duplicate_phrases": 342, "template_detected": true, "time_distribution": {"last_3_days": 6234, "last_week": 6890}, "rating_distribution": {"5_star": 6234, "1_star": 1851}}'::jsonb
        WHEN a.query ILIKE '%prime%' THEN
            '{"total_reviews": 5623, "verified_purchases": 1234, "duplicate_phrases": 278, "template_detected": true, "suspicious_timing": true}'::jsonb
        ELSE
            '{"total_reviews": 1234, "verified_purchases": 1100, "duplicate_phrases": 12, "template_detected": false, "natural_distribution": true}'::jsonb
    END,
    '["amazon_scraper", "trustpilot_api", "mock_fallback"]'::jsonb,
    FLOOR(RANDOM() * 1500 + 1000)
FROM signalzero.analyses a 
WHERE a.status = 'COMPLETE'
ON CONFLICT (analysis_id, agent_type) DO NOTHING;

-- Insert promotion detection results  
INSERT INTO signalzero.agent_results (analysis_id, agent_type, score, confidence, status, evidence, data_sources, processing_time_ms)
SELECT 
    a.id,
    'paid-promotion',
    a.promotion_score,
    85.2,
    'COMPLETE',
    CASE 
        WHEN a.query ILIKE '%stanley%' THEN 
            '{"total_posts": 500, "undisclosed_likely": 127, "properly_disclosed": 23, "influencer_coordination": true, "timing_patterns": {"posts_within_1_hour": 47}}'::jsonb
        WHEN a.query ILIKE '%prime%' THEN
            '{"total_posts": 423, "undisclosed_likely": 89, "properly_disclosed": 15, "influencer_coordination": true, "sponsored_content_detected": true}'::jsonb
        ELSE
            '{"total_posts": 89, "undisclosed_likely": 2, "properly_disclosed": 12, "influencer_coordination": false, "organic_mentions": true}'::jsonb
    END,
    '["youtube_api", "instagram_api", "sponsorblock"]'::jsonb,
    FLOOR(RANDOM() * 2000 + 1500)
FROM signalzero.analyses a 
WHERE a.status = 'COMPLETE'
ON CONFLICT (analysis_id, agent_type) DO NOTHING;

-- Insert score aggregator results
INSERT INTO signalzero.agent_results (analysis_id, agent_type, score, confidence, status, evidence, data_sources, processing_time_ms)
SELECT 
    a.id,
    'score-aggregator',
    a.reality_score,
    93.1,
    'COMPLETE',
    ('{"final_score": ' || a.reality_score || ', "classification": "' || a.manipulation_level || '", "agent_scores": {"bot": ' || (100 - a.bot_percentage) || ', "trend": ' || a.trend_score || ', "review": ' || a.review_score || ', "promotion": ' || a.promotion_score || '}, "weights_applied": {"bot": 0.4, "trend": 0.3, "review": 0.2, "promotion": 0.1}}')::jsonb,
    '["internal"]'::jsonb,
    FLOOR(RANDOM() * 500 + 200)
FROM signalzero.analyses a 
WHERE a.status = 'COMPLETE'
ON CONFLICT (analysis_id, agent_type) DO NOTHING;

-- =============================================================================
-- WALL OF SHAME ENTRIES (AUTO-POPULATED BY TRIGGER)
-- =============================================================================

-- The wall_of_shame entries will be automatically created by the trigger
-- when analyses with bot_percentage > 60% are inserted. But we can also
-- manually ensure they exist with proper company information:

INSERT INTO signalzero.wall_of_shame (
    analysis_id,
    product_name,
    company,
    category,
    bot_percentage,
    reality_score,
    manipulation_level,
    evidence_summary,
    key_findings,
    display_order
) 
SELECT 
    a.id,
    a.query,
    CASE 
        WHEN a.query ILIKE '%stanley%' THEN 'Stanley Brand'
        WHEN a.query ILIKE '%prime%' THEN 'Prime Hydration LLC'  
        WHEN a.query ILIKE '%grimace%' THEN 'McDonald''s'
        WHEN a.query ILIKE '%dubai%' THEN 'Fix Dessert Chocolatier'
        WHEN a.query ILIKE '%$buzz%' THEN 'VanEck Social Sentiment ETF'
        ELSE 'Unknown Company'
    END,
    a.query_type,
    a.bot_percentage,
    a.reality_score,
    a.manipulation_level,
    'Coordinated inauthentic activity detected across multiple platforms with evidence of bot networks and undisclosed sponsorships.',
    CASE 
        WHEN a.query ILIKE '%stanley%' THEN 
            '["62% bot accounts detected", "73% of reviews posted in 3 days", "127 undisclosed sponsorships found", "Artificial viral spike pattern"]'::jsonb
        WHEN a.query ILIKE '%prime%' THEN 
            '["71% bot engagement", "Coordinated influencer campaign", "Suspicious review patterns", "Undisclosed partnerships"]'::jsonb
        WHEN a.query ILIKE '%$buzz%' THEN 
            '["87% bot accounts", "Pump and dump coordination", "Fake trading signals", "Social media manipulation"]'::jsonb
        WHEN a.query ILIKE '%dubai%' THEN
            '["64% bot accounts", "Viral TikTok manipulation", "Fake scarcity creation", "Influencer coordination"]'::jsonb
        ELSE 
            '["High bot activity", "Suspicious growth pattern", "Review manipulation detected"]'::jsonb
    END,
    CASE 
        WHEN a.query ILIKE '%$buzz%' THEN 1  -- Highest bot percentage first
        WHEN a.query ILIKE '%prime%' THEN 2
        WHEN a.query ILIKE '%dubai%' THEN 3  
        WHEN a.query ILIKE '%stanley%' THEN 4
        ELSE 999
    END
FROM signalzero.analyses a
WHERE a.bot_percentage > 50 
  AND a.status = 'COMPLETE'
  AND a.is_public = true
ON CONFLICT (analysis_id) DO NOTHING;

-- =============================================================================
-- WAITLIST ENTRIES FOR IMPRESSIVE NUMBERS
-- =============================================================================

-- Insert demo waitlist entries
INSERT INTO signalzero.waitlist (email, referral_code, source, position, email_verified) VALUES
('early.adopter1@gmail.com', 'EARLY001', 'reddit', 1, true),
('viral.hunter@yahoo.com', 'VIRAL002', 'twitter', 2, true),
('fomo.killer@outlook.com', 'FOMO003', 'producthunt', 3, true),
('truth.seeker@proton.me', 'TRUTH004', 'hackernews', 4, true),
('bot.detective@gmail.com', 'DETECT05', 'reddit', 5, true),
('reality.checker@gmail.com', 'REALITY6', 'direct', 6, false),
('signal.seeker@yahoo.com', 'SIGNAL07', 'twitter', 7, true),
('authenticity.fan@hotmail.com', 'AUTH008', 'producthunt', 8, true)
ON CONFLICT (email) DO NOTHING;

-- Generate additional waitlist entries for impressive demo numbers
INSERT INTO signalzero.waitlist (email, referral_code, source, position, email_verified)
SELECT 
    'user' || gs || '@example.com',
    'REF' || LPAD(gs::text, 6, '0'),
    (ARRAY['reddit', 'twitter', 'direct', 'producthunt', 'hackernews', 'discord'])[1 + (gs % 6)],
    gs + 8,
    (gs % 3 = 0) -- Every 3rd user is verified
FROM generate_series(1, 992) gs
ON CONFLICT (email) DO NOTHING;

-- =============================================================================
-- DEMO PAYMENTS FOR REVENUE METRICS
-- =============================================================================

-- Insert demo payment records
INSERT INTO signalzero.payments (user_id, amount, currency, payment_type, stripe_payment_intent_id, status, description, processed_at) VALUES
((SELECT id FROM signalzero.users WHERE email = 'demo@s1gnalzero.com'), 99.00, 'USD', 'monthly', 'pi_demo_stanley_001', 'SUCCEEDED', 'PRO subscription - Monthly', NOW() - INTERVAL '5 days'),
((SELECT id FROM signalzero.users WHERE email = 'founder@s1gnalzero.com'), 4999.00, 'USD', 'annual', 'pi_demo_founder_001', 'SUCCEEDED', 'ENTERPRISE subscription - Annual', NOW() - INTERVAL '10 days'),
((SELECT id FROM signalzero.users WHERE email = 'business@company.com'), 499.00, 'USD', 'monthly', 'pi_demo_business_001', 'SUCCEEDED', 'BUSINESS subscription - Monthly', NOW() - INTERVAL '2 days'),
((SELECT id FROM signalzero.users WHERE email = 'free@example.com'), 99.00, 'USD', 'monthly', 'pi_demo_failed_001', 'FAILED', 'PRO subscription attempt - Card declined', NOW() - INTERVAL '1 day')
ON CONFLICT (stripe_payment_intent_id) DO NOTHING;

-- =============================================================================
-- DEMO MARKETING EVENTS
-- =============================================================================

-- Insert demo marketing events
INSERT INTO signalzero.marketing_events (event_type, platform, action, content, analysis_id, product_exposed, views, clicks, engagements, status, posted_at) VALUES
('reddit_post', 'reddit', 'exposure_posted', 'EXPOSED: Stanley Cup is 62% BOTS! S1GNAL.ZERO reveals the truth behind viral products. Check it out!', 
 (SELECT id FROM signalzero.analyses WHERE query ILIKE '%stanley%' LIMIT 1), 'Stanley Cup Tumbler', 1247, 89, 156, 'POSTED', NOW() - INTERVAL '3 hours'),

('twitter_post', 'twitter', 'exposure_posted', '🚨 VIRAL MANIPULATION ALERT 🚨\n\nPrime Energy: 71% BOT ENGAGEMENT detected!\n\n#NoMoreFOMO #BotDetection #S1GNALZERO', 
 (SELECT id FROM signalzero.analyses WHERE query ILIKE '%prime%' LIMIT 1), 'Prime Energy Drink', 892, 45, 78, 'POSTED', NOW() - INTERVAL '6 hours'),

('discord_message', 'discord', 'exposure_posted', '$BUZZ meme stock manipulation exposed! 87% bots pushing fake hype. Protect your investments with S1GNAL.ZERO', 
 (SELECT id FROM signalzero.analyses WHERE query ILIKE '%$buzz%' LIMIT 1), '$BUZZ Meme Stock', 234, 23, 41, 'POSTED', NOW() - INTERVAL '2 hours')
ON CONFLICT DO NOTHING;

-- =============================================================================
-- UPDATE USAGE STATISTICS FOR DEMO USERS  
-- =============================================================================

-- Update usage statistics for demo users
UPDATE signalzero.users SET 
    analyses_used_this_month = 2,
    analyses_used_total = 15,
    last_login_at = NOW() - INTERVAL '1 hour'
WHERE email = 'demo@s1gnalzero.com';

UPDATE signalzero.users SET 
    analyses_used_this_month = 8,
    analyses_used_total = 127,
    referral_count = 5,
    last_login_at = NOW() - INTERVAL '30 minutes'
WHERE email = 'founder@s1gnalzero.com';

UPDATE signalzero.users SET 
    analyses_used_this_month = 3,
    analyses_used_total = 3,
    last_login_at = NOW() - INTERVAL '2 hours'
WHERE email = 'free@example.com';

-- =============================================================================
-- REFRESH MATERIALIZED VIEWS
-- =============================================================================

-- Refresh materialized views with new data
REFRESH MATERIALIZED VIEW signalzero.mv_hourly_metrics;

-- =============================================================================
-- VERIFY DATA INSERTION
-- =============================================================================

-- Display summary of seeded data
DO $$
DECLARE
    user_count INTEGER;
    analysis_count INTEGER;
    wall_count INTEGER;
    waitlist_count INTEGER;
    payment_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM signalzero.users;
    SELECT COUNT(*) INTO analysis_count FROM signalzero.analyses WHERE status = 'COMPLETE';
    SELECT COUNT(*) INTO wall_count FROM signalzero.wall_of_shame WHERE is_active = true;
    SELECT COUNT(*) INTO waitlist_count FROM signalzero.waitlist;
    SELECT COUNT(*) INTO payment_count FROM signalzero.payments WHERE status = 'SUCCEEDED';
    
    RAISE NOTICE '=== S1GNAL.ZERO DEMO DATA SEEDED ===';
    RAISE NOTICE 'Users: %', user_count;
    RAISE NOTICE 'Completed Analyses: %', analysis_count;
    RAISE NOTICE 'Wall of Shame Entries: %', wall_count;
    RAISE NOTICE 'Waitlist Signups: %', waitlist_count;
    RAISE NOTICE 'Successful Payments: %', payment_count;
    RAISE NOTICE '=====================================';
END $$;

-- Demo data seeding complete
COMMENT ON TABLE signalzero.users IS 'Demo data includes 5 users across all subscription tiers with realistic usage patterns';
COMMENT ON TABLE signalzero.analyses IS 'Demo data includes viral product analyses with hardcoded Reality Scores for consistent demos';
COMMENT ON TABLE signalzero.wall_of_shame IS 'Demo data includes high-manipulation products automatically populated by triggers';
