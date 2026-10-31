# Graph Report - ModularPlanters  (2026-10-31)

## Corpus Check
- 177 files · ~43,440 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 10 file(s) not represented in the graph (top: (none) 3, .properties 2, .graphify-bak 1)

## Summary
- 1491 nodes · 4864 edges · 78 communities (27 shown, 51 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 482 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d9401e94`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Planter Block and GameTests
- CoreScreen
- MeBlockEntity
- CoreMenu
- StructureScannerTest
- .buildTable
- net.minecraft.world.item.ItemStack
- MeLinkTests
- Capability Registration
- Client Outlines and Screens
- Structure Scan Problems
- Docs Mockups and License
- Seed Bin
- Planter Ticker and Grouping
- Structure and No-AE2 Tests
- CoreMenu.java
- PlanterMenu.java
- PlanterBlock.java
- ATE Recipes and Data Maps
- Fertilizer Pool and Doorway
- Cloche Yield and Remainders
- GameTest Registration
- net.minecraft.gametest.framework.GameTestHelper
- WikiStructureTest.java
- net.neoforged.neoforge.transfer.item.ItemResource
- Growth Math
- Loot and Data Components
- .tickFromCore
- FertilizerBlockEntity
- CoreScreenTests
- Recipe Datagen
- net.minecraft.resources.Identifier
- Model Datagen
- MPBlocks.java
- CoreScreen.java
- MenuTests
- CoreScreenTests.java
- StructureTests
- PowerBlockEntity
- MeMenu
- GameTest Listener
- Ae2Tests
- OutputSink
- Planter Power ME Textures
- Entire Agent Guide
- Core Export Textures
- TestStructureProvider.java
- Fertilizer Textures
- PartType
- MeLink
- WikiStructureTest
- CoreBlockEntity
- ConditionalDataTest
- FluxBridge.java
- MPJadePlugin.java
- net.neoforged.neoforge.transfer.item.ResourceHandlerSlot
- AppliedFluxTests
- Multiblock
- ae2.mdx
- example_builds.mdx
- overview.mdx

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

## Communities (78 total, 51 thin omitted)

### Community 0 - "Planter Block and GameTests"
Cohesion: 0.11
Nodes (7): PlanterBlockEntity, Fixtures, Rig, PlanterLifecycleTests, Snapshot, PlanterTests, SoilSlot

### Community 1 - "CoreScreen"
Cohesion: 0.05
Nodes (24): CoreScreen, Entry, Problem, MISSING_EXPORT, MISSING_PLANTER, MISSING_POWER, NONE, SECOND_CORE (+16 more)

### Community 4 - "StructureScannerTest"
Cohesion: 0.17
Nodes (5): JadeTests, JadeInfo, ScanResult, P, StructureScannerTest

### Community 6 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.07
Nodes (7): AteBridge, PlantRecipe, RecipeMemo, AteBridgeTests, PlanterMenu, DataSlot, SeedBinSlot

### Community 8 - "Capability Registration"
Cohesion: 0.15
Nodes (4): Ae2Integration, MPCreativeTab, MPItems, MPMenus

### Community 10 - "Client Outlines and Screens"
Cohesion: 0.08
Nodes (4): Ae2Client, ModularPlantersClient, PlanterOutlines, MPDataGen

### Community 12 - "Docs Mockups and License"
Cohesion: 0.09
Nodes (35): Block textures adapted from Agritech: Evolved, MIT License (MisterD Modding), Core screen mockup, LN Computer Node Table inspiration, Open planter screen with Back to core, Planter block list grouped by seed, Planter multiblock layouts mockup, Raised bed under a cloche (+27 more)

### Community 13 - "Seed Bin"
Cohesion: 0.12
Nodes (4): Combo, Entry, SeedBin, SeedBinTest

### Community 14 - "Planter Ticker and Grouping"
Cohesion: 0.20
Nodes (5): Grouping, AdvanceResult, PlanterTicker, PlanterTickerTest, ReferencePoweredPlanter

### Community 15 - "Structure and No-AE2 Tests"
Cohesion: 0.12
Nodes (3): Ae2AbsentTests, RecipeTests, TestCase

### Community 18 - "PlanterBlock.java"
Cohesion: 0.09
Nodes (7): MeBlock, CoreBlock, ExportBlock, FertilizerBlock, PartBlock, PlanterBlock, PowerBlock

### Community 20 - "Fertilizer Pool and Doorway"
Cohesion: 0.18
Nodes (3): DoorwayHandlers, Budget, FertilizerPool

### Community 21 - "Cloche Yield and Remainders"
Cohesion: 0.20
Nodes (3): ClocheYield, RemainderLedger, ClocheYieldTest

### Community 27 - "Growth Math"
Cohesion: 0.09
Nodes (13): AteNumbers, GrowthMath, ModuleKind, NONE, SPEED_MK1, SPEED_MK2, SPEED_MK3, YIELD_MK1 (+5 more)

### Community 30 - "FertilizerBlockEntity"
Cohesion: 0.13
Nodes (5): ExportBlockEntity, FertilizerBlockEntity, ItemStacksResourceHandler, PowerBlockEntity, MPBlockEntities

### Community 33 - "net.minecraft.resources.Identifier"
Cohesion: 0.24
Nodes (4): CoreProvider, INSTANCE, PlanterProvider, INSTANCE

### Community 35 - "MPBlocks.java"
Cohesion: 0.08
Nodes (4): MPBlockTags, MPLang, ModularPlanters, MPBlocks

### Community 36 - "CoreScreen.java"
Cohesion: 0.12
Nodes (3): MeScreen, PlanterScreen, StorageScreen

### Community 38 - "CoreScreenTests.java"
Cohesion: 0.08
Nodes (6): PlanterStatus, GROWING, IDLE, PAUSED_EXPORT_FULL, PAUSED_FOR_POWER, SHORT_ON_SOIL

### Community 42 - "MeMenu"
Cohesion: 0.19
Nodes (3): FilterSlot, MeMenu, DataSlot

### Community 45 - "OutputSink"
Cohesion: 0.18
Nodes (3): LiveBlocks, OutputSink, TickContext

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
Cohesion: 0.14
Nodes (10): PartType, CORE, EXPORT, FERTILIZER, FRAME, ME, NOT_A_PART, PLANTER (+2 more)

### Community 64 - "ae2.mdx"
Cohesion: 0.50
Nodes (3): Applied Flux, Fertilizer filter, ME block

### Community 65 - "example_builds.mdx"
Cohesion: 0.50
Nodes (3): Raised bed under a cloche, Rosette, Vertical farm

## Knowledge Gaps
- **43 isolated node(s):** `IDLE`, `GROWING`, `SHORT_ON_SOIL`, `PAUSED_FOR_POWER`, `PAUSED_EXPORT_FULL` (+38 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 244 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CoreBlockEntity` connect `CoreBlockEntity` to `CoreMenu`, `StructureScannerTest`, `.buildTable`, `Structure Scan Problems`, `Docs Mockups and License`, `CoreMenu.java`, `PlanterMenu.java`, `Fertilizer Pool and Doorway`, `ME and Part Blocks`, `FertilizerBlockEntity`, `CoreScreenTests`, `net.minecraft.resources.Identifier`, `CoreScreenTests.java`, `StructureTests`, `Doorway Handler Tests`, `Ae2Tests`, `OutputSink`, `MeLink`, `MPJadePlugin.java`, `Multiblock`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **What connects `IDLE`, `GROWING`, `SHORT_ON_SOIL` to the rest of the system?**
  _43 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Planter Block and GameTests` be split into smaller, more focused modules?**
  _Cohesion score 0.10526315789473684 - nodes in this community are weakly interconnected._
- **Why does `PlanterBlockEntity` connect `Planter Block and GameTests` to `CoreMenu`, `net.minecraft.world.item.ItemStack`, `PlanterTests.java`, `Seed Bin`, `CoreMenu.java`, `PlanterMenu.java`, `PlanterBlock.java`, `ATE Recipes and Data Maps`, `Cloche Yield and Remainders`, `ME and Part Blocks`, `Growth Math`, `Loot and Data Components`, `.tickFromCore`, `FertilizerBlockEntity`, `net.minecraft.resources.Identifier`, `MenuTests`, `CoreScreenTests.java`, `Doorway Handler Tests`, `MPJadePlugin.java`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Should `CoreScreen` be split into smaller, more focused modules?**
  _Cohesion score 0.053482221569203646 - nodes in this community are weakly interconnected._
- **Why does `Core block` connect `Docs Mockups and License` to `CoreBlockEntity`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **Should `MeBlockEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.13538461538461538 - nodes in this community are weakly interconnected._