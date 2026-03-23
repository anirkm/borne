#!/bin/bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/projet/TronGame" || exit 1
python3 main.py
