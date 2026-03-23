# -*- coding: utf-8 -*-
"""Plotly chart utilities for ZIP‑PT Enterprise Dashboard.
Matches Deep Navy theme with high-contrast highlights.
"""
import numpy as np
import plotly.graph_objects as go
import pandas as pd

def risk_gauge(risk_index: int = None) -> go.Figure:
    """Enterprise-style gauge chart showing risk index (0‑100)."""
    if risk_index is None:
        risk_index = np.random.randint(20, 85)
    
    gauge = go.Figure(
        go.Indicator(
            mode="gauge+number",
            value=risk_index,
            title={"text": "리스크 지수", "font": {"size": 24, "color": "#64FFDA"}},
            number={"font": {"size": 40, "color": "#FFFFFF"}},
            gauge={
                "axis": {"range": [0, 100], "tickcolor": "#CCD6F6"},
                "bar": {"color": "#64FFDA"}, # Mint Cyan
                "bgcolor": "rgba(0,0,0,0)",
                "borderwidth": 2,
                "bordercolor": "#233554",
                "steps": [
                    {"range": [0, 30], "color": "rgba(100, 255, 218, 0.4)"},
                    {"range": [30, 70], "color": "rgba(255, 171, 0, 0.4)"},
                    {"range": [70, 100], "color": "rgba(255, 82, 82, 0.4)"},
                ],
            },
        )
    )
    
    gauge.update_layout(
        paper_bgcolor="rgba(0,0,0,0)",
        plot_bgcolor="rgba(0,0,0,0)",
        font={"family": "Inter, sans-serif", "color": "#CCD6F6"},
        margin=dict(r=30, t=100, l=30, b=30),
    )
    return gauge

def price_trend_chart(df: pd.DataFrame) -> go.Figure:
    """Enterprise-style line chart of average price over time."""
    line = go.Figure()
    line.add_trace(
        go.Scatter(
            x=df["date"],
            y=df["avg_price"],
            mode="lines+markers",
            line=dict(color="#64FFDA", width=3), # Mint Cyan Line
            marker=dict(size=8, color="#FFFFFF", line=dict(color="#64FFDA", width=2)),
            name="평균 가격",
            fill='tozeroy', 
            fillcolor='rgba(100, 255, 218, 0.05)' # Gentle fill glow
        )
    )
    
    line.update_layout(
        title="시장 시세 추이",
        title_font={"size": 24, "color": "#64FFDA"},
        template="plotly_dark", # Use dark base for better visibility
        paper_bgcolor="rgba(0,0,0,0)",
        plot_bgcolor="rgba(0,0,0,0)",
        font={"family": "Inter, sans-serif", "color": "#CCD6F6"},
        xaxis=dict(showgrid=False, linecolor="#233554"),
        yaxis=dict(showgrid=True, gridcolor="#233554", linecolor="#233554"),
        hovermode="x unified",
        margin=dict(r=30, t=100, l=30, b=30),
    )
    return line
