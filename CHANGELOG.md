# Changelog

Each heading is a release tag, `<minecraft version>-<mod version>`, and its notes go to GitHub, CurseForge and Modrinth.

## 26.1.2-0.2.0

- Planter chunks can now unload while the Planter Core keeps ticking. Before, the core kept every planter chunk loaded. A multiblock with a part in an unloaded chunk now pauses, keeping its progress, until that chunk loads again.
- The blocks have shorter names, since the mod name already says Modular Planters. Existing worlds keep their blocks.
  - Modular Planter Core is now Planter Core
  - Modular Planter Block is now Planter Block
  - Modular Planter Power Block is now Power Block
  - Modular Planter Fertilizer Block is now Fertilizer Block
  - Modular Planter Export Block is now Export Block
  - Modular Planter ME Block is now ME Block
  - Modular Planter Frame is now Planter Frame

## 26.1.2-0.1.0

First release, for Minecraft 26.1.2 and Agritech: Evolved 2.2.0 or newer.

- A multiblock that replaces rows of Advanced Planters: a Modular Planter Core with Modular Planter Blocks that each grow up to 64 seeds
- One connection to the Modular Planter Core runs power, fertilizer and outputs
- The Modular Planter Core's screen lists every Modular Planter Block's seed, upgrades and status
- A built-in cloche that needs no item
- An optional Modular Planter ME Block for AE2, with Applied Flux power
- Jade tooltips and an in-game guide through Oracle Index
- Datapack recipes and a server config
