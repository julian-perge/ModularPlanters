# Graph Report - ModularPlanters  (2026-10-30)

## Corpus Check
- Corpus is ~38,913 words - fits in a single context window. You may not need a graph.

## Summary
- 1407 nodes · 4633 edges · 54 communities (29 shown, 25 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 466 edges (avg confidence: 0.85)
- Token cost: 147,149 input · 0 output

## Community Hubs (Navigation)
- Planter Block and GameTests
- Core and Planter Screens
- ME Block Entity and Menu
- Core Menu and Network
- Structure Scanner and Part Types
- Power Block and Pool
- Planter Menu and Seed Slot
- ME Link GameTests
- Capability Registration
- Coverage and Lifecycle Tests
- Client Outlines and Screens
- Structure Scan Problems
- Docs Mockups and License
- Seed Bin
- Planter Ticker and Grouping
- Structure and No-AE2 Tests
- ATE Bridge
- Storage Menu and Fertilizer Block
- Export and Fertilizer Blocks
- Fertilizer Pool and Doorway
- Cloche Yield and Remainders
- GameTest Registration
- Test Assertions and Module Kind
- Output Sink and ME Link
- ME and Part Blocks
- Growth Math
- Loot and Data Components
- Module Coverage
- AE2 Grid Node Types
- Core Network Payloads
- Recipe Datagen
- Recipe Tests and Blocks
- Model Datagen
- Tags and Lang Datagen
- Planter Status Display
- Test Structure Provider
- ME Status
- Core Block Entity
- Conditional Data Tests
- Planter Block
- GameTest Listener
- Fake ME Link
- Core Block
- Planter Power ME Textures
- Entire Agent Guide
- Core Export Textures
- Reference Powered Planter
- Fertilizer Textures

## God Nodes (most connected - your core abstractions)
1. `PlanterBlockEntity` - 65 edges
2. `CoreBlockEntity` - 54 edges
3. `PlanterMenu` - 47 edges
4. `MeBlockEntity` - 37 edges
5. `CoreScreen` - 37 edges
6. `MenuTests` - 35 edges
7. `CoreMenu` - 32 edges
8. `MPBlocks` - 32 edges
9. `ExportBlockEntity` - 31 edges
10. `SeedBin` - 31 edges

## Surprising Connections (you probably didn't know these)
- `Core block` --references--> `CoreBlockEntity`  [INFERRED]
  README.md → src/main/java/io/github/julianperge/modular_planters/blockentity/CoreBlockEntity.java
- `Trellis free form` --conceptually_related_to--> `Multiblock building rules`  [INFERRED]
  mockups/layouts.html → README.md
- `Planter multiblock layouts mockup` --conceptually_related_to--> `Multiblock building rules`  [INFERRED]
  mockups/layouts.html → README.md
- `Core screen mockup` --conceptually_related_to--> `Core screen`  [INFERRED]
  mockups/core-screen.html → README.md
- `Core Side Texture` --semantically_similar_to--> `Frame Side Texture`  [INFERRED] [semantically similar]
  src/main/resources/assets/modular_planters/textures/block/core_side.png → src/main/resources/assets/modular_planters/textures/block/frame_side.png

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Modular Planters multiblock parts** — readme_core_block, readme_power_block, readme_fertilizer_block, readme_export_block, readme_planter_block, readme_frame_block, readme_me_block [EXTRACTED 0.95]
- **Agent guide entry points** — agents, claude, _entire_agent_guide [EXTRACTED 0.95]

## Communities (54 total, 25 thin omitted)

### Community 0 - "Planter Block and GameTests"
Cohesion: 0.05
Nodes (10): PlanterBlockEntity, DoorwayTests, Fixtures, Rig, MenuTests, Rig, PlanterLifecycleTests, Snapshot (+2 more)

### Community 1 - "Core and Planter Screens"
Cohesion: 0.08
Nodes (6): MeScreen, CoreScreen, Entry, PlanterScreen, StorageScreen, Row

### Community 2 - "ME Block Entity and Menu"
Cohesion: 0.07
Nodes (6): FluxBridge, MeBlockEntity, FilterSlot, MeMenu, DataSlot, AppliedFluxTests

### Community 3 - "Core Menu and Network"
Cohesion: 0.09
Nodes (7): CoreScreenTests, Setup, CoreMenu, Action, BACK_TO_CORE, OPEN, MPNetwork

### Community 4 - "Structure Scanner and Part Types"
Cohesion: 0.10
Nodes (14): PartType, CORE, EXPORT, FERTILIZER, FRAME, ME, NOT_A_PART, PLANTER (+6 more)

### Community 5 - "Power Block and Pool"
Cohesion: 0.09
Nodes (4): ExternalHandler, PowerBlockEntity, InsertView, PowerPool

### Community 6 - "Planter Menu and Seed Slot"
Cohesion: 0.10
Nodes (3): PlanterMenu, DataSlot, SeedBinSlot

### Community 7 - "ME Link GameTests"
Cohesion: 0.13
Nodes (3): ExportBlockEntity, Ae2Tests, MeLinkTests

### Community 8 - "Capability Registration"
Cohesion: 0.12
Nodes (6): Ae2Integration, ModularPlanters, MPBlockEntities, MPCreativeTab, MPItems, MPMenus

### Community 10 - "Client Outlines and Screens"
Cohesion: 0.09
Nodes (4): Ae2Client, ModularPlantersClient, PlanterOutlines, MPDataGen

### Community 11 - "Structure Scan Problems"
Cohesion: 0.09
Nodes (13): Problem, MISSING_EXPORT, MISSING_PLANTER, MISSING_POWER, NONE, SECOND_CORE, SECOND_ME, TOO_MANY_PARTS (+5 more)

### Community 12 - "Docs Mockups and License"
Cohesion: 0.09
Nodes (35): Block textures adapted from Agritech: Evolved, MIT License (MisterD Modding), Core screen mockup, LN Computer Node Table inspiration, Open planter screen with Back to core, Planter block list grouped by seed, Planter multiblock layouts mockup, Raised bed under a cloche (+27 more)

### Community 13 - "Seed Bin"
Cohesion: 0.14
Nodes (4): Combo, Entry, SeedBin, SeedBinTest

### Community 14 - "Planter Ticker and Grouping"
Cohesion: 0.17
Nodes (6): Group, Grouping, ModuleSet, AdvanceResult, PlanterTicker, PlanterTickerTest

### Community 15 - "Structure and No-AE2 Tests"
Cohesion: 0.10
Nodes (3): Ae2AbsentTests, StructureTests, TestCase

### Community 16 - "ATE Bridge"
Cohesion: 0.13
Nodes (6): AteBridge, PlantRecipe, RecipeMemo, AteBridgeTests, ModuleSlot, SoilSlot

### Community 17 - "Storage Menu and Fertilizer Block"
Cohesion: 0.19
Nodes (3): FertilizerBlockEntity, StorageMenu, TakeOnlySlot

### Community 18 - "Export and Fertilizer Blocks"
Cohesion: 0.18
Nodes (3): ExportBlock, FertilizerBlock, PowerBlock

### Community 20 - "Fertilizer Pool and Doorway"
Cohesion: 0.17
Nodes (3): DoorwayHandlers, Budget, FertilizerPool

### Community 21 - "Cloche Yield and Remainders"
Cohesion: 0.18
Nodes (3): ClocheYield, RemainderLedger, ClocheYieldTest

### Community 25 - "Output Sink and ME Link"
Cohesion: 0.15
Nodes (3): LiveBlocks, MeLink, OutputSink

### Community 27 - "Growth Math"
Cohesion: 0.14
Nodes (4): AteNumbers, GrowthMath, AteDefaults, GrowthMathTest

### Community 29 - "Module Coverage"
Cohesion: 0.12
Nodes (9): ModuleKind, NONE, SPEED_MK1, SPEED_MK2, SPEED_MK3, YIELD_MK1, YIELD_MK2, YIELD_MK3 (+1 more)

### Community 31 - "Core Network Payloads"
Cohesion: 0.20
Nodes (4): CoreRequestPayload, CoreTablePayload, Totals, SeedBinCombosPayload

### Community 36 - "Planter Status Display"
Cohesion: 0.13
Nodes (6): PlanterStatus, GROWING, IDLE, PAUSED_EXPORT_FULL, PAUSED_FOR_POWER, SHORT_ON_SOIL

### Community 38 - "ME Status"
Cohesion: 0.14
Nodes (5): MeStatus, NO_CHANNEL, NONE, OFFLINE, ONLINE

### Community 46 - "Planter Power ME Textures"
Cohesion: 0.48
Nodes (7): ME Side Texture, Planter Side On Texture, Planter Side Texture, Planter Top Texture, Power Bottom Texture, Power Side Texture, Power Top Texture

### Community 47 - "Entire Agent Guide"
Cohesion: 0.33
Nodes (3): Entire agent guide, Entire Brain tool, Entire Graph tool

### Community 48 - "Core Export Textures"
Cohesion: 0.40
Nodes (5): Core Side Texture, Core Top Texture, Export Side Texture, Export Top Texture, Frame Side Texture

## Knowledge Gaps
- **41 isolated node(s):** `IDLE`, `GROWING`, `SHORT_ON_SOIL`, `PAUSED_FOR_POWER`, `PAUSED_EXPORT_FULL` (+36 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 221 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **25 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CoreBlockEntity` connect `Core Block Entity` to `Core Menu and Network`, `Structure Scanner and Part Types`, `Power Block and Pool`, `ME Status`, `ME Link GameTests`, `Doorway Handler Tests`, `Coverage and Lifecycle Tests`, `Capability Registration`, `Structure Scan Problems`, `Docs Mockups and License`, `Structure and No-AE2 Tests`, `Export and Fertilizer Blocks`, `Fertilizer Pool and Doorway`, `Menu Payload Tests`, `Output Sink and ME Link`, `ME and Part Blocks`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **What connects `IDLE`, `GROWING`, `SHORT_ON_SOIL` to the rest of the system?**
  _41 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Planter Block and GameTests` be split into smaller, more focused modules?**
  _Cohesion score 0.05374088502647088 - nodes in this community are weakly interconnected._
- **Why does `PlanterBlockEntity` connect `Planter Block and GameTests` to `Core Menu and Network`, `Planter Status Display`, `ME Status`, `Planter Menu and Seed Slot`, `Capability Registration`, `Coverage and Lifecycle Tests`, `Seed Bin`, `Planter Ticker and Grouping`, `ATE Bridge`, `Storage Menu and Fertilizer Block`, `Export and Fertilizer Blocks`, `ATE Recipes and Data Maps`, `Cloche Yield and Remainders`, `Menu Payload Tests`, `ME and Part Blocks`, `Loot and Data Components`, `Module Coverage`?**
  _High betweenness centrality (0.060) - this node is a cross-community bridge._
- **Should `Core and Planter Screens` be split into smaller, more focused modules?**
  _Cohesion score 0.07879428873611846 - nodes in this community are weakly interconnected._
- **Why does `MPBlocks` connect `Recipe Tests and Blocks` to `Recipe Datagen`, `Model Datagen`, `Tags and Lang Datagen`, `Capability Registration`, `Doorway Handler Tests`, `Coverage and Lifecycle Tests`, `Structure Scan Problems`, `Planter Block`, `Core Block`, `Storage Menu and Fertilizer Block`, `Export and Fertilizer Blocks`, `Menu Payload Tests`, `ME and Part Blocks`, `Loot and Data Components`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **Should `ME Block Entity and Menu` be split into smaller, more focused modules?**
  _Cohesion score 0.06594071385359952 - nodes in this community are weakly interconnected._