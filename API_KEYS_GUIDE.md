# API Keys Setup - S1GNAL.ZERO

Quick setup guide for hackathon APIs.
test 
---

## 🔴 REQUIRED (Critical)

### 1. Stripe (Revenue)
1. Sign up: https://stripe.com
2. Get test keys: Dashboard → Developers → API keys
3. Create webhook: `http://localhost:8081/webhook/stripe`

```
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

### 2. Database
```bash
createdb signalzero
```
```
DB_URL=jdbc:postgresql://localhost:5432/signalzero
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### 3. Solace
```bash
docker run -d -p 55555:55555 -p 8080:8080 --name=solace solace/solace-pubsub-standard
```
```
SOLACE_HOST=tcp://localhost:55555
SOLACE_VPN=default
SOLACE_USERNAME=admin
SOLACE_PASSWORD=admin
```

---

## 🟡 OPTIONAL (Free APIs)

### Reddit API (5 min)
1. Go to: https://www.reddit.com/prefs/apps
2. Create app (script type)

```
REDDIT_CLIENT_ID=your_client_id
REDDIT_CLIENT_SECRET=your_secret_key
```

### YouTube API (10 min)  
1. Google Cloud Console: https://console.cloud.google.com
2. Create project → Enable YouTube Data API v3 → Create API key

```
YOUTUBE_API_KEY=AIzaSy...
```

### News API (3 min)
1. Sign up: https://newsapi.org
2. Get API key

```
NEWSAPI_KEY=your_key
```

### Twitter API ($100/month)
1. Apply: https://developer.twitter.com
2. Create app → Get bearer token

```
TWITTER_BEARER_TOKEN=AAAAAAAAAA...
```

---

## 📁 Environment Files

### Backend `.env`
```properties
# Required
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...
DB_URL=jdbc:postgresql://localhost:5432/signalzero
SOLACE_HOST=tcp://localhost:55555

# Optional
REDDIT_CLIENT_ID=your_id
YOUTUBE_API_KEY=your_key
NEWSAPI_KEY=your_key
TWITTER_BEARER_TOKEN=your_token

# Fallback
DEMO_MODE=true
```

### Python `.env`
```bash
SOLACE_HOST=tcp://localhost:55555
SOLACE_USERNAME=admin
SOLACE_PASSWORD=admin
USE_MOCK_DATA=true
```

---

## 🚀 Hackathon Priority

**Hour 0**: Just Stripe + Solace + Database  
**Hour 2**: Add Reddit + YouTube (both free)  
**Skip**: Twitter (needs approval), Instagram (complex), paid APIs

**Remember**: System works with mock data if APIs fail!
