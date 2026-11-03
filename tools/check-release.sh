#!/usr/bin/env bash
# Checks a release tag before anything is built or published: the tag must have the release format, equal the version
# in gradle.properties, and have notes in the changelog. The release workflow and `just release-check` both run it.
# Usage: check-release.sh <tag> [repo dir]
set -euo pipefail

tag="${1:?usage: check-release.sh <tag> [repo dir]}"
repo="${2:-.}"
tools=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# The tag is <minecraft version>-<mod version>, such as 26.1.2-0.1.0 or 26.3-0.2.0-beta.
# Every tag of this shape matches the release workflow's trigger, and the build only knows the -alpha and -beta suffixes,
# so any other suffix would publish as a full release.
format='^[0-9]+\.[0-9]+(\.[0-9]+)?-[0-9]+\.[0-9]+\.[0-9]+(-(alpha|beta)(\.[0-9]+)?)?$'
if [[ ! "$tag" =~ $format ]]; then
  echo "tag $tag is not <minecraft version>-<mod version>, where the mod version is three numbers with an optional -alpha or -beta suffix" >&2
  exit 1
fi

expected=$(bash "$tools/release-version.sh" "$repo")
if [[ "$tag" != "$expected" ]]; then
  echo "tag $tag does not match gradle.properties, which gives $expected" >&2
  exit 1
fi
bash "$tools/changelog-section.sh" "$tag" "$repo/CHANGELOG.md" > /dev/null
echo "$tag is ready to release"
