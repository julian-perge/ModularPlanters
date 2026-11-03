#!/usr/bin/env bash
# Prints the notes for one version from the changelog, which the release uses for GitHub, CurseForge and Modrinth.
# The notes are the lines under the "## <tag>" heading up to the next "## " heading, without the blank lines around them.
# Usage: changelog-section.sh <tag> [changelog file]
set -euo pipefail

tag="${1:?usage: changelog-section.sh <tag> [changelog file]}"
file="${2:-CHANGELOG.md}"

section=$(awk -v heading="## $tag" '
  $0 == heading { inside = 1; next }
  inside && /^## / { exit }
  inside && !started && /^[[:space:]]*$/ { next }
  inside { started = 1; print }
' "$file")

if [[ -z "$section" ]]; then
  echo "$file has no notes under \"## $tag\"" >&2
  exit 1
fi
printf '%s\n' "$section"
