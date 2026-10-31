# Modular Planters

A NeoForge addon for Agritech: Evolved (ATE) that replaces many Powered Planters with a few multiblocks. A planter block with N growing seeds produces and draws what N Powered Planters would, and one connection to the core runs the whole multiblock.

It targets Minecraft 26.1.2 and NeoForge 26.1.2.121. ATE 2.2.0 or newer is required. An optional ME block connects a multiblock to an AE2 network, with Applied Flux support. It needs AE2 and does not exist without it.

## Blocks

| Block | Job |
|---|---|
| Core | Forms the multiblock, applies the built-in cloche, opens the core screen, and is the doorway for energy, fertilizer and outputs. It stores nothing itself. |
| Power | Stores energy. Several power blocks pool their storage. Energy goes in but can't be taken out. A broken power block keeps its energy in the dropped item. |
| Fertilizer | 27 slots shared by every planter block. It accepts only ATE fertilizers. |
| Export | 54 slots that hold outputs until something pulls them out. Only the multiblock can insert. Anything can extract. |
| Planter | Grows one seed type, up to 64 seeds. It has a seed slot, a soil slot and two module slots. |
| Frame | Connects parts and does nothing else. |
| ME | Connects the multiblock to an AE2 network. It exists only when AE2 is installed. See AE2 and Applied Flux below. |

Fertilizer and export blocks drop their contents when broken.

### The core as a doorway

While the multiblock is formed, the core exposes two connections:

- Energy inserted into the core goes to the power blocks.
- Items inserted into the core go to the fertilizer blocks, and only fertilizers are accepted. Items extracted from the core come out of the export blocks.

So one Logistics Networks node on the core, or one cable or pipe, runs the whole multiblock. The power, fertilizer and export blocks also expose their own contents directly. While the multiblock is not formed, the core exposes nothing.

### Core screen

The core screen shows the formed state and part count, the reason when the multiblock is not formed, stored power and draw, fertilizer stored, how full the export blocks are, and the ME status. It lists every planter block with its seed, growing count, module coverage, status and position. Each row has an Outline toggle that shows or hides a glowing outline of that block, for you only, and an Open button that opens the planter block's screen remotely. The server checks that you are within 4 blocks of the core and that the planter block belongs to that core's formed multiblock.

## Building rules

- The multiblock is the core plus every part connected to it face to face, directly or through other parts. Diagonal contact does not connect.
- It needs at least one power block, one export block and one planter block. Fertilizer and frame blocks are optional.
- At most 24 parts by default (the `part_cap` server option), not counting the core. Frames count.
- If the connected parts reach a second core, neither core forms. The core screen names the problem block and its position.
- When the shape breaks, planter blocks stop and every block keeps its contents.
- If a connected part is in an unloaded chunk, the multiblock pauses until the chunk loads, and the core screen says so.
- The core can't tell what is in an unloaded chunk, so a multiblock built against the edge of the loaded area also pauses until the next chunk loads, even when no part is in it.

## How a planter block grows

- Growing seeds G is the smaller of the seed count and the soil count. Seeds past that stay idle.
- One module covers one seed. Module 1 covers the first seeds in the bin and module 2 does the same, so seeds fill both modules first.
- Seeds with the same modules and the same seed data grow as a group. Each seed levels on its own, as it would in its own Powered Planter.
- Growth time, power and fertilizer follow ATE's formulas and are read from ATE's live config. Power is drawn each tick for every seed that is still growing. If the power blocks can't cover a planter block's full draw, its seeds stop growing for that tick. Seeds that are already ready still harvest.
- Fertilizer is taken from the first non-empty slot of the fertilizer blocks. Each tick, planter blocks claim it in position order, and each group claims one item for every one of its seeds or nothing. A group without a claim grows and harvests unfertilized that tick. A claim uses up fertilizer only when its group harvests, so the fertilizer stored speeds up only as many seeds as it can pay for. A paused group keeps claiming each tick, the way a paused Powered Planter keeps its own fertilizer, so planter blocks later in the order can run unfertilized while it waits.
- When there is no room in the export blocks, planter blocks wait. Nothing is voided.
- Breaking a block drops everything it holds.
- Picking up the whole seed run and putting it back, or scrolling seeds out and back in one at a time, restarts those seeds' growth. No items are lost.

### Built-in cloche

The core applies a cloche to every planter block. It always works and needs no item. It uses ATE's configured cloche speed and yield bonuses. ATE rounds a 1-item drop at 1.1 yield back to 1, so a cloche does nothing for Mystical Agriculture essence there. The built-in cloche keeps the fraction that rounding throws away and gives one extra item each time it adds up to 1. With the default numbers that is one extra item every tenth harvest.

## Recipes

These are shaped crafting recipes shipped as datapack JSON, so a pack can replace them.

| Block | Pattern | Key |
|---|---|---|
| Frame | `PPP` `PIP` `PPP` | P = ATE Powered Planter, I = block of iron |
| Planter | `FFF` `FIF` `FFF` | F = Frame, I = ATE Infused Farmland |
| Core | `IGI` `GCG` `IDI` | C = ATE Cloche Dome, G = glass (`#c:glass_blocks`), I = block of iron, D = block of diamond |
| Power | `IRI` `RCR` `IRI` | C = ATE Capacitor Tier 3, R = block of redstone, I = block of iron |
| Fertilizer | `IBI` `BSB` `IBI` | S = ATE Fertilizer Spreader, B = bone block, I = block of iron |
| Export | `IHI` `HSH` `IHI` | S = ATE Silo, H = hopper, I = block of iron |
| ME | `IFI` `FXF` `IFI` | X = any ME Interface (`#ae2:interface`), F = fluix crystal (`#c:gems/fluix`), I = block of iron. Exists only when AE2 is installed. |

A planter block costs 8 frames, so 64 Powered Planters plus 8 blocks of iron and one Infused Farmland.

## AE2 and Applied Flux

The mod runs without AE2, Applied Flux, or both. Without AE2 there is no ME block. Without Applied Flux the ME block does not touch power.

### ME block

- A multiblock can have at most one ME block. A second one stops the multiblock from forming. The ME block uses one AE2 channel.
- Harvests go into ME first and whatever ME does not take goes into the export blocks. When ME is offline (no power or no channel), everything goes to the export blocks.
- Every `me_interval` ticks the ME block moves the export blocks' contents into ME and tops the fertilizer blocks up from ME with the fertilizer set in its filter slot.
- Item moves cost AE energy, the same as an ME Interface. The ME block also draws 1 AE/t while idle.
- The core screen shows the ME status: None, Offline, No channel or Online.

### Fertilizer filter

The filter slot is a ghost slot. Click it with a fertilizer to set it, or with an empty hand to clear it. Shift-clicking a fertilizer in your inventory also sets it. No item is used up. Only ATE fertilizers are accepted. The top-up fills free room in the fertilizer blocks even when they hold another fertilizer, and the first fertilizer found is still the one planters use.

### Applied Flux

With Applied Flux installed, the ME block fills the power blocks' free room from ME's FE every tick, before the planter blocks run, so the power blocks stay full. Filling empty power blocks takes up to their whole buffer (4,000,000 RF each by default) out of ME once.

A Flux Accessor placed against the core or a power block also charges it, with or without an ME block.

## Logistics Networks

Logistics Networks is optional and this mod has no code for it. A node on the core works through the core's connections. The node needs an upgrade sized to the multiblock. A node uses its best upgrade for every channel, and these are Logistics Networks' default limits:

| Node upgrade | Energy per tick | Items a second |
|---|---|---|
| None | 2,000 RF | 0.8 |
| Iron | 10,000 RF | 64 |
| Gold | 50,000 RF | 256 |
| Diamond | 250,000 RF | 5,120 |
| Netherite | about 2.1 billion RF | 200,000 |

Example with ATE's default numbers: one full planter block of 64 nickel seeds with 2 MK3 Speed Modules, insanium farmland and the built-in cloche.

- Power: it draws 32,768 RF/t whatever the seeds' traits.
- Items: with seeds that have no traits it puts out about 24 items a second. With seeds maxed in Productive Farming's Growth and Yield it puts out about 320 items a second, which is more than a gold upgrade moves.
- Upgrade needed: a diamond upgrade covers up to 7 such planter blocks, and power is the limit. More than 7 needs a netherite upgrade, or more nodes on the power blocks.

## Server config

| Option | Default | Meaning |
|---|---|---|
| `part_cap` | 24 | Most parts in one multiblock, not counting the core |
| `power_block_buffer` | 4,000,000 | RF each power block holds |
| `me_interval` | 20 | Ticks between ME item moves and fertilizer top-ups (1 to 1200) |

ATE's numbers are never copied into this config. They are read from ATE.

## Jade

With Jade installed, looking at a core shows "Formed: N / cap parts" or "Not formed: <problem>". Looking at a planter block shows its status and how many of its seeds are growing, as "growing/total seeds growing". Both can be turned off in Jade's plugin settings.

## Guide

The in-game guide is an Oracle Index book. Its pages live in `wiki/` and `processResources` copies them into the jar. Oracle Index is optional and this mod has no code for it.

## Build

```
./gradlew build
```

The jar is `build/libs/modular_planters-0.1.0.jar`. `./gradlew runGameTestServer` runs the in-game tests.

## License

MIT, see `LICENSE`. The block textures are adapted from ATE's, which are MIT. ATE's copyright notice is in `LICENSE-ATE.txt` and ships in the jar.
