# -*- coding: utf-8 -*-
"""
Streamlit entry‑point for the ZIP‑PT dashboard.
Restructured with 'Middle Dashboard' layout for better decision flow.
"""

import streamlit as st
from datetime import datetime, time, timedelta
import numpy as np

# 1️⃣ Page Config
st.set_page_config(page_title="ZIP‑PT 대시보드", layout="wide")

# 2️⃣ Import local modules
from style import inject_css
from data import properties, auction_df, price_trend
from charts import risk_gauge, price_trend_chart

# 3️⃣ Global UI setup
inject_css()

# Sidebar – role selection
st.sidebar.title("🔑 로그인")
role = st.sidebar.selectbox(
    "사용자 유형 선택",
    ["매수자 (Buyer)", "매도자 (Seller)", "중개사 (Agent)"]
)

# Header
st.markdown("<h1 class='title'>🏠 집피티 (ZIP‑PT) 대시보드</h1>", unsafe_allow_html=True)

# Helper for the middle dashboard
def render_dashboard():
    st.markdown("---")
    st.caption("📈 시장 데이터 및 리스크 지수 (Middle Insights)")
    col1, col2 = st.columns(2)
    with col1:
        st.plotly_chart(risk_gauge(), use_container_width=True)
    with col2:
        st.plotly_chart(price_trend_chart(price_trend), use_container_width=True)
    st.markdown("---")

# ----------------------------------------------------------------------
# 5️⃣ Role‑specific sections
# ----------------------------------------------------------------------
if "매수자" in role:
    # --- Part 1: Exploration ---
    st.subheader("🗺️ 매물 지도 (상세 정보 확인 가능)")
    import plotly.express as px
    fig = px.scatter_mapbox(
        properties,
        lat="lat",
        lon="lon",
        hover_name="name",
        hover_data={"lat": False, "lon": False, "price_per_m2": ":,d"},
        zoom=10.5,
        height=500,
    )
    fig.update_traces(
        marker=dict(size=15, opacity=1.0, color="#1E3A8A") # Navy markers
    )
    fig.update_layout(
        mapbox_style="open-street-map", # Fixed standard ASCII hyphen
        margin={"r":0,"t":0,"l":0,"b":0}
    )
    st.plotly_chart(fig, use_container_width=True, config={'scrollZoom': True})

    # --- Part 2: Middle Insights ---
    render_dashboard()

    # --- Part 3: Action ---
    st.subheader("📅 예약 캘린더")
    today = datetime.today()
    selected_date = st.date_input("예약 날 짜 선택", value=today, min_value=today, max_value=today + timedelta(days=30))
    slots = [time(h, 0) for h in range(9, 18)]
    if 'unavailable_slots' not in st.session_state:
        st.session_state.unavailable_slots = [slots[i] for i in [1, 4, 6]]
    unavail = st.session_state.unavailable_slots
    slot_labels = [f"{s.strftime('%H:%M')}{' (불가)' if s in unavail else ''}" for s in slots]
    selected_slot = st.selectbox("예약 시간 선택", slot_labels)
    if "불가" in selected_slot:
        st.warning("선택한 시간은 현재 예약이 불가능합니다.")
    else:
        if st.button("예약 요청"):
            st.success(f"{selected_date} {selected_slot}에 예약이 완료되었습니다.")

elif "매도자" in role:
    # --- Part 1: Exploration ---
    st.subheader("📊 실시간 역경매 입찰 현황")
    st.dataframe(auction_df, use_container_width=True)

    # --- Part 2: Middle Insights ---
    render_dashboard()

    # --- Part 3: Action ---
    st.subheader("💰 낙찰 결정 (중개사 선정)")
    prop = st.selectbox("낙찰할 매물 선택", auction_df["Property"])
    bid_amount = st.number_input("입찰 금액 (₩)", min_value=0, step=10_000, value=1_000_000)
    if st.button("낙찰하기"):
        st.success(f"{prop}에 {bid_amount:,}원으로 낙찰했습니다!")

elif "중개사" in role:
    # --- Part 1: Exploration ---
    st.subheader("📊 실시간 역경매 현황 (입찰 참여중)")
    st.dataframe(auction_df, use_container_width=True)

    # --- Part 2: Middle Insights ---
    render_dashboard()

    # --- Part 3: Action ---
    st.subheader("💼 중개 입찰 (중개가 수수료 기반)")
    prop = st.selectbox("매물 선택", auction_df["Property"], key="agent_prop")
    bid_amount = st.number_input("입찰 금액 (₩)", min_value=0, step=10_000, key="agent_bid")
    fee_amount = st.number_input("중개보수 (%)", min_value=0.0, step=0.1, value=0.0, key="agent_fee")
    if st.button("중개 입찰"):
        st.success(f"매물 {prop}에 대한 중개 입찰(입찰가: {bid_amount:,}원, 보수: {fee_amount:.1f})이 완료되었습니다!")

# ----------------------------------------------------------------------
# 6️⃣ Review component (available for all roles)
# ----------------------------------------------------------------------
st.markdown("---")
st.subheader("⭐ 방문 후기 남기기")
col1, col2 = st.columns([1, 3])
with col1:
    rating = st.radio("별점", options=[1, 2, 3, 4, 5], index=4, horizontal=True, key="rating")
with col2:
    review_text = st.text_area("리뷰 (선택)", placeholder="방문 후기를 남겨 주세요...", height=80)
if st.button("리뷰 제출"):
    if not review_text.strip():
        st.warning("리뷰 내용을 입력해주세요.")
    else:
        st.success(f"감사합니다! {rating}점 리뷰가 등록되었습니다.")

# Footer
st.markdown("<p style='text-align:center; color:#6B7280; font-size:0.9rem'>© 2026 ZIP‑PT. All rights reserved.</p>", unsafe_allow_html=True)