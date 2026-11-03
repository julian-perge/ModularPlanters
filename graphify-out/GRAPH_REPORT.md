# Graph Report - ModularPlanters  (2026-11-03)

## Corpus Check
- 202 files · ~48,936 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 12 file(s) not represented in the graph (top: (none) 5, .properties 2, .toml 2)

## Summary
- 1741 nodes · 5227 edges · 91 communities (39 shown, 52 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 482 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9c886831`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MPTestRegistration.java
- CoreScreen
- MeBlockEntity
- CoreScreenTests.java
- with
- CoreBlockEntity
- net.minecraft.world.item.ItemStack
- MenuTests
- Capability Registration
- PlanterBlockEntity
- Client Outlines and Screens
- PartBlock
- Docs Mockups and License
- Seed Bin
- Planter Ticker and Grouping
- Structure and No-AE2 Tests
- org.junit.jupiter.api.Test
- with
- net.minecraft.core.BlockPos
- AteBridge.java
- MeLinkTests
- Cloche Yield and Remainders
- net.minecraft.gametest.framework.GameTestHelper
- WikiStructureTest.java
- Ae2Tests.java
- attributes
- UpgradeKind
- Loot and Data Components
- .tickFromCore
- FertilizerBlockEntity
- PowerBlockEntity
- Recipe Datagen
- PartType
- Model Datagen
- MPBlockTags.java
- org.jspecify.annotations.Nullable
- FertilizerPool
- FakeMeLink
- render_logo.py
- OutputSink
- net.minecraft.world.entity.player.Player
- body
- net.minecraft.gametest.framework.GameTestInfo
- PlanterOutlines.java
- PlanterBlockEntity.java
- Planter Power ME Textures
- Entire Agent Guide
- Core Export Textures
- TestStructureProvider.java
- Fertilizer Textures
- description.md
- contact_links
- net.minecraft.nbt.CompoundTag
- Override
- CoreTooltipProvider.java
- net.minecraft.resources.Identifier
- MPJadePlugin.java
- PULL_REQUEST_TEMPLATE.md
- net.minecraft.world.entity.player.Inventory
- ae2.mdx
- example_builds.mdx
- overview.mdx
- core.mdx
- me.mdx
- planter.mdx
- power.mdx
- ExportBlockEntity
- CoreBlock.java
- JadeTests.java
- MeLink
- build.yml
- ReleaseScriptsTest
- TickContext
- JadeRegistration
- Changelog
- release-version.sh
- changelog-section.sh
- check-release.sh

## God Nodes (most connected - your core abstractions)
1. `PlanterBlockEntity` - 69 edges
2. `CoreBlockEntity` - 59 edges
3. `PlanterMenu` - 47 edges
4. `MeBlockEntity` - 37 edges
5. `CoreScreen` - 37 edges
6. `MenuTests` - 36 edges
7. `MPBlocks` - 33 edges
8. `CoreMenu` - 32 edges
9. `ExportBlockEntity` - 31 edges
10. `SeedBin` - 31 edges

## Surprising Connections (you probably didn't know these)
- `Core block` --references--> `CoreBlockEntity`  [INFERRED]
  README.md → src/main/java/io/github/julianperge/modular_planters/blockentity/CoreBlockEntity.java
- `Trellis free form` --conceptually_related_to--> `Multiblock building rules`  [INFERRED]
  mockups/layouts.html → README.md
- `Core screen mockup` --conceptually_related_to--> `Core screen`  [INFERRED]
  mockups/core-screen.html → README.md
- `Planter multiblock layouts mockup` --conceptually_related_to--> `Multiblock building rules`  [INFERRED]
  mockups/layouts.html → README.md
- `ME Side Texture` --semantically_similar_to--> `Power Bottom Texture`  [INFERRED] [semantically similar]
  src/main/resources/assets/modular_planters/textures/block/me_side.png → src/main/resources/assets/modular_planters/textures/block/power_bottom.png

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Agent guide entry points** — agents, claude, _entire_agent_guide [EXTRACTED 0.95]
- **Modular Planters multiblock parts** — readme_core_block, readme_power_block, readme_fertilizer_block, readme_export_block, readme_planter_block, readme_frame_block, readme_me_block [EXTRACTED 0.95]

## Communities (91 total, 52 thin omitted)

### Community 1 - "CoreScreen"
Cohesion: 0.10
Nodes (4): CoreScreen, Entry, PlanterScreen, Row

### Community 2 - "MeBlockEntity"
Cohesion: 0.06
Nodes (6): FluxBridge, MeBlockEntity, FilterSlot, MeMenu, DataSlot, AppliedFluxTests

### Community 3 - "CoreScreenTests.java"
Cohesion: 0.09
Nodes (7): Action, BACK_TO_CORE, OPEN, CoreRequestPayload, CoreTablePayload, Totals, SeedBinCombosPayload

### Community 4 - "with"
Cohesion: 0.05
Nodes (47): needs, uses, if, needs, runs-on, steps, defaults, run (+39 more)

### Community 5 - "CoreBlockEntity"
Cohesion: 0.12
Nodes (4): CoreBlockEntity, DoorwayHandlers, InsertView, PowerPool

### Community 6 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.06
Nodes (8): PlantRecipe, RecipeMemo, AteBridgeTests, Snapshot, PlanterMenu, DataSlot, UpgradeSlot, SeedBinSlot

### Community 8 - "Capability Registration"
Cohesion: 0.13
Nodes (5): Ae2Integration, ModularPlanters, MPCreativeTab, MPItems, MPMenus

### Community 9 - "PlanterBlockEntity"
Cohesion: 0.19
Nodes (7): PlanterBlockEntity, PlanterStatus, GROWING, IDLE, PAUSED_EXPORT_FULL, PAUSED_FOR_POWER, SHORT_ON_SOIL

### Community 10 - "Client Outlines and Screens"
Cohesion: 0.14
Nodes (3): Ae2Client, ModularPlantersClient, MPDataGen

### Community 12 - "Docs Mockups and License"
Cohesion: 0.06
Nodes (43): Contributing to Modular Planters, Issues, Pull requests, Commands, Dev mods, Developing Modular Planters, Releasing, Screenshots with Structure Lab (+35 more)

### Community 13 - "Seed Bin"
Cohesion: 0.14
Nodes (4): Combo, Entry, SeedBin, SeedBinTest

### Community 14 - "Planter Ticker and Grouping"
Cohesion: 0.21
Nodes (4): Grouping, AdvanceResult, PlanterTickerTest, ReferenceAdvancedPlanter

### Community 15 - "Structure and No-AE2 Tests"
Cohesion: 0.12
Nodes (3): Ae2AbsentTests, RecipeTests, TestCase

### Community 16 - "org.junit.jupiter.api.Test"
Cohesion: 0.16
Nodes (3): LogoTest, TexturesExistTest, ProjectDocsTest

### Community 17 - "with"
Cohesion: 0.07
Nodes (35): runs-on, steps, defaults, run, name, runs-on, steps, strategy (+27 more)

### Community 18 - "net.minecraft.core.BlockPos"
Cohesion: 0.13
Nodes (3): MeBlock, SoilSlot, Multiblock

### Community 21 - "Cloche Yield and Remainders"
Cohesion: 0.16
Nodes (3): ClocheYield, RemainderLedger, ClocheYieldTest

### Community 23 - "net.minecraft.gametest.framework.GameTestHelper"
Cohesion: 0.05
Nodes (10): Ae2Tests, CoreScreenTests, Setup, DoorwayTests, Fixtures, Rig, PlanterLifecycleTests, PlanterTests (+2 more)

### Community 26 - "attributes"
Cohesion: 0.13
Nodes (14): description, label, options, placeholder, value, body, attributes, id (+6 more)

### Community 27 - "UpgradeKind"
Cohesion: 0.09
Nodes (13): AteNumbers, GrowthMath, UpgradeKind, NONE, SPEED_MK1, SPEED_MK2, SPEED_MK3, YIELD_MK1 (+5 more)

### Community 29 - ".tickFromCore"
Cohesion: 0.12
Nodes (3): Coverage, Group, CoverageTest

### Community 31 - "PowerBlockEntity"
Cohesion: 0.14
Nodes (6): PowerBlockEntity, MeStatus, NO_CHANNEL, NONE, OFFLINE, ONLINE

### Community 33 - "PartType"
Cohesion: 0.07
Nodes (15): JadeTests, StructureTests, PartType, CORE, EXPORT, FERTILIZER, FRAME, ME (+7 more)

### Community 36 - "org.jspecify.annotations.Nullable"
Cohesion: 0.15
Nodes (4): ExportBlock, FertilizerBlock, PowerBlock, MPBlockEntities

### Community 39 - "render_logo.py"
Cohesion: 0.23
Nodes (7): affine_to_texture(), draw_cube(), draw_face(), main(), render(), texture(), visible_core_share()

### Community 42 - "body"
Cohesion: 0.15
Nodes (12): description, label, value, body, attributes, id, type, validations (+4 more)

### Community 46 - "Planter Power ME Textures"
Cohesion: 0.48
Nodes (7): ME Side Texture, Planter Side On Texture, Planter Side Texture, Planter Top Texture, Power Bottom Texture, Power Side Texture, Power Top Texture

### Community 47 - "Entire Agent Guide"
Cohesion: 0.33
Nodes (3): Entire agent guide, Entire Brain tool, Entire Graph tool

### Community 48 - "Core Export Textures"
Cohesion: 0.40
Nodes (5): Core Side Texture, Core Top Texture, Export Side Texture, Export Top Texture, Frame Side Texture

### Community 54 - "description.md"
Cohesion: 0.25
Nodes (7): AE2 and Applied Flux, Build it your way, Credits, Features, Guide and Jade, One connection runs it all, See every planter at once

### Community 55 - "contact_links"
Cohesion: 0.33
Nodes (5): blank_issues_enabled, contact_links, about, name, url

### Community 56 - "net.minecraft.nbt.CompoundTag"
Cohesion: 0.23
Nodes (4): CoreProvider, INSTANCE, PlanterProvider, INSTANCE

### Community 58 - "CoreTooltipProvider.java"
Cohesion: 0.23
Nodes (4): CoreTooltipProvider, INSTANCE, PlanterTooltipProvider, INSTANCE

### Community 63 - "net.minecraft.world.entity.player.Inventory"
Cohesion: 0.13
Nodes (4): MeScreen, StorageScreen, StorageMenu, TakeOnlySlot

### Community 64 - "ae2.mdx"
Cohesion: 0.50
Nodes (3): Applied Flux, Fertilizer filter, ME block

### Community 65 - "example_builds.mdx"
Cohesion: 0.50
Nodes (3): Raised bed under a cloche, Rosette, Vertical farm

### Community 71 - "me.mdx"
Cohesion: 0.50
Nodes (3): Applied Flux, Fertilizer filter, ME block

### Community 81 - "JadeTests.java"
Cohesion: 0.09
Nodes (12): Problem, MISSING_EXPORT, MISSING_PLANTER, MISSING_POWER, NONE, SECOND_CORE, SECOND_ME, TOO_MANY_PARTS (+4 more)

### Community 83 - "build.yml"
Cohesion: 0.14
Nodes (13): uses, concurrency, cancel-in-progress, group, jobs, checks, name, on (+5 more)

## Knowledge Gaps
- **44 isolated node(s):** `IDLE`, `GROWING`, `SHORT_ON_SOIL`, `PAUSED_FOR_POWER`, `PAUSED_EXPORT_FULL` (+39 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 356 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **52 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CoreBlockEntity` connect `CoreBlockEntity` to `PartType`, `CoreScreenTests.java`, `org.jspecify.annotations.Nullable`, `FertilizerPool`, `OutputSink`, `PlanterBlockEntity`, `net.minecraft.world.entity.player.Player`, `Docs Mockups and License`, `JadeTests.java`, `net.minecraft.core.BlockPos`, `MeLink`, `net.minecraft.gametest.framework.GameTestHelper`, `net.minecraft.nbt.CompoundTag`, `Ae2Tests.java`, `MPJadePlugin.java`, `PowerBlockEntity`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **What connects `IDLE`, `GROWING`, `SHORT_ON_SOIL` to the rest of the system?**
  _44 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CoreScreen` be split into smaller, more focused modules?**
  _Cohesion score 0.09693877551020408 - nodes in this community are weakly interconnected._
- **Why does `PlanterBlockEntity` connect `PlanterBlockEntity` to `CoreScreenTests.java`, `net.minecraft.world.item.ItemStack`, `MenuTests`, `Seed Bin`, `Planter Ticker and Grouping`, `net.minecraft.core.BlockPos`, `AteBridge.java`, `Cloche Yield and Remainders`, `map`, `net.minecraft.gametest.framework.GameTestHelper`, `Ae2Tests.java`, `UpgradeKind`, `.tickFromCore`, `FertilizerBlockEntity`, `PowerBlockEntity`, `org.jspecify.annotations.Nullable`, `OutputSink`, `net.minecraft.world.entity.player.Player`, `PlanterBlockEntity.java`, `net.minecraft.nbt.CompoundTag`, `MPJadePlugin.java`, `JadeTests.java`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **Should `MeBlockEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.061367621274108705 - nodes in this community are weakly interconnected._
- **Why does `Core block` connect `Docs Mockups and License` to `CoreBlockEntity`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **Should `CoreScreenTests.java` be split into smaller, more focused modules?**
  _Cohesion score 0.08562367864693446 - nodes in this community are weakly interconnected._