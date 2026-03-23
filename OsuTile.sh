#!/bin/bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/projet/OsuTile" || exit 1
python3 main.py
