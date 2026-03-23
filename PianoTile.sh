#!/bin/bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/projet/PianoTile" || exit 1
python3 app/game.py
