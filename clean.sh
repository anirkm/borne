#!/bin/bash

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT" || exit 1

find . \( -name '*.class' -o -name '*~' \) -delete
