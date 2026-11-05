<!-- entire-agent:begin -->
Read .entire/agent-guide.md for this repository's workflow, source inspection, and verification guidance.
@.entire/agent-guide.md
<!-- entire-agent:end -->

## Project

NeoForge 26.1.2 addon for Agritech: Evolved (ATE) that merges many planters into one multiblock. DEVELOPMENT.md has the commands, layout, dev mods and release steps. CONTRIBUTING.md has the pull request rules.

The repo is public, and each push also sends Entire session logs to `origin`, so keep private paths and personal details out of commits.

## Verifying

- Run `just check` and `just lint`. After changing `datagen`, run `just data` and commit the result.
- After changing the Jade integration, also run `just gametest -PnoJade`.
- GameTests cannot prove that Jade tooltips or the Oracle Index book work. Jade swallows plugin exceptions and checks its provider rules only on a physical client, so ask the owner for an in-game check.

## Rules

- The mod must load with any optional mod missing. AE2 classes stay in the `ae2` package behind `ModList.get().isLoaded("ae2")`, and generated data that references AE2 or `modular_planters:me` carries a `mod_loaded` condition.
- `MeLink` implementations return at most the requested amount, and a simulated call changes nothing. Callers subtract the result without clamping, so over-reporting voids items.
- Don't rename save keys or registry ids, because that breaks existing worlds. The save key `soil_and_modules` keeps its old name, and ATE's own names stay as ATE has them.
- Never squash or rebase merged history, because `.git-blame-ignore-revs` names the reformat commit e1a9a58.
- Keep `cache-provider: basic` on setup-gradle. The default provider accepts proprietary Terms of Use, and only the owner can agree to those.
- Hooks in `.claude/settings.json` call tools by name behind a `command -v` check, never by absolute path.
- Player-facing text uses the block names from `en_us.json`, "upgrades" instead of "modules", and "Advanced Planter" instead of "Powered Planter".

## Gotchas

- `assert` does nothing in GameTests because the server JVM runs without `-ea`. Use `helper.fail(...)`.
- `makeMockPlayer(GameType.CREATIVE)` leaves `hasInfiniteMaterials()` false, so set `abilities.instabuild = true` as well.
- `GameTestHelper.assertValueEqual(actual, expected, msg)` prints its values swapped: "Expected X to be A: was B" means the actual value was A.
- `wiki/` pages are MDX, so a placeholder like `<problem>` parses as a JSX tag and breaks the page.
- ProjectDocsTest checks links against `git ls-files`, so a linked file must be tracked and match its exact case.
- `docs/description.md` loads its images from `main`, so push before pasting it into CurseForge or Modrinth.
- `entire graph` misses calls through record accessors such as `ctx.outputs().insert(...)`, so check `TickContext` callers in source before refactoring.
- `./gradlew clean` wipes `build/moddev`, and rebuilding it needs the network.
