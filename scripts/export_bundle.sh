#!/usr/bin/env bash
set -euo pipefail

OUT_FILE=${1:-langmaster-work.bundle}
BRANCH=${2:-work}

echo "Creating bundle for branch '$BRANCH' -> $OUT_FILE"
git bundle create "$OUT_FILE" "$BRANCH"
echo "Bundle created: $OUT_FILE"
