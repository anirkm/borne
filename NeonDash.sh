#!/bin/bash

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/projet/NeonDash" || exit 1
touch highscore
java -cp .:"$ROOT" Main
