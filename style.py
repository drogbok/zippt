# -*- coding: utf-8 -*-
import streamlit as st

def inject_css():
    st.markdown(
        """
        <style>
        /* 1. Deep Navy Enterprise Background */
        [data-testid="stAppViewContainer"] {
            background: radial-gradient(circle at top, #0A192F 0%, #020617 100%) !important;
            color: #CCD6F6 !important;
        }
        [data-testid="stHeader"], [data-testid="stToolbar"] {
            background: rgba(0,0,0,0) !important;
        }
        [data-testid="stSidebar"] {
            background-color: #020617 !important;
            border-right: 1px solid #1E293B;
        }

        /* 2. Custom Card Design */
        .card {
            background: rgba(17, 34, 64, 0.7);
            border-radius: 16px;
            border: 1px solid #233554;
            padding: 2rem;
            margin-bottom: 2rem;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
            backdrop-filter: blur(10px);
        }

        /* 3. High Contrast Titles */
        h1, h2, h3, .title {
            color: #64FFDA !important; /* Mint Cyan for tech feeling */
            font-family: 'Inter', sans-serif;
            font-weight: 850;
            letter-spacing: -0.02em;
        }
        
        /* 4. Glass-morphism Buttons */
        .stButton > button {
            background: linear-gradient(135deg, #64FFDA 0%, #48D1CC 100%);
            color: #0A192F !important;
            border-radius: 12px;
            padding: 0.7rem 2rem;
            font-weight: 800;
            border: none;
            transition: all 0.4s ease;
            box-shadow: 0 4px 14px rgba(100, 255, 218, 0.2);
        }
        .stButton > button:hover {
            transform: translateY(-3px);
            box-shadow: 0 8px 25px rgba(100, 255, 218, 0.4);
            filter: brightness(1.1);
        }
        
        /* Sidebar Text */
        [data-testid="stSidebar"] p, [data-testid="stSidebar"] h1 {
            color: #CCD6F6;
        }
        </style>
        """,
        unsafe_allow_html=True,
    )
