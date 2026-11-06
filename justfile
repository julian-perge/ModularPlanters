# Local shortcuts for building, testing, formatting and releasing Modular Planters. `just` lists them, and `just <recipe>` runs one.
#
# CI does not call just. The workflows in .github/workflows run the same Gradle tasks and tools/ scripts directly.

set shell := ["bash", "-euo", "pipefail", "-c"]

# Recipe arguments arrive as $1, $2 and so on, so variadic recipes forward them with "$@" and keep each argument intact.
set positional-arguments

[private]
default:
    @just --list --unsorted

# Recipes that call tools outside the JDK depend on this, so a missing tool stops the recipe before it starts. DEVELOPMENT.md says how to install each one.
[private]
require +tools:
    #!/usr/bin/env bash
    set -euo pipefail
    missing=()
    for tool in "$@"; do
        command -v "$tool" >/dev/null || missing+=("$tool")
    done
    if [ ${#missing[@]} -gt 0 ]; then
        echo "error: missing ${missing[*]}. See the Setup section of DEVELOPMENT.md." >&2
        exit 1
    fi

[doc('Compile, run the unit tests and build the jar into build/libs/')]
[group('gradle')]
build:
    ./gradlew build

[doc('Run the unit tests in src/test')]
[group('gradle')]
test:
    ./gradlew test

[doc('Run the in-game tests on a headless server, for example: just gametest -PnoAe2')]
[group('gradle')]
gametest *flags:
    ./gradlew runGameTestServer "$@"

[doc('Run the unit tests, then the in-game tests with all dev mods, without AE2, without Applied Flux and without Jade, the four ways CI runs them')]
[group('gradle')]
check:
    ./gradlew test
    ./gradlew runGameTestServer
    ./gradlew runGameTestServer -PnoAe2
    ./gradlew runGameTestServer -PnoAppliedFlux
    ./gradlew runGameTestServer -PnoJade

[doc('Start a dev client, for example: just client -PnoPowah')]
[group('gradle')]
client *flags:
    ./gradlew runClient "$@"

[doc('Start a dev dedicated server')]
[group('gradle')]
server *flags:
    ./gradlew runServer "$@"

[doc('Regenerate src/generated/resources')]
[group('gradle')]
data:
    ./gradlew runData

# The first run updates gradle-wrapper.properties with the old Gradle, and the second run lets the new Gradle rewrite gradlew and gradle-wrapper.jar to match it.
[doc('Upgrade the Gradle wrapper, for example: just gradle-update 9.8.2')]
[group('gradle')]
gradle-update version:
    ./gradlew wrapper --gradle-version "$1"
    ./gradlew wrapper --gradle-version "$1"

[doc('Run every pre-commit hook on every file, as CI does')]
[group('lint')]
lint: (require "prek")
    prek run --all-files --show-diff-on-failure

# The formatters exit 1 when they rewrite a file, so a second run decides the result: it passes when the first run fixed everything.
[doc('Format the Java, Markdown, YAML and JSON files in place')]
[group('lint')]
fmt: (require "prek")
    prek run --all-files pretty-format-java prettier || prek run --all-files pretty-format-java prettier

[doc('Lint the GitHub workflows with actrun and zizmor')]
[group('ci')]
ci-lint: (require "actrun" "zizmor")
    actrun lint
    zizmor --offline .github/workflows

# local-ci runs the jobs in Docker with GitHub's own runner, against the working tree including uncommitted changes.
[doc('Run the Build workflow locally in Docker, for example: just ci -p to pause on a failed step')]
[group('ci')]
ci *flags: (require "local-ci" "docker")
    @docker info >/dev/null 2>&1 || { echo "error: Docker is not running. Start Docker Desktop first." >&2; exit 1; }
    local-ci run --workflow .github/workflows/build.yml "$@"

[doc('Render the mod logo into docs/media/logo.png and src/main/resources/logo.png')]
[group('media')]
logo: (require "uv")
    uv run tools/render_logo.py

[doc('Compress PNG files in place, for example: just compress docs/media/*.png')]
[group('media')]
compress +files: (require "pngquant" "oxipng")
    pngquant --quality=80-95 --speed 1 --strip --force --ext .png "$@"
    oxipng -o 4 --strip safe "$@"

[doc('Print the changelog notes for one release tag, for example: just changelog 26.1.2-0.1.0')]
[group('release')]
changelog version:
    @bash tools/changelog-section.sh "$1"

[doc('Check that the version in gradle.properties is ready to tag, then dry-run the publish to GitHub, CurseForge and Modrinth')]
[group('release')]
release-check:
    bash tools/check-release.sh "$(bash tools/release-version.sh)"
    DRY_RUN=1 GITHUB_TOKEN=dry ./gradlew publishMods
