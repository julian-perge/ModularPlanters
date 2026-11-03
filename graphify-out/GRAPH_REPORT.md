# Graph Report - ModularPlanters  (2026-11-03)

## Corpus Check
- 194 files · ~46,009 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 2, .properties 2, .jar 1)

## Summary
- 1619 nodes · 5061 edges · 86 communities (38 shown, 48 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 482 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3efb1a62`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CoreScreenTests
- CoreScreen
- MeBlockEntity
- CoreScreenTests.java
- StructureScannerTest
- PowerBlockEntity
- net.minecraft.world.item.ItemStack
- MeMenu
- Capability Registration
- PlanterTests.java
- Client Outlines and Screens
- CoreBlockEntity.java
- Docs Mockups and License
- Seed Bin
- Planter Ticker and Grouping
- Structure and No-AE2 Tests
- org.junit.jupiter.api.Test
- net.minecraft.network.chat.Component
- net.neoforged.neoforge.transfer.item.ItemResource
- Cloche Yield and Remainders
- GameTest Registration
- net.minecraft.gametest.framework.GameTestHelper
- Ae2Tests.java
- attributes
- UpgradeKind
- Loot and Data Components
- .tickFromCore
- ExportBlockEntity
- PlanterMenu.java
- Recipe Datagen
- net.minecraft.resources.Identifier
- Model Datagen
- MPBlockTags.java
- ExportBlock.java
- MenuTests
- CoreScreen.java
- render_logo.py
- net.minecraft.world.entity.player.Player
- body
- GameTest Listener
- AppliedFluxTests.java
- Planter Power ME Textures
- Entire Agent Guide
- Core Export Textures
- TestStructureProvider.java
- Fertilizer Textures
- description.md
- contact_links
- WikiStructureTest
- CoreBlockEntity
- ConditionalDataTest
- ModularPlanters.java
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
- StructureScannerTest.java
- PlanterBlock.java
- Problem
- MPConfig
- Action
- TexturesExistTest

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

## Communities (86 total, 48 thin omitted)

### Community 1 - "CoreScreen"
Cohesion: 0.15
Nodes (3): CoreScreen, Entry, Row

### Community 3 - "CoreScreenTests.java"
Cohesion: 0.15
Nodes (4): CoreRequestPayload, CoreTablePayload, Totals, SeedBinCombosPayload

### Community 5 - "PowerBlockEntity"
Cohesion: 0.10
Nodes (5): ExternalHandler, PowerBlockEntity, InsertView, PowerPool, MPBlockEntities

### Community 6 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.06
Nodes (9): AteBridge, PlantRecipe, RecipeMemo, AteBridgeTests, PlanterMenu, DataSlot, SoilSlot, UpgradeSlot (+1 more)

### Community 7 - "MeMenu"
Cohesion: 0.16
Nodes (3): FilterSlot, MeMenu, DataSlot

### Community 8 - "Capability Registration"
Cohesion: 0.14
Nodes (4): Ae2Integration, MPCreativeTab, MPItems, MPMenus

### Community 9 - "PlanterTests.java"
Cohesion: 0.10
Nodes (7): PlanterStatus, GROWING, IDLE, PAUSED_EXPORT_FULL, PAUSED_FOR_POWER, SHORT_ON_SOIL, Coverage

### Community 10 - "Client Outlines and Screens"
Cohesion: 0.08
Nodes (4): Ae2Client, ModularPlantersClient, PlanterOutlines, MPDataGen

### Community 11 - "CoreBlockEntity.java"
Cohesion: 0.13
Nodes (6): CoreBlock, PartBlock, PartType, NOT_A_PART, StructureScanner, CoreFinder

### Community 12 - "Docs Mockups and License"
Cohesion: 0.06
Nodes (42): Contributing to Modular Planters, Issues, Pull requests, Dev mods, Developing Modular Planters, Gradle tasks, Screenshots with Structure Lab, Setup (+34 more)

### Community 14 - "Planter Ticker and Grouping"
Cohesion: 0.20
Nodes (5): Grouping, AdvanceResult, PlanterTicker, PlanterTickerTest, ReferenceAdvancedPlanter

### Community 15 - "Structure and No-AE2 Tests"
Cohesion: 0.12
Nodes (3): Ae2AbsentTests, RecipeTests, TestCase

### Community 17 - "net.minecraft.network.chat.Component"
Cohesion: 0.17
Nodes (3): MeScreen, PlanterScreen, StorageScreen

### Community 20 - "net.neoforged.neoforge.transfer.item.ItemResource"
Cohesion: 0.05
Nodes (9): Ae2Tests, FakeMeLink, MeLinkTests, DoorwayHandlers, Budget, FertilizerPool, LiveBlocks, MeLink (+1 more)

### Community 21 - "Cloche Yield and Remainders"
Cohesion: 0.16
Nodes (4): ClocheYield, RemainderLedger, LogoTest, ClocheYieldTest

### Community 23 - "net.minecraft.gametest.framework.GameTestHelper"
Cohesion: 0.05
Nodes (11): PlanterBlockEntity, DoorwayTests, Fixtures, Rig, JadeTests, PlanterLifecycleTests, Snapshot, PlanterTests (+3 more)

### Community 26 - "attributes"
Cohesion: 0.13
Nodes (14): description, label, options, placeholder, value, body, attributes, id (+6 more)

### Community 27 - "UpgradeKind"
Cohesion: 0.08
Nodes (13): AteNumbers, GrowthMath, UpgradeKind, NONE, SPEED_MK1, SPEED_MK2, SPEED_MK3, YIELD_MK1 (+5 more)

### Community 29 - ".tickFromCore"
Cohesion: 0.10
Nodes (6): Group, Combo, Entry, SeedBin, TickContext, CoverageTest

### Community 30 - "ExportBlockEntity"
Cohesion: 0.16
Nodes (3): ExportBlockEntity, FertilizerBlockEntity, ItemStacksResourceHandler

### Community 31 - "PlanterMenu.java"
Cohesion: 0.14
Nodes (3): ScanResult, Multiblock, MPNetwork

### Community 33 - "net.minecraft.resources.Identifier"
Cohesion: 0.12
Nodes (9): JadeRegistration, CoreProvider, INSTANCE, CoreTooltipProvider, INSTANCE, PlanterProvider, INSTANCE, PlanterTooltipProvider (+1 more)

### Community 36 - "ExportBlock.java"
Cohesion: 0.15
Nodes (4): MeBlock, ExportBlock, FertilizerBlock, PowerBlock

### Community 38 - "CoreScreen.java"
Cohesion: 0.13
Nodes (5): MeStatus, NO_CHANNEL, NONE, OFFLINE, ONLINE

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

### Community 59 - "ModularPlanters.java"
Cohesion: 0.13
Nodes (3): MPTestRegistration, ModularPlanters, MPDataComponents

### Community 64 - "ae2.mdx"
Cohesion: 0.50
Nodes (3): Applied Flux, Fertilizer filter, ME block

### Community 65 - "example_builds.mdx"
Cohesion: 0.50
Nodes (3): Raised bed under a cloche, Rosette, Vertical farm

### Community 71 - "me.mdx"
Cohesion: 0.50
Nodes (3): Applied Flux, Fertilizer filter, ME block

### Community 79 - "StructureScannerTest.java"
Cohesion: 0.11
Nodes (8): CORE, EXPORT, FERTILIZER, FRAME, ME, PLANTER, POWER, UNLOADED

### Community 81 - "Problem"
Cohesion: 0.22
Nodes (9): Problem, MISSING_EXPORT, MISSING_PLANTER, MISSING_POWER, NONE, SECOND_CORE, SECOND_ME, TOO_MANY_PARTS (+1 more)

### Community 84 - "Action"
Cohesion: 0.50
Nodes (3): Action, BACK_TO_CORE, OPEN

## Knowledge Gaps
- **44 isolated node(s):** `IDLE`, `GROWING`, `SHORT_ON_SOIL`, `PAUSED_FOR_POWER`, `PAUSED_EXPORT_FULL` (+39 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 302 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **48 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CoreBlockEntity` connect `CoreBlockEntity` to `CoreScreenTests`, `net.minecraft.resources.Identifier`, `CoreScreenTests.java`, `PowerBlockEntity`, `DoorwayTests.java`, `PlanterTests.java`, `net.minecraft.world.entity.player.Player`, `CoreBlockEntity.java`, `Docs Mockups and License`, `AppliedFluxTests.java`, `net.minecraft.core.BlockPos`, `MPConfig`, `net.neoforged.neoforge.transfer.item.ItemResource`, `net.minecraft.gametest.framework.GameTestHelper`, `Ae2Tests.java`, `MPJadePlugin.java`, `PlanterMenu.java`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **What connects `IDLE`, `GROWING`, `SHORT_ON_SOIL` to the rest of the system?**
  _44 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MeBlockEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.13105413105413105 - nodes in this community are weakly interconnected._
- **Why does `Core block` connect `Docs Mockups and License` to `CoreBlockEntity`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **Should `PowerBlockEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.10121951219512196 - nodes in this community are weakly interconnected._
- **Why does `PlanterBlockEntity` connect `net.minecraft.gametest.framework.GameTestHelper` to `CoreScreenTests.java`, `PowerBlockEntity`, `net.minecraft.world.item.ItemStack`, `PlanterTests.java`, `Seed Bin`, `net.minecraft.core.BlockPos`, `net.neoforged.neoforge.transfer.item.ItemResource`, `Cloche Yield and Remainders`, `Ae2Tests.java`, `UpgradeKind`, `.tickFromCore`, `ExportBlockEntity`, `PlanterMenu.java`, `net.minecraft.resources.Identifier`, `MenuTests`, `net.minecraft.world.entity.player.Player`, `AppliedFluxTests.java`, `ModularPlanters.java`, `MPJadePlugin.java`, `PlanterBlock.java`, `MPConfig`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **Should `net.minecraft.world.item.ItemStack` be split into smaller, more focused modules?**
  _Cohesion score 0.06107594936708861 - nodes in this community are weakly interconnected._