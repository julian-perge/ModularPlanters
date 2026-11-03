# Local shortcuts for building, testing, formatting and releasing Modular Planters. `just` lists them, and `just <recipe>` runs one.
#
# CI does not call just. The workflows in .github/workflows run the same Gradle tasks and tools/ scripts directly.

set shell := ["bash", "-euo", "pipefail", "-c"]

# Recipe arguments arrive as $1, $2 and so on, so variadic recipes forward them with "$@" and keep each argument intact.
set positional-arguments

[private]
default:
    @just --list --unsorted

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

[doc('Run the unit tests, then the in-game tests with all dev mods, without AE2 and without Applied Flux, the three ways CI runs them')]
[group('gradle')]
check:
    ./gradlew test
    ./gradlew runGameTestServer
    ./gradlew runGameTestServer -PnoAe2
    ./gradlew runGameTestServer -PnoAppliedFlux

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

[doc('Run every pre-commit hook on every file, as CI does')]
[group('lint')]
lint:
    prek run --all-files --show-diff-on-failure

# The formatters exit 1 when they rewrite a file, so a second run decides the result: it passes when the first run fixed everything.
[doc('Format the Java, Markdown, YAML and JSON files in place')]
[group('lint')]
fmt:
    prek run --all-files pretty-format-java prettier || prek run --all-files pretty-format-java prettier

[doc('Render the mod logo into docs/media/logo.png and src/main/resources/logo.png')]
[group('media')]
logo:
    uv run tools/render_logo.py

[doc('Compress PNG files in place, for example: just compress docs/media/*.png')]
[group('media')]
compress +files:
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
