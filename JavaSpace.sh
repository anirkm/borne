#!/bin/bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
command -v xdotool >/dev/null 2>&1 && xdotool mousemove 1280 1024 >/dev/null 2>&1 || true
cd "$ROOT/projet/JavaSpace" || exit 1
touch highscore
java -cp .:"$ROOT" Main
