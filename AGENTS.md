<!-- entire-agent:begin -->
Read .entire/agent-guide.md for this repository's workflow, source inspection, and verification guidance.
@.entire/agent-guide.md
<!-- entire-agent:end -->

# Modular Planters

NeoForge 26.1.2 addon for Agritech: Evolved (ATE) that merges many planters into one multiblock. DEVELOPMENT.md has the commands, layout, dev mods and release steps. CONTRIBUTING.md has the pull request rules.

The repo is public, and each push also sends Entire session logs to `origin`, so keep private paths and personal details out of commits.

## Every task

- Run `just check` and `just lint` to verify a change.
- The mod must load with any optional mod missing.
- Keep save keys and registry ids as they are, because renaming breaks existing worlds. The save key `soil_and_modules` keeps its old name, and ATE's own names stay as ATE has them.
- Keep merged history as it is, because `.git-blame-ignore-revs` names the reformat commit e1a9a58, and a squash or rebase breaks that.
- Player-facing text uses the block names from `en_us.json`, "upgrades" instead of "modules", and "Advanced Planter" instead of "Powered Planter". Those names are Planter Core, Planter Block, Power Block, Fertilizer Block, Export Block, ME Block and Planter Frame. They started with "Modular Planter" before 2026-11-07, and released changelog entries and ADRs keep the old names.

## Read the doc that matches the task

- [GLOSSARY.md](GLOSSARY.md): before naming anything in code, docs or questions. [docs/adr/](docs/adr/) holds the decisions that are hard to reverse.
- [Verifying changes](docs/agents/verifying.md): before running datagen, or when a change touches Jade or the Oracle Index book.
- [Optional mods and the ME link](docs/agents/optional-mods.md): before touching `ae2` code, `mod_loaded` conditions or `MeLink`.
- [Minecraft, NeoForge and addon lookups](docs/agents/minecraft-lookups.md): before using a Minecraft, NeoForge, FML or AE2 class, method, registry id or version.
- [GameTests](docs/agents/gametests.md): before writing or debugging a GameTest.
- [Docs, wiki and store pages](docs/agents/docs-and-wiki.md): before editing `wiki/` or `docs/description.md`.
- [Build and tooling](docs/agents/build-and-tooling.md): before changing CI, hooks or the Gradle setup, or before using `entire graph` for a refactor.

## Workflow

- Tasks are tracked in GitHub Issues for julian-perge/ModularPlanters, with the `gh` CLI. `/agent-skills:plan` creates one sub-issue per task. `tasks/` is git-ignored scratch.
- Specs are files at `spec/<issue>-<slug>.md`, not `SPEC.md`.
- ADRs go in `docs/adr/`. Read `GLOSSARY.md` and `docs/adr/` (those that exist) before design work.
- Start each `/agent-skills` stage with `agent-skills brief <stage> <n>`. It prints every rule of `docs/agents/workflow.md` that the stage needs, so do not read that file in a stage. Before other work in this repo, read all of `docs/agents/workflow.md`.
