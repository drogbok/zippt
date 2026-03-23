# -*- coding: utf-8 -*-
"""
Rich Mock Data for ZIP‑PT Demonstration.
Includes various property clusters in Seoul, auction status, and realistic price trends.
"""
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

# 1️⃣ Property markers (Seoul clusters: Gangnam, Mapo, Seongsu, Yongsan)
properties = pd.DataFrame({
    "lat": [
        37.4979, 37.5005, 37.4950, # Gangnam cluster
        37.5500, 37.5550,          # Mapo cluster
        37.5400, 37.5450,          # Seongsu cluster
        37.5300, 37.5350, 37.5250  # Yongsan cluster
    ],
    "lon": [
        127.0276, 127.0300, 127.0250,
        126.9200, 126.9250,
        127.0500, 127.0550,
        126.9800, 126.9850, 126.9750
    ],
    "name": [
        "강남 자이 1단지", "강남 푸르지오", "역삼 래미안",
        "마포 래미안 푸르지오", "합정 메세나폴리스",
        "성수 아크로 포레스트", "트리마제",
        "용산 하이페리온", "한남 더 힐", "이촌 첼리투스"
    ],
    "price_per_m2": [
        3500, 3200, 3000,
        2800, 2600,
        5500, 5200,
        3400, 7500, 6200
    ]
})

# 2️⃣ Auction Status (Diversified bids and remaining times)
auction_df = pd.DataFrame({
    "Property": [
        "강남 자이 A-101", "성수 트리마제 B-502", "반포 래미안 C-303", 
        "압구정 현대 D-707", "마포 자이 E-101", "한남 더힐 F-202"
    ],
    "Status": ["입찰 중", "입찰 대기", "낙찰 완료", "입찰 중", "입찰 중", "입찰 중"],
    "CurrentBid": [12_500_000, 42_000_000, 28_000_000, 55_000_000, 15_000_000, 85_000_000],
    "TotalBidders": [12, 0, 5, 8, 3, 24],
    "RemainingTime": ["02:15:30", "08:12:00", "Ended", "00:45:12", "01:20:05", "00:05:45"]
})

# 3️⃣ Realistic Price Trend (Last 60 days with gradual growth)
base_price = 4500
dates = pd.date_range(end=datetime.today(), periods=60)
# Mocking a slight upward trend with some volatility
trend_values = base_price + np.cumsum(np.random.normal(5, 20, size=60))
price_trend = pd.DataFrame({
    "date": dates,
    "avg_price": trend_values.astype(int)
})

# 4️⃣ Pre-generated dummy reviews for display (extra)
mock_reviews = [
    {"user": "매수자A", "rating": 5, "text": "매물 상태가 사진보다 훨씬 좋아요!"},
    {"user": "중개인K", "rating": 4, "text": "상담이 빠르고 정확합니다."},
    {"user": "매도인H", "rating": 5, "text": "역경매 시스템 덕분에 좋은 가격에 체결했습니다."}
]
