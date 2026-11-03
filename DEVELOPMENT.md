# Developing Modular Planters

## Setup

- A JDK 17 or newer on your `PATH` or in `JAVA_HOME`, to start Gradle 9.2.1 through the wrapper (`./gradlew`).
- The build compiles and runs with JDK 25. Gradle finds an installed JDK 25 or downloads one through the Foojay toolchain resolver, so installing JDK 25 yourself covers both needs.
- An IDE is optional. IntelliJ IDEA imports the project from `build.gradle`, and the `idea` plugin downloads sources and javadoc for Minecraft and NeoForge.

The first build downloads Minecraft, NeoForge and the dev mods, so it takes a few minutes.

## Gradle tasks

| Command                       | What it does                                                                                   |
| ----------------------------- | ---------------------------------------------------------------------------------------------- |
| `./gradlew build`             | Compiles, runs the unit tests and builds the jar into `build/libs/`                            |
| `./gradlew test`              | Runs the unit tests in `src/test`                                                              |
| `./gradlew runGameTestServer` | Starts a headless server that runs the in-game tests in `src/main/java/.../gametest` and exits |
| `./gradlew runClient`         | Starts a dev client with the mod and the dev mods below                                        |
| `./gradlew runServer`         | Starts a dev dedicated server with no GUI                                                      |
| `./gradlew runData`           | Regenerates `src/generated/resources` (recipes, loot tables, lang, models)                     |

Run `./gradlew test` and `./gradlew runGameTestServer` before opening a pull request.

## Where things live

- `src/main/java`: the mod. The `gametest` and `datagen` packages are left out of the jar.
- `src/generated/resources`: output of `runData`. It is committed, so run `runData` and commit the result after changing anything in `datagen`.
- `wiki/`: the guide's pages. `processResources` copies them into the jar, where Oracle Index reads them as a book.
- `docs/description.md` and `docs/media/`: the CurseForge and Modrinth page and its images. World shots are JPEG. Screenshots of screens are PNG, compressed with `pngquant --quality=80-95 --speed 1 --strip --force --ext .png <files>` and then `oxipng -o 4 --strip safe <files>` (`brew install pngquant oxipng`). The logo PNGs are left as `tools/render_logo.py` writes them.
- `tools/render_logo.py`: draws the logo from the block textures. Run `uv run tools/render_logo.py` after changing a block texture, and commit both `docs/media/logo.png` and `src/main/resources/logo.png`.

## Dev mods

These are on the dev runtime (`runClient`, `runServer`, `runGameTestServer`) but not in the jar. Agritech: Evolved is the only required dependency. Each switch below leaves a mod out of a run, for example `./gradlew runClient -PnoAe2`.

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

## Screenshots with Structure Lab

`-PstructureLabJar=<path to jar>` adds Structure Lab to a dev run. It places a structure file in a flat world and saves screenshots from set camera angles, which is how the world shots in `docs/media/` were taken. It is a local build that isn't published, so leave the switch out unless you have the jar. It needs a world created with the "Structure Lab" world type.
