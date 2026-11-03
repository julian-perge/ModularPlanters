#!/usr/bin/env bash
# Checks a release tag before anything is built or published: the tag must equal the version in gradle.properties,
# and the changelog must have notes for it. The release workflow and `just release-check` both run it.
# Usage: check-release.sh <tag> [repo dir]
set -euo pipefail

tag="${1:?usage: check-release.sh <tag> [repo dir]}"
repo="${2:-.}"
tools=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

expected=$(bash "$tools/release-version.sh" "$repo")
if [[ "$tag" != "$expected" ]]; then
  echo "tag $tag does not match gradle.properties, which gives $expected" >&2
  exit 1
fi
bash "$tools/changelog-section.sh" "$tag" "$repo/CHANGELOG.md" > /dev/null
echo "$tag is ready to release"
