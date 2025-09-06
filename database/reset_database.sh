#!/bin/bash

# S1GNAL.ZERO Database Reset Script
# Resets the SignalZero database for development and testing
# Reference: DETAILED_DESIGN.md Section 6

set -e  # Exit on any error

# Configuration
DB_NAME="${DB_NAME:-signalzero}"
DB_USER="${DB_USER:-postgres}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🔄 S1GNAL.ZERO Database Reset Starting...${NC}"

# Check if database exists
if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -lqt | cut -d \| -f 1 | grep -qw "$DB_NAME"; then
    echo -e "${YELLOW}📋 Database '$DB_NAME' exists. Dropping...${NC}"
    dropdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" --if-exists
    echo -e "${GREEN}✅ Database dropped successfully${NC}"
else
    echo -e "${YELLOW}📋 Database '$DB_NAME' does not exist${NC}"
fi

# Create fresh database
echo -e "${YELLOW}🏗️  Creating fresh database '$DB_NAME'...${NC}"
createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
echo -e "${GREEN}✅ Database created successfully${NC}"

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Apply schema files in order
echo -e "${YELLOW}📊 Applying database schema (01_create_schema.sql)...${NC}"
if [ -f "$SCRIPT_DIR/01_create_schema.sql" ]; then
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SCRIPT_DIR/01_create_schema.sql" -q
    echo -e "${GREEN}✅ Schema applied successfully${NC}"
else
    echo -e "${RED}❌ Schema file not found: $SCRIPT_DIR/01_create_schema.sql${NC}"
    exit 1
fi

echo -e "${YELLOW}⚙️  Applying database functions (02_create_functions.sql)...${NC}"
if [ -f "$SCRIPT_DIR/02_create_functions.sql" ]; then
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SCRIPT_DIR/02_create_functions.sql" -q
    echo -e "${GREEN}✅ Functions applied successfully${NC}"
else
    echo -e "${RED}❌ Functions file not found: $SCRIPT_DIR/02_create_functions.sql${NC}"
    exit 1
fi

echo -e "${YELLOW}👁️  Applying database views (03_create_views.sql)...${NC}"
if [ -f "$SCRIPT_DIR/03_create_views.sql" ]; then
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SCRIPT_DIR/03_create_views.sql" -q
    echo -e "${GREEN}✅ Views applied successfully${NC}"
else
    echo -e "${RED}❌ Views file not found: $SCRIPT_DIR/03_create_views.sql${NC}"
    exit 1
fi

echo -e "${YELLOW}🌱 Seeding demo data (04_seed_demo_data.sql)...${NC}"
if [ -f "$SCRIPT_DIR/04_seed_demo_data.sql" ]; then
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SCRIPT_DIR/04_seed_demo_data.sql" -q
    echo -e "${GREEN}✅ Demo data seeded successfully${NC}"
else
    echo -e "${YELLOW}⚠️  Demo data file not found: $SCRIPT_DIR/04_seed_demo_data.sql (skipping)${NC}"
fi

# Verify the setup
echo -e "${YELLOW}🔍 Verifying database setup...${NC}"
TABLE_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';")
FUNCTION_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT count(*) FROM information_schema.routines WHERE routine_schema = 'public' AND routine_type = 'FUNCTION';")
VIEW_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT count(*) FROM information_schema.views WHERE table_schema = 'public';")
USER_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT count(*) FROM users;" 2>/dev/null || echo "0")
ANALYSIS_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT count(*) FROM analyses;" 2>/dev/null || echo "0")
WALL_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT count(*) FROM wall_of_shame;" 2>/dev/null || echo "0")

echo -e "${GREEN}📊 Database Verification Results:${NC}"
echo -e "   • Tables: ${TABLE_COUNT// /} (expected: 8)"
echo -e "   • Functions: ${FUNCTION_COUNT// /} (expected: 5+)"
echo -e "   • Views: ${VIEW_COUNT// /} (expected: 6+)"
echo -e "   • Demo Users: ${USER_COUNT// /} (expected: 5)"
echo -e "   • Demo Analyses: ${ANALYSIS_COUNT// /} (expected: 5)"
echo -e "   • Wall of Shame: ${WALL_COUNT// /} (expected: 5)"

# Test Reality Score calculation
echo -e "${YELLOW}🧪 Testing Reality Score calculation...${NC}"
REALITY_SCORE=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT calculate_reality_score(38, 22, 27, 18);" 2>/dev/null || echo "ERROR")
if [ "$REALITY_SCORE" != "ERROR" ]; then
    EXPECTED="34.00"
    ACTUAL=$(echo "$REALITY_SCORE" | tr -d ' ')
    if [ "$ACTUAL" = "$EXPECTED" ]; then
        echo -e "${GREEN}✅ Reality Score function working: $ACTUAL (Stanley Cup demo value)${NC}"
    else
        echo -e "${YELLOW}⚠️  Reality Score calculation: $ACTUAL (expected: $EXPECTED)${NC}"
    fi
else
    echo -e "${RED}❌ Error testing Reality Score function${NC}"
fi

# Display connection info for development
echo -e "${GREEN}🎯 Database reset complete!${NC}"
echo -e "${YELLOW}📋 Connection Details:${NC}"
echo -e "   • Database: $DB_NAME"
echo -e "   • Host: $DB_HOST:$DB_PORT"
echo -e "   • User: $DB_USER"
echo -e ""
echo -e "${YELLOW}🚀 Ready for development! Start the Spring Boot application with:${NC}"
echo -e "   cd backend && mvn spring-boot:run"
echo -e ""
echo -e "${YELLOW}💡 Demo Login Credentials:${NC}"
echo -e "   • demo@s1gnalzero.com / password123 (PRO tier)"
echo -e "   • founder@s1gnalzero.com / password123 (ENTERPRISE tier)"
echo -e "   • free@example.com / password123 (FREE tier)"
echo -e ""
echo -e "${YELLOW}🎪 Demo Queries (hardcoded Reality Scores):${NC}"
echo -e "   • 'Stanley Cup' → 34% Reality Score (62% bots)"
echo -e "   • 'Prime Energy' → 29% Reality Score (71% bots)"
echo -e "   • '\$BUZZ' → 12% Reality Score (87% bots)"

# Check for common connection issues
if ! command -v psql &> /dev/null; then
    echo -e "${RED}⚠️  WARNING: 'psql' command not found. Install PostgreSQL client tools.${NC}"
fi

# Test connection
echo -e "${YELLOW}🔌 Testing database connection...${NC}"
if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 'Database connection successful!' as status;" >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Database connection test successful${NC}"
else
    echo -e "${RED}❌ Database connection test failed${NC}"
    echo -e "${YELLOW}💡 Troubleshooting:${NC}"
    echo -e "   • Check PostgreSQL service is running"
    echo -e "   • Verify connection parameters"
    echo -e "   • Ensure user $DB_USER has database creation privileges"
fi

echo -e "${GREEN}🛡️  S1GNAL.ZERO database reset completed successfully!${NC}"
