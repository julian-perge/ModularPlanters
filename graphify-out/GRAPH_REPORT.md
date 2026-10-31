# Graph Report - ModularPlanters  (2026-10-31)

## Corpus Check
- 182 files · ~43,936 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 8 file(s) not represented in the graph (top: (none) 2, .properties 2, .jar 1)

## Summary
- 1515 nodes · 4898 edges · 82 communities (28 shown, 54 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 482 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5ba16500`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- PlanterBlockEntity
- CoreScreen
- MeBlockEntity
- CoreMenu
- StructureScannerTest
- PowerBlockEntity
- net.minecraft.world.item.ItemStack
- OutputSink
- Capability Registration
- Client Outlines and Screens
- CoreBlockEntity.java
- Docs Mockups and License
- Seed Bin
- Planter Ticker and Grouping
- Structure and No-AE2 Tests
- CoreMenu.java
- PlanterMenu.java
- PlanterBlock.java
- Fertilizer Pool and Doorway
- Cloche Yield and Remainders
- GameTest Registration
- net.minecraft.gametest.framework.GameTestHelper
- net.minecraft.core.BlockPos
- UpgradeKind
- Loot and Data Components
- .tickFromCore
- FertilizerBlockEntity
- .plant
- Recipe Datagen
- net.minecraft.resources.Identifier
- Model Datagen
- MPBlockTags.java
- MPBlocks.java
- MenuTests
- CoreScreenTests.java
- ModularPlanters.java
- CoreTooltipProvider.java
- MeMenu
- GameTest Listener
- Ae2Tests
- Planter Power ME Textures
- Entire Agent Guide
- Core Export Textures
- TestStructureProvider.java
- Fertilizer Textures
- PartType
- PlanterMenu.java
- WikiStructureTest
- CoreBlockEntity
- ConditionalDataTest
- FluxBridge.java
- MPJadePlugin.java
- net.neoforged.neoforge.transfer.item.ResourceHandlerSlot
- AppliedFluxTests
- .lastScan
- ae2.mdx
- example_builds.mdx
- overview.mdx
- core.mdx
- me.mdx
- planter.mdx
- power.mdx
- StorageTests
- Problem
- Ae2AbsentTests
- JadeRegistration

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

## Communities (82 total, 54 thin omitted)

### Community 0 - "PlanterBlockEntity"
Cohesion: 0.14
Nodes (4): PlanterBlockEntity, PlanterLifecycleTests, Snapshot, SoilSlot

### Community 1 - "CoreScreen"
Cohesion: 0.05
Nodes (13): MeScreen, CoreScreen, Entry, PlanterScreen, StorageScreen, Action, BACK_TO_CORE, OPEN (+5 more)

### Community 3 - "CoreMenu"
Cohesion: 0.10
Nodes (4): CoreScreenTests, Setup, CoreMenu, MPNetwork

### Community 5 - "PowerBlockEntity"
Cohesion: 0.12
Nodes (4): ExternalHandler, PowerBlockEntity, InsertView, PowerPool

### Community 6 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.06
Nodes (9): AteBridge, PlantRecipe, RecipeMemo, AteBridgeTests, PlanterMenu, DataSlot, UpgradeSlot, SeedBinSlot (+1 more)

### Community 7 - "OutputSink"
Cohesion: 0.09
Nodes (4): FakeMeLink, MeLinkTests, MeLink, OutputSink

### Community 8 - "Capability Registration"
Cohesion: 0.16
Nodes (4): Ae2Integration, MPCreativeTab, MPItems, MPMenus

### Community 10 - "Client Outlines and Screens"
Cohesion: 0.08
Nodes (4): Ae2Client, ModularPlantersClient, PlanterOutlines, MPDataGen

### Community 11 - "CoreBlockEntity.java"
Cohesion: 0.12
Nodes (7): PartBlock, PartType, NOT_A_PART, ScanResult, StructureScanner, CoreFinder, Multiblock

### Community 12 - "Docs Mockups and License"
Cohesion: 0.09
Nodes (33): Core screen mockup, LN Computer Node Table inspiration, Open planter screen with Back to core, Planter block list grouped by seed, Planter multiblock layouts mockup, Raised bed under a cloche, Rosette, Trellis free form (+25 more)

### Community 13 - "Seed Bin"
Cohesion: 0.14
Nodes (4): Combo, Entry, SeedBin, SeedBinTest

### Community 14 - "Planter Ticker and Grouping"
Cohesion: 0.19
Nodes (5): Grouping, AdvanceResult, PlanterTicker, PlanterTickerTest, ReferenceAdvancedPlanter

### Community 18 - "PlanterBlock.java"
Cohesion: 0.12
Nodes (5): MeBlock, CoreBlock, ExportBlock, FertilizerBlock, PlanterBlock

### Community 20 - "Fertilizer Pool and Doorway"
Cohesion: 0.12
Nodes (4): DoorwayHandlers, Budget, FertilizerPool, LiveBlocks

### Community 21 - "Cloche Yield and Remainders"
Cohesion: 0.17
Nodes (4): ClocheYield, RemainderLedger, TexturesExistTest, ClocheYieldTest

### Community 27 - "UpgradeKind"
Cohesion: 0.08
Nodes (14): AteNumbers, Group, GrowthMath, UpgradeKind, NONE, SPEED_MK1, SPEED_MK2, SPEED_MK3 (+6 more)

### Community 30 - "FertilizerBlockEntity"
Cohesion: 0.13
Nodes (4): ExportBlockEntity, FertilizerBlockEntity, ItemStacksResourceHandler, MPBlockEntities

### Community 33 - "net.minecraft.resources.Identifier"
Cohesion: 0.24
Nodes (4): CoreProvider, INSTANCE, PlanterProvider, INSTANCE

### Community 38 - "CoreScreenTests.java"
Cohesion: 0.08
Nodes (11): PlanterStatus, GROWING, IDLE, PAUSED_EXPORT_FULL, PAUSED_FOR_POWER, SHORT_ON_SOIL, MeStatus, NO_CHANNEL (+3 more)

### Community 41 - "CoreTooltipProvider.java"
Cohesion: 0.23
Nodes (4): CoreTooltipProvider, INSTANCE, PlanterTooltipProvider, INSTANCE

### Community 46 - "Planter Power ME Textures"
Cohesion: 0.48
Nodes (7): ME Side Texture, Planter Side On Texture, Planter Side Texture, Planter Top Texture, Power Bottom Texture, Power Side Texture, Power Top Texture

### Community 47 - "Entire Agent Guide"
Cohesion: 0.33
Nodes (3): Entire agent guide, Entire Brain tool, Entire Graph tool

### Community 48 - "Core Export Textures"
Cohesion: 0.40
Nodes (5): Core Side Texture, Core Top Texture, Export Side Texture, Export Top Texture, Frame Side Texture

### Community 54 - "PartType"
Cohesion: 0.11
Nodes (8): CORE, EXPORT, FERTILIZER, FRAME, ME, PLANTER, POWER, UNLOADED

### Community 64 - "ae2.mdx"
Cohesion: 0.50
Nodes (3): Applied Flux, Fertilizer filter, ME block

### Community 65 - "example_builds.mdx"
Cohesion: 0.50
Nodes (3): Raised bed under a cloche, Rosette, Vertical farm

### Community 71 - "me.mdx"
Cohesion: 0.50
Nodes (3): Applied Flux, Fertilizer filter, ME block

### Community 79 - "Problem"
Cohesion: 0.20
Nodes (9): Problem, MISSING_EXPORT, MISSING_PLANTER, MISSING_POWER, NONE, SECOND_CORE, SECOND_ME, TOO_MANY_PARTS (+1 more)

## Knowledge Gaps
- **44 isolated node(s):** `IDLE`, `GROWING`, `SHORT_ON_SOIL`, `PAUSED_FOR_POWER`, `PAUSED_EXPORT_FULL` (+39 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 254 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **54 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CoreBlockEntity` connect `CoreBlockEntity` to `CoreScreen`, `CoreMenu`, `PowerBlockEntity`, `OutputSink`, `CoreBlockEntity.java`, `Docs Mockups and License`, `CoreMenu.java`, `PlanterBlock.java`, `Fertilizer Pool and Doorway`, `net.minecraft.gametest.framework.GameTestHelper`, `Ae2Tests.java`, `net.minecraft.core.BlockPos`, `FertilizerBlockEntity`, `net.minecraft.resources.Identifier`, `CoreScreenTests.java`, `list`, `Ae2Tests`, `PlanterMenu.java`, `MPJadePlugin.java`, `.lastScan`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **What connects `IDLE`, `GROWING`, `SHORT_ON_SOIL` to the rest of the system?**
  _44 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `PlanterBlockEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.1358974358974359 - nodes in this community are weakly interconnected._
- **Why does `PlanterBlockEntity` connect `PlanterBlockEntity` to `CoreMenu`, `net.minecraft.world.item.ItemStack`, `OutputSink`, `PlanterTests.java`, `Seed Bin`, `CoreMenu.java`, `PlanterBlock.java`, `Cloche Yield and Remainders`, `Ae2Tests.java`, `net.minecraft.core.BlockPos`, `UpgradeKind`, `.tickFromCore`, `FertilizerBlockEntity`, `.plant`, `net.minecraft.resources.Identifier`, `MenuTests`, `CoreScreenTests.java`, `ModularPlanters.java`, `PlanterMenu.java`, `MPJadePlugin.java`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Should `CoreScreen` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Why does `Core block` connect `Docs Mockups and License` to `CoreBlockEntity`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Should `CoreMenu` be split into smaller, more focused modules?**
  _Cohesion score 0.10460992907801418 - nodes in this community are weakly interconnected._