#!/usr/bin/env bash

# Exit on any error
set -e

# Create virtual environment in .venv if it does not exist
if [ ! -d ".venv" ]; then
  echo "Creating virtual environment..."
  python3 -m venv .venv
fi

# Activate the virtual environment
source .venv/bin/activate

# Upgrade pip and install dependencies
pip install --upgrade pip
pip install -r requirements.txt

echo "Setup complete. To start the app, run ./run.sh"

# ------------------------------------------------------------
# Auto‑activate .venv when entering this directory
# ------------------------------------------------------------
ZSHRC="$HOME/.zshrc"
MARKER="# >>> ZIP‑PT auto‑activate .venv >>>"
if ! grep -F "$MARKER" "$ZSHRC" > /dev/null 2>&1; then
  echo "" >> "$ZSHRC"
  echo "$MARKER" >> "$ZSHRC"
  echo "function _zippt_auto_activate() {" >> "$ZSHRC"
  echo "  if [ -f .venv/bin/activate ]; then" >> "$ZSHRC"
  echo "    source .venv/bin/activate" >> "$ZSHRC"
  echo "  fi" >> "$ZSHRC"
  echo "}" >> "$ZSHRC"
  echo "add-zsh-hook chpwd _zippt_auto_activate" >> "$ZSHRC"
  echo "# <<< ZIP‑PT auto‑activate .venv <<<" >> "$ZSHRC"
  echo "Auto‑activation hook added to $ZSHRC. Restart terminal or run 'source $ZSHRC' to apply." >> "$ZSHRC"
fi
