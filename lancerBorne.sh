#!/bin/bash

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT" || exit 1

if command -v setxkbmap >/dev/null 2>&1; then
    setxkbmap borne >/dev/null 2>&1 || true
fi

echo "Nettoyage des repertoires"
./clean.sh

echo "Compilation"
./compilation.sh || exit 1

echo "Lancement du menu"
exec java -cp "$ROOT" Main
