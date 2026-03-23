#!/bin/bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
command -v xdotool >/dev/null 2>&1 && xdotool mousemove 1280 1024 >/dev/null 2>&1 || true
cd "$ROOT/projet/Puissance_X" || exit 1
java -Dsun.java2d.pmoffscreen=false -cp .:"$ROOT" Main

# -Dsun.java2d.pmoffscreen=false : Améliore les performances sur les système Unix utilisant X11 (donc Raspbian est concerné).
# -Dsun.java2d.opengl=true : Utilise OpenGL (peut améliorer les performances).
