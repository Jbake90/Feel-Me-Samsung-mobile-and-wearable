#!/usr/bin/env bash
set -euo pipefail

if ! command -v git >/dev/null 2>&1; then
  echo "Git is required but not installed." >&2
  exit 1
fi

if [ $# -lt 1 ]; then
  echo "Usage: $(basename "$0") <repo-url> [branch]" >&2
  echo "Example: $(basename "$0") https://github.com/yourname/feel-me.git work" >&2
  exit 1
fi

REPO_URL="$1"
BRANCH_NAME="${2:-$(git rev-parse --abbrev-ref HEAD)}"

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$REPO_URL"
  echo "Updated existing origin to $REPO_URL"
else
  git remote add origin "$REPO_URL"
  echo "Added origin remote pointing to $REPO_URL"
fi

echo "Pushing branch $BRANCH_NAME to origin..."
git push -u origin "$BRANCH_NAME"
