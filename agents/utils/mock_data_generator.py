"""
Mock Data Generator for S1GNAL.ZERO Agents
Generates realistic mock data when real APIs are unavailable.
"""

import random
import string
from typing import Dict, Any, List
from datetime import datetime, timedelta


class MockDataGenerator:
    """
    Generates realistic mock data for all agent types
    """
    
    def __init__(self):
        self.viral_products = [
            "Stanley Cup Tumbler", "Prime Energy Drink", "Grimace Shake",
            "Dubai Chocolate", "Viral TikTok Product", "Trending Amazon Item",
            "Influencer Favorite", "Must-Have Gadget", "Viral Beauty Product"
        ]
        
        self.platforms = ["twitter", "instagram", "tiktok", "youtube", "reddit", "amazon"]
        
        self.suspicious_usernames = [
            "user{}", "account{}", "bot{}", "fake{}", "temp{}", 
            "new{}", "random{}", "guest{}", "anon{}", "test{}"
        ]
        
    def generate_bot_detection_data(self, query: str, total_accounts: int = None) -> Dict[str, Any]:
        """Generate mock bot detection data"""
        
        if total_accounts is None:
            total_accounts = random.randint(500, 5000)
            
        # Determine suspiciousness based on query
        suspiciousness = self._get_query_suspiciousness(query)
        
        # Generate account data
        new_accounts = int(total_accounts * random.uniform(0.2, 0.6) * suspiciousness)
        default_avatars = int(total_accounts * random.uniform(0.3, 0.7) * suspiciousness)
        suspicious_usernames = int(total_accounts * random.uniform(0.1, 0.4) * suspiciousness)
        
        return {
            "total_accounts": total_accounts,
            "new_accounts": new_accounts,
            "default_avatars": default_avatars,
            "suspicious_usernames": suspicious_usernames,
            "burst_creation": suspiciousness > 0.6,
            "cluster_detected": suspiciousness > 0.7,
            "sample_suspicious_accounts": self._generate_suspicious_usernames(5)
        }
        
    def generate_trend_data(self, query: str, hours: int = 168) -> List[Dict[str, Any]]:
        """Generate mock trend data points"""
        
        suspiciousness = self._get_query_suspiciousness(query)
        base_volume = random.randint(100, 1000)
        
        data_points = []
        
        for hour in range(hours):
            timestamp = datetime.now() - timedelta(hours=hours - hour)
            
            # Generate volume based on suspiciousness
            if suspiciousness > 0.7 and 48 <= hour <= 72:  # Artificial spike
                volume = int(base_volume * random.uniform(5, 15))
            elif suspiciousness > 0.5:  # Coordinated campaign
                if hour % 24 in [9, 13, 17, 21]:
                    volume = int(base_volume * random.uniform(2, 5))
                else:
                    volume = int(base_volume * random.uniform(0.8, 1.5))
            else:  # Organic growth
                growth_factor = 1 + (hour / hours) * 2
                volume = int(base_volume * growth_factor * random.uniform(0.8, 1.2))
                
            data_points.append({
                "timestamp": timestamp.isoformat(),
                "volume": volume,
                "mentions": random.randint(int(volume * 0.1), int(volume * 0.3)),
                "engagement_rate": random.uniform(0.02, 0.15),
                "unique_users": random.randint(int(volume * 0.6), int(volume * 0.9))
            })
            
        return data_points
        
    def generate_review_data(self, query: str, total_reviews: int = None) -> List[Dict[str, Any]]:
        """Generate mock review data"""
        
        if total_reviews is None:
            total_reviews = random.randint(100, 3000)
            
        suspiciousness = self._get_query_suspiciousness(query)
        reviews = []
        
        for i in range(total_reviews):
            # Determine review characteristics based on suspiciousness
            if suspiciousness > 0.7:  # Fake surge pattern
                days_ago = random.randint(1, 30) if i < total_reviews * 0.7 else random.randint(31, 365)
                rating = random.choices([5, 4, 3, 2, 1], weights=[70, 15, 8, 4, 3])[0]
                verified = random.choice([True, False])
                template_similarity = random.uniform(0.6, 0.9)
            elif suspiciousness > 0.5:  # Bot reviews
                days_ago = random.randint(1, 180)
                rating = random.choices([5, 4, 3, 2, 1], weights=[80, 10, 5, 3, 2])[0]
                verified = random.choice([True, False])
                template_similarity = random.uniform(0.7, 0.95)
            else:  # Organic reviews
                days_ago = random.randint(1, 365)
                rating = random.choices([5, 4, 3, 2, 1], weights=[35, 30, 20, 10, 5])[0]
                verified = random.choice([True, True, False])
                template_similarity = random.uniform(0.1, 0.4)
                
            reviews.append({
                "id": f"review_{i}",
                "rating": rating,
                "date": datetime.now() - timedelta(days=days_ago),
                "verified_purchase": verified,
                "reviewer_id": f"user_{random.randint(1000, 99999)}",
                "review_length": random.randint(20, 300),
                "helpful_votes": random.randint(0, 15),
                "template_similarity": template_similarity
            })
            
        return reviews
        
    def generate_promotion_data(self, query: str, total_content: int = None) -> List[Dict[str, Any]]:
        """Generate mock promotional content data"""
        
        if total_content is None:
            total_content = random.randint(50, 500)
            
        suspiciousness = self._get_query_suspiciousness(query)
        content_items = []
        
        for i in range(total_content):
            days_ago = random.randint(1, 90)
            
            # Determine content characteristics based on suspiciousness
            if suspiciousness > 0.7:  # Undisclosed campaign
                has_disclosure = random.choice([False, False, False, True])
                engagement_rate = random.uniform(0.08, 0.25)
                promotional_keywords = random.randint(3, 8)
            elif suspiciousness > 0.5:  # Coordinated influencers
                has_disclosure = random.choice([False, False, True])
                engagement_rate = random.uniform(0.06, 0.20)
                promotional_keywords = random.randint(2, 6)
            else:  # Organic content
                has_disclosure = random.choice([True, False, False, False])
                engagement_rate = random.uniform(0.02, 0.08)
                promotional_keywords = random.randint(0, 2)
                
            content_items.append({
                "id": f"content_{i}",
                "platform": random.choice(self.platforms),
                "creator_id": f"influencer_{random.randint(1000, 9999)}",
                "follower_count": random.randint(10000, 1000000),
                "date": datetime.now() - timedelta(days=days_ago),
                "has_disclosure": has_disclosure,
                "disclosure_type": "#ad" if has_disclosure else "none",
                "engagement_rate": engagement_rate,
                "typical_engagement": random.uniform(0.02, 0.06),
                "promotional_keywords": promotional_keywords,
                "brand_mentions": random.randint(0, 5),
                "call_to_action": random.choice([True, False]),
                "discount_code": random.choice([True, False])
            })
            
        return content_items
        
    def generate_social_media_accounts(self, count: int, suspicious_ratio: float = 0.3) -> List[Dict[str, Any]]:
        """Generate mock social media account data"""
        
        accounts = []
        suspicious_count = int(count * suspicious_ratio)
        
        for i in range(count):
            is_suspicious = i < suspicious_count
            
            if is_suspicious:
                # Generate suspicious account
                username = random.choice(self.suspicious_usernames).format(random.randint(1000, 99999))
                account_age_days = random.randint(1, 30)
                has_default_avatar = random.choice([True, True, False])
                follower_count = random.randint(0, 100)
                following_count = random.randint(1000, 5000)
                post_count = random.randint(0, 10)
            else:
                # Generate normal account
                username = self._generate_normal_username()
                account_age_days = random.randint(30, 2000)
                has_default_avatar = random.choice([False, False, True])
                follower_count = random.randint(50, 5000)
                following_count = random.randint(100, 1000)
                post_count = random.randint(10, 500)
                
            accounts.append({
                "username": username,
                "account_age_days": account_age_days,
                "has_default_avatar": has_default_avatar,
                "follower_count": follower_count,
                "following_count": following_count,
                "post_count": post_count,
                "is_verified": random.choice([False, False, False, True]),
                "bio_length": random.randint(0, 150),
                "last_post_days_ago": random.randint(0, 30)
            })
            
        return accounts
        
    def _get_query_suspiciousness(self, query: str) -> float:
        """Determine suspiciousness level based on query"""
        query_lower = query.lower()
        
        # High suspiciousness indicators
        if any(term in query_lower for term in ["viral", "trending", "bot", "fake", "stanley", "prime", "$buzz"]):
            return random.uniform(0.7, 0.9)
        elif any(term in query_lower for term in ["product", "launch", "new", "popular"]):
            return random.uniform(0.5, 0.7)
        elif any(term in query_lower for term in ["review", "recommendation", "must-have"]):
            return random.uniform(0.4, 0.6)
        else:
            return random.uniform(0.2, 0.4)
            
    def _generate_suspicious_usernames(self, count: int) -> List[str]:
        """Generate suspicious usernames"""
        usernames = []
        for _ in range(count):
            template = random.choice(self.suspicious_usernames)
            number = random.randint(1000, 99999)
            usernames.append(template.format(number))
        return usernames
        
    def _generate_normal_username(self) -> str:
        """Generate a normal-looking username"""
        patterns = [
            lambda: f"{random.choice(['john', 'jane', 'mike', 'sarah', 'alex', 'chris'])}{random.randint(80, 99)}",
            lambda: f"{random.choice(['music', 'photo', 'travel', 'food', 'art'])}_lover_{random.randint(1, 999)}",
            lambda: f"{random.choice(['real', 'official', 'the'])}{random.choice(['artist', 'writer', 'creator'])}",
            lambda: ''.join(random.choices(string.ascii_lowercase, k=random.randint(6, 12)))
        ]
        
        return random.choice(patterns)()
        
    def generate_api_response_mock(self, api_type: str, query: str) -> Dict[str, Any]:
        """Generate mock API response for different services"""
        
        if api_type == "twitter":
            return self._generate_twitter_mock(query)
        elif api_type == "reddit":
            return self._generate_reddit_mock(query)
        elif api_type == "youtube":
            return self._generate_youtube_mock(query)
        elif api_type == "google_trends":
            return self._generate_google_trends_mock(query)
        elif api_type == "news":
            return self._generate_news_mock(query)
        else:
            return {"error": f"Unknown API type: {api_type}"}
            
    def _generate_twitter_mock(self, query: str) -> Dict[str, Any]:
        """Generate mock Twitter API response"""
        return {
            "data": [
                {
                    "id": f"tweet_{i}",
                    "text": f"Mock tweet about {query} #{random.randint(1, 100)}",
                    "author_id": f"user_{random.randint(1000, 9999)}",
                    "created_at": (datetime.now() - timedelta(hours=random.randint(1, 48))).isoformat(),
                    "public_metrics": {
                        "retweet_count": random.randint(0, 1000),
                        "like_count": random.randint(0, 5000),
                        "reply_count": random.randint(0, 100),
                        "quote_count": random.randint(0, 50)
                    }
                }
                for i in range(random.randint(10, 100))
            ],
            "meta": {
                "result_count": random.randint(10, 100)
            }
        }
        
    def _generate_reddit_mock(self, query: str) -> Dict[str, Any]:
        """Generate mock Reddit API response"""
        return {
            "data": {
                "children": [
                    {
                        "data": {
                            "id": f"post_{i}",
                            "title": f"Mock Reddit post about {query}",
                            "author": f"user_{random.randint(1000, 9999)}",
                            "created_utc": (datetime.now() - timedelta(hours=random.randint(1, 168))).timestamp(),
                            "score": random.randint(-10, 1000),
                            "num_comments": random.randint(0, 500),
                            "subreddit": random.choice(["products", "reviews", "trending", "viral"])
                        }
                    }
                    for i in range(random.randint(5, 50))
                ]
            }
        }
        
    def _generate_youtube_mock(self, query: str) -> Dict[str, Any]:
        """Generate mock YouTube API response"""
        return {
            "items": [
                {
                    "id": {"videoId": f"video_{i}"},
                    "snippet": {
                        "title": f"Mock YouTube video about {query}",
                        "channelTitle": f"Channel_{random.randint(100, 999)}",
                        "publishedAt": (datetime.now() - timedelta(days=random.randint(1, 30))).isoformat(),
                        "description": f"Mock description for {query} video"
                    },
                    "statistics": {
                        "viewCount": str(random.randint(1000, 1000000)),
                        "likeCount": str(random.randint(10, 50000)),
                        "commentCount": str(random.randint(5, 5000))
                    }
                }
                for i in range(random.randint(5, 20))
            ]
        }
        
    def _generate_google_trends_mock(self, query: str) -> Dict[str, Any]:
        """Generate mock Google Trends data"""
        return {
            "interest_over_time": [
                {
                    "date": (datetime.now() - timedelta(days=i)).strftime("%Y-%m-%d"),
                    "value": random.randint(0, 100)
                }
                for i in range(30, 0, -1)
            ],
            "related_queries": [
                f"{query} review",
                f"{query} price",
                f"buy {query}",
                f"{query} vs",
                f"is {query} good"
            ]
        }
        
    def _generate_news_mock(self, query: str) -> Dict[str, Any]:
        """Generate mock news API response"""
        return {
            "articles": [
                {
                    "title": f"Mock news article about {query}",
                    "description": f"Mock description of news about {query}",
                    "url": f"https://example.com/news/{i}",
                    "publishedAt": (datetime.now() - timedelta(days=random.randint(1, 7))).isoformat(),
                    "source": {"name": f"News Source {random.randint(1, 10)}"}
                }
                for i in range(random.randint(3, 15))
            ],
            "totalResults": random.randint(50, 500)
        }
