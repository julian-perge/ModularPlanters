#!/usr/bin/env bash
# Prints the release version, <minecraft_version>-<mod_version>, read from gradle.properties.
# The release tag and the jar name both use this version.
# Usage: release-version.sh [repo dir]
set -euo pipefail

repo="${1:-.}"

property() {
  awk -v key="$1" 'index($0, key "=") == 1 { print substr($0, length(key) + 2); exit }' "$repo/gradle.properties"
}

minecraft=$(property minecraft_version)
mod=$(property mod_version)
if [[ -z "$minecraft" || -z "$mod" ]]; then
  echo "gradle.properties in $repo needs both minecraft_version and mod_version" >&2
  exit 1
fi
printf '%s-%s\n' "$minecraft" "$mod"
