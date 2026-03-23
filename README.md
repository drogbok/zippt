# ZIP‑PT 대시보드

A premium **Streamlit** prototype for the intelligent real‑estate service **집피티 (ZIP‑PT)**.

---

## 📦 Project Structure
```
zippt/
├─ app.py            # Streamlit entry‑point (orchestrates UI)
├─ data.py           # Mocked datasets (properties, auctions, price trend)
├─ style.py          # CSS injection helper
├─ charts.py         # Plotly chart factories (risk gauge, price trend)
├─ requirements.txt  # Python dependencies
├─ setup.sh          # One‑click virtual‑env creation & install
├─ run.sh            # Shortcut to launch the app
├─ .gitignore        # Excludes venv, caches, OS files
└─ README.md         # This document
```

---

## 🛠️ Quick‑Start Guide
> **All commands are run from the project root** (`/Users/sqa/zippt`).

1. **Clone / navigate to the repo**
   ```bash
   # If you haven't cloned yet
   git clone https://github.com/your‑org/zippt.git
   cd zippt
   # Or, if the folder already exists
   cd /Users/sqa/zippt
   ```

2. **Make helper scripts executable**
   ```bash
   chmod +x setup.sh run.sh
   ```
   *(The previous attempt failed because `run.sh` did not exist yet – we will create it next.)*

3. **Create `run.sh` (one‑time step)**
   ```bash
   cat <<'EOF' > run.sh
   #!/usr/bin/env bash
   source .venv/bin/activate
   streamlit run app.py
   EOF
   chmod +x run.sh
   ```

4. **Initial environment setup** – creates a virtual environment and installs all dependencies.
   ```bash
   ./setup.sh
   ```
   This script:
   * Creates `.venv` if it does not exist.
   * Activates the venv.
   * Upgrades `pip`.
   * Installs the packages listed in `requirements.txt`.
   * Prints *"Setup complete. To start the app, run ./run.sh"*.

5. **Run the dashboard**
   ```bash
   ./run.sh
   ```
   Open the URL shown in the terminal (usually `http://localhost:8501`).

---

## 📚 What the scripts do
| Script | Purpose |
|--------|---------|
| `setup.sh` | Automates virtual‑env creation & dependency installation. |
| `run.sh`   | Activates the venv and launches the Streamlit app with a single command. |
| `style.py` | Holds the premium CSS injected into Streamlit via `inject_css()`. |
| `data.py`  | Generates mocked property, auction, and price‑trend data. |
| `charts.py`| Provides reusable Plotly chart functions (`risk_gauge`, `price_trend_chart`). |
| `app.py`   | Main UI – imports the helpers above and builds the role‑based dashboard. |

---

## 🚀 Deploying / Sharing
* Push the repository (including `setup.sh`, `run.sh`, `requirements.txt`, `.gitignore`, and the Python modules) to GitHub.
* Team members only need to run steps **2‑5** on their machines – the virtual environment guarantees a consistent Python stack.

---

## ❓ Troubleshooting
* **`pip` not found** – ensure you are inside the virtual environment (`source .venv/bin/activate`).
* **Permission denied** – double‑check the `chmod +x …` step.
* **Port already in use** – run `streamlit run app.py --server.port <unused_port>`.

---

*Happy coding! 🎉*
