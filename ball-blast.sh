#!/bin/bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/projet/ball-blast" || exit 1
python3 ./src/__main__.py
