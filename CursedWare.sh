#!/bin/bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/projet/CursedWare" || exit 1
if ! command -v love >/dev/null 2>&1; then
    echo "love n'est pas installe" >&2
    exit 1
fi
love .
