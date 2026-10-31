# Graph Report - ModularPlanters  (2026-10-30)

## Corpus Check
- 154 files · ~39,224 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 10 file(s) not represented in the graph (top: (none) 3, .properties 2, .graphify-bak 1)

## Summary
- 1408 nodes · 4639 edges · 59 communities (26 shown, 33 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 467 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `04d9a315`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Planter Block and GameTests
- Core and Planter Screens
- ME Block Entity and Menu
- CoreMenu
- StructureScannerTest
- .buildTable
- net.minecraft.world.item.ItemStack
- ExportBlockEntity
- Capability Registration
- Client Outlines and Screens
- Structure Scan Problems
- Docs Mockups and License
- Seed Bin
- Planter Ticker and Grouping
- Structure and No-AE2 Tests
- Storage Menu and Fertilizer Block
- Export and Fertilizer Blocks
- ATE Recipes and Data Maps
- Fertilizer Pool and Doorway
- Cloche Yield and Remainders
- GameTest Registration
- StructureScannerTest.java
- net.neoforged.neoforge.transfer.item.ItemResource
- ME and Part Blocks
- Growth Math
- Loot and Data Components
- .assign
- FertilizerBlockEntity
- Core Network Payloads
- Recipe Datagen
- Recipe Tests and Blocks
- Model Datagen
- Tags and Lang Datagen
- CoreScreen.java
- MenuTests
- CoreScreenTests.java
- CoreBlockEntity
- PowerBlockEntity
- Planter Block
- GameTest Listener
- Ae2Tests
- net.neoforged.neoforge.transfer.ResourceHandler
- Planter Power ME Textures
- Entire Agent Guide
- Core Export Textures
- Reference Powered Planter
- Fertilizer Textures
- PartType
- Problem
- PlanterScreen
- .processingTime
- TexturesExistTest

## God Nodes (most connected - your core abstractions)
1. `PlanterBlockEntity` - 64 edges
2. `CoreBlockEntity` - 54 edges
3. `PlanterMenu` - 47 edges
4. `MeBlockEntity` - 37 edges
5. `CoreScreen` - 37 edges
6. `MenuTests` - 36 edges
7. `CoreMenu` - 32 edges
8. `MPBlocks` - 32 edges
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

## Communities (59 total, 33 thin omitted)

### Community 0 - "Planter Block and GameTests"
Cohesion: 0.07
Nodes (9): CoreScreenTests, Setup, DoorwayTests, Fixtures, Rig, PlanterLifecycleTests, Snapshot, PlanterTests (+1 more)

### Community 1 - "Core and Planter Screens"
Cohesion: 0.15
Nodes (4): CoreScreen, Entry, Row, Totals

### Community 2 - "ME Block Entity and Menu"
Cohesion: 0.06
Nodes (6): FluxBridge, MeBlockEntity, FilterSlot, MeMenu, DataSlot, AppliedFluxTests

### Community 4 - "StructureScannerTest"
Cohesion: 0.28
Nodes (3): ScanResult, P, StructureScannerTest

### Community 6 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.05
Nodes (9): PlantRecipe, RecipeMemo, PlanterBlockEntity, AteBridgeTests, ModuleSlot, PlanterMenu, DataSlot, SoilSlot (+1 more)

### Community 7 - "ExportBlockEntity"
Cohesion: 0.17
Nodes (3): ExportBlockEntity, MeLinkTests, OutputSink

### Community 8 - "Capability Registration"
Cohesion: 0.13
Nodes (5): Ae2Integration, ModularPlanters, MPCreativeTab, MPItems, MPMenus

### Community 10 - "Client Outlines and Screens"
Cohesion: 0.09
Nodes (4): Ae2Client, ModularPlantersClient, PlanterOutlines, MPDataGen

### Community 11 - "Structure Scan Problems"
Cohesion: 0.13
Nodes (3): StructureScanner, MPConfig, CoreFinder

### Community 12 - "Docs Mockups and License"
Cohesion: 0.09
Nodes (35): Block textures adapted from Agritech: Evolved, MIT License (MisterD Modding), Core screen mockup, LN Computer Node Table inspiration, Open planter screen with Back to core, Planter block list grouped by seed, Planter multiblock layouts mockup, Raised bed under a cloche (+27 more)

### Community 13 - "Seed Bin"
Cohesion: 0.16
Nodes (4): Combo, Entry, SeedBin, SeedBinTest

### Community 14 - "Planter Ticker and Grouping"
Cohesion: 0.22
Nodes (4): Grouping, AdvanceResult, PlanterTicker, PlanterTickerTest

### Community 15 - "Structure and No-AE2 Tests"
Cohesion: 0.07
Nodes (4): TestStructureProvider, Ae2AbsentTests, MPTestRegistration, TestCase

### Community 18 - "Export and Fertilizer Blocks"
Cohesion: 0.15
Nodes (5): MeBlock, CoreBlock, ExportBlock, FertilizerBlock, MPBlockEntities

### Community 19 - "ATE Recipes and Data Maps"
Cohesion: 0.10
Nodes (3): AteBridge, Coverage, TickContext

### Community 20 - "Fertilizer Pool and Doorway"
Cohesion: 0.21
Nodes (3): Budget, FertilizerPool, LiveBlocks

### Community 21 - "Cloche Yield and Remainders"
Cohesion: 0.16
Nodes (3): ClocheYield, RemainderLedger, ClocheYieldTest

### Community 24 - "StructureScannerTest.java"
Cohesion: 0.06
Nodes (5): CORE, FERTILIZER, PLANTER, UNLOADED, ConditionalDataTest

### Community 27 - "Growth Math"
Cohesion: 0.10
Nodes (13): AteNumbers, GrowthMath, ModuleKind, NONE, SPEED_MK1, SPEED_MK2, SPEED_MK3, YIELD_MK1 (+5 more)

### Community 31 - "Core Network Payloads"
Cohesion: 0.14
Nodes (6): Action, BACK_TO_CORE, OPEN, CoreRequestPayload, CoreTablePayload, SeedBinCombosPayload

### Community 38 - "CoreScreenTests.java"
Cohesion: 0.09
Nodes (11): PlanterStatus, GROWING, IDLE, PAUSED_EXPORT_FULL, PAUSED_FOR_POWER, SHORT_ON_SOIL, MeStatus, NO_CHANNEL (+3 more)

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
Cohesion: 0.19
Nodes (8): PartBlock, PowerBlock, PartType, EXPORT, FRAME, ME, NOT_A_PART, POWER

### Community 55 - "Problem"
Cohesion: 0.22
Nodes (9): Problem, MISSING_EXPORT, MISSING_PLANTER, MISSING_POWER, NONE, SECOND_CORE, SECOND_ME, TOO_MANY_PARTS (+1 more)

## Knowledge Gaps
- **41 isolated node(s):** `IDLE`, `GROWING`, `SHORT_ON_SOIL`, `PAUSED_FOR_POWER`, `PAUSED_EXPORT_FULL` (+36 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 221 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CoreBlockEntity` connect `CoreBlockEntity` to `Planter Block and GameTests`, `CoreMenu`, `StructureScannerTest`, `.buildTable`, `ExportBlockEntity`, `Structure Scan Problems`, `Docs Mockups and License`, `PlanterMenu.java`, `Export and Fertilizer Blocks`, `Fertilizer Pool and Doorway`, `Ae2Tests.java`, `net.neoforged.neoforge.transfer.item.ItemResource`, `ME and Part Blocks`, `FertilizerBlockEntity`, `CoreScreenTests.java`, `Doorway Handler Tests`, `Ae2Tests`, `net.neoforged.neoforge.transfer.ResourceHandler`, `PartType`?**
  _High betweenness centrality (0.091) - this node is a cross-community bridge._
- **What connects `IDLE`, `GROWING`, `SHORT_ON_SOIL` to the rest of the system?**
  _41 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Planter Block and GameTests` be split into smaller, more focused modules?**
  _Cohesion score 0.07014441497200118 - nodes in this community are weakly interconnected._
- **Why does `PlanterBlockEntity` connect `net.minecraft.world.item.ItemStack` to `Planter Block and GameTests`, `CoreMenu`, `MenuTests`, `CoreScreenTests.java`, `PlanterTests.java`, `Seed Bin`, `PlanterMenu.java`, `Export and Fertilizer Blocks`, `ATE Recipes and Data Maps`, `Cloche Yield and Remainders`, `Ae2Tests.java`, `.processingTime`, `ME and Part Blocks`, `Loot and Data Components`, `FertilizerBlockEntity`?**
  _High betweenness centrality (0.064) - this node is a cross-community bridge._
- **Should `Core and Planter Screens` be split into smaller, more focused modules?**
  _Cohesion score 0.14714714714714713 - nodes in this community are weakly interconnected._
- **Why does `Core block` connect `Docs Mockups and License` to `CoreBlockEntity`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **Should `ME Block Entity and Menu` be split into smaller, more focused modules?**
  _Cohesion score 0.05721153846153846 - nodes in this community are weakly interconnected._