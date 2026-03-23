#!/bin/bash

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT" || exit 1

echo "Compilation de MG2D"
if ! find MG2D -name '*.java' -print0 | xargs -0 javac -cp "$ROOT"; then
    echo "Echec de compilation de MG2D" >&2
    exit 1
fi

echo "Compilation du menu de la borne d'arcade"
if ! javac -cp "$ROOT" *.java; then
    echo "Echec de compilation du menu" >&2
    exit 1
fi

game_failures=0
for dir in "$ROOT"/projet/*; do
    [ -d "$dir" ] || continue
    if find "$dir" -maxdepth 1 -name '*.java' -print -quit | grep -q .; then
        name="$(basename "$dir")"
        echo "Compilation du jeu $name"
        if ! (cd "$dir" && javac -cp .:"$ROOT" *.java); then
            echo "Compilation échouée pour $name" >&2
            game_failures=$((game_failures + 1))
        fi
    fi
done

if [ "$game_failures" -gt 0 ]; then
    echo "$game_failures jeu(x) Java n'ont pas compilé, mais le menu est prêt."
fi
