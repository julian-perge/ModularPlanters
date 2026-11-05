# Developing Modular Planters

## Setup

- JDK 25 on your `PATH`. Gradle 9.8.2 starts with JDK 17 or newer through the wrapper (`./gradlew`), and the build compiles with JDK 25, which Gradle can also download through the Foojay toolchain resolver. The Java formatter hook runs the `java` on your `PATH` and reads the code with that JDK's compiler, so it needs JDK 25.
- [just](https://just.systems) for the commands below and [prek](https://prek.j178.dev) for the pre-commit hooks: `brew install just prek`. Run `prek install` once in your clone. After that, each commit formats Java with google-java-format and Markdown, YAML and JSON with Prettier, then checks the workflows and shell scripts.
- Optional, for redrawing the logo with `just logo`: [uv](https://docs.astral.sh/uv/) (`brew install uv`). For compressing screenshots with `just compress`: `brew install pngquant oxipng`.
- Optional, for testing the GitHub workflows before pushing: Docker, `brew install zizmor`, and `bun add -g @mizchi/actrun run-local-ci`. `just ci` runs the Build workflow in Docker and `just ci-lint` lints the workflows.
- An IDE is optional. IntelliJ IDEA imports the project from `build.gradle`, and the `idea` plugin downloads sources and javadoc for Minecraft and NeoForge. `.editorconfig` sets Java to 2-space indents and 100 columns, matching google-java-format.

The first build downloads Minecraft, NeoForge and the dev mods, so it takes a few minutes.

## Commands

Run `just` to list the recipes. Each one wraps a Gradle task or a script in `tools/`, so `./gradlew runClient` and the rest still work without just.

| Command                        | What it does                                                                                              |
| ------------------------------ | --------------------------------------------------------------------------------------------------------- |
| `just build`                   | Compiles, runs the unit tests and builds the jar into `build/libs/`                                       |
| `just test`                    | Runs the unit tests in `src/test`                                                                         |
| `just gametest`                | Starts a headless server that runs the in-game tests in `src/main/java/.../gametest` and exits            |
| `just check`                   | Runs the unit tests, then the in-game tests with every dev mod, with `-PnoAe2` and with `-PnoAppliedFlux` |
| `just client`                  | Starts a dev client with the mod and the dev mods below                                                   |
| `just server`                  | Starts a dev dedicated server with no GUI                                                                 |
| `just data`                    | Regenerates `src/generated/resources` (recipes, loot tables, lang, models)                                |
| `just gradle-update <version>` | Upgrades the Gradle wrapper to that version                                                               |
| `just lint`                    | Runs every pre-commit hook on every file                                                                  |
| `just fmt`                     | Formats the Java, Markdown, YAML and JSON files in place                                                  |
| `just ci-lint`                 | Lints the GitHub workflows with actrun and zizmor                                                         |
| `just ci`                      | Runs the Build workflow locally in Docker with local-ci, uncommitted changes included                     |
| `just logo`                    | Redraws the logo from the block textures                                                                  |
| `just compress <files>`        | Compresses PNG screenshots with pngquant and oxipng                                                       |
| `just changelog <tag>`         | Prints the changelog notes for one release                                                                |
| `just release-check`           | Checks that the version is ready to tag, then dry-runs the publish                                        |

Run `just check` and `just lint` before opening a pull request. After changing anything in `datagen`, also run `just data` and commit the result. The Build workflow runs all of these on every push and pull request, and fails if `runData` would change `src/generated`.

## Where things live

- `src/main/java`: the mod. The `gametest` and `datagen` packages are left out of the jar.
- `src/generated/resources`: output of `runData`. It is committed, so run `runData` and commit the result after changing anything in `datagen`.
- `wiki/`: the guide's pages. `processResources` copies them into the jar, where Oracle Index reads them as a book.
- `docs/description.md` and `docs/media/`: the CurseForge and Modrinth page and its images. World shots are JPEG. Screenshots of screens are PNG, compressed with `just compress <files>`, which runs `pngquant` and then `oxipng` (`brew install pngquant oxipng`). The logo PNGs are left as `tools/render_logo.py` writes them.
- `tools/changelog-section.sh`, `tools/check-release.sh` and `tools/release-version.sh`: the release checks that `just release-check` and the Release workflow run.
- `.github/workflows/`: `build.yml` runs `checks.yml` on every push and pull request, and `release.yml` runs it on a release tag before publishing.
- `tools/render_logo.py`: draws the logo from the block textures. Run `just logo` after changing a block texture, and commit both `docs/media/logo.png` and `src/main/resources/logo.png`.

## Dev mods

These are on the dev runtime (`runClient`, `runServer`, `runGameTestServer`) but not in the jar. Agritech: Evolved is the only required dependency. Each switch below leaves a mod out of a run, for example `just client -PnoAe2`.

| Mod                | Version       | Comes with                     | Switch                                     |
| ------------------ | ------------- | ------------------------------ | ------------------------------------------ |
| Agritech: Evolved  | 2.2.0.6       |                                | required                                   |
| Jade               | 26.1.12       |                                | `-PnoJade`                                 |
| AE2                | 26.1.13-beta  | GuideME 26.1.15-beta           | `-PnoAe2`                                  |
| Applied Flux       | 26.1-1.0.2    | Glodium 26.1-1.2               | `-PnoAppliedFlux`, also off with `-PnoAe2` |
| Powah              | 7.0.4-alpha   | Cloth Config 26.1.154, GuideME | `-PnoPowah`                                |
| Oracle Index       | 2.0.0         | Architectury 20.1.17           | `-PnoOracleIndex`                          |
| Logistics Networks | 26.1.2-1.13.0 |                                | `-PnoLogisticsNetworks`                    |

The mod has code only for Jade, AE2 and Applied Flux. Powah is there for its fast FE source and cables. Oracle Index shows the guide, and Logistics Networks lets you test a node on the core. Most versions match the ones the All the Mods 11 modpack runs. Applied Flux isn't in that pack, and its AE2 build can differ.

## Releasing

A release tag is `<minecraft_version>-<mod_version>`, for example `26.1.2-0.1.0` or `26.3-0.2.0`. Pushing one runs the Release workflow. It checks the tag and runs the same checks as every push. Then it makes a GitHub release with the jar those checks built, and after that uploads the same jar to CurseForge and Modrinth.

1. Set `mod_version` in `gradle.properties` to three numbers, such as `0.2.0`. A `-alpha` or `-beta` suffix, such as `0.2.0-beta`, publishes an alpha or a beta. Without one it is a full release.
2. Add a `## <tag>` section to the top of `CHANGELOG.md`, below the intro line. Its notes become the release notes on all three sites.
3. Run `just release-check`. It builds the tag from `gradle.properties`, fails if the tag has the wrong format or the changelog has no notes for it, then dry-runs the publish.
4. Commit, then run `git tag <tag> && git push origin main <tag>`.

The workflow reads the repository secrets `CURSEFORGE_API_KEY` and `MODRINTH_API_KEY`, and the project ids `curseforge_project_id` and `modrinth_project_id` in `gradle.properties`. An empty id skips that site, and the run logs a notice saying so. The GitHub release uses the workflow's own token.

If an upload fails, for example because a site is down, use "Re-run failed jobs" on that workflow run. A fix that needs a code change is released as a new version with a new tag. A tag that was pushed is never moved.

## Screenshots with Structure Lab

`-PstructureLabJar=<path to jar>` adds Structure Lab to a dev run. It places a structure file in a flat world and saves screenshots from set camera angles, which is how the world shots in `docs/media/` were taken. It is a local build that isn't published, so leave the switch out unless you have the jar. It needs a world created with the "Structure Lab" world type.
