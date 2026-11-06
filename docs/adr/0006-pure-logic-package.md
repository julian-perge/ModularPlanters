# Growth and structure math lives in a package with no Minecraft imports

All growth, coverage, cloche and structure math is in `logic/`, which imports nothing from `net.minecraft`, `net.neoforged`, `com.mojang` or ATE. Plain JUnit tests cover it and run with `./gradlew test`, while block entities only move items and energy, fire ATE's events and call into `logic/`. Anything that needs a world is covered by GameTests instead.

ATE's numbers reach `logic/` as a plain value object that tests can fill with ATE's defaults.
