# Modular Planters

An addon for Agritech: Evolved that replaces many Advanced Planters with a few multiblocks. A block of N growing seeds produces, draws power and costs about what N Advanced Planters would.

## Language

### The multiblock

**Multiblock**:
A Modular Planter Core together with every part connected to it face to face, directly or through other parts. Diagonal contact does not connect.
_Avoid_: structure, machine

**Formed**:
A multiblock that meets the building rules and is running. A multiblock that is not formed keeps every block's contents but grows nothing.
_Avoid_: active, assembled

**Problem**:
The reason a multiblock is not formed, such as a second Core, too many parts, or a missing required part.
_Avoid_: error

**Part**:
Any Modular Planter block that joins a multiblock, other than the Core: Power, Fertilizer, Export, Planter, ME or Frame.
_Avoid_: module, component

**Modular Planter Core**:
The block that forms the multiblock, applies the built-in cloche to every Modular Planter Block, and opens the Core screen. It stores nothing itself.
_Avoid_: Planter Core, controller

**Doorway**:
The three resources a formed Core exposes to outside connections: energy in, fertilizer in, and outputs out.
_Avoid_: port, interface

**Modular Planter Power Block**:
Stores the multiblock's energy. Several Power Blocks pool their storage, and energy cannot be extracted from them.
_Avoid_: Planter Power, capacitor

**Modular Planter Fertilizer Block**:
Stores fertilizer for every Modular Planter Block in the multiblock. Several Fertilizer Blocks pool their contents.
_Avoid_: Planter Fertilizer, import block

**Modular Planter Export Block**:
Holds harvested outputs until something pulls them out. Only the multiblock inserts into it.
_Avoid_: Planter Export, output block

**Modular Planter Block**:
Grows one seed type, up to 64 seeds, from one soil type and up to two upgrade types.
_Avoid_: Planter Block, planter

**Modular Planter ME Block**:
An optional Part, present only when AE2 is installed, that connects the multiblock to an AE2 network. A multiblock can have at most one.
_Avoid_: ME interface, Planter ME

**Modular Planter Frame**:
A Part that does nothing except connect other Parts. It counts toward the part cap.
_Avoid_: Planter Frame

### Growing

**Seed**:
Any item an Advanced Planter accepts for planting, saplings included. Each seed carries its own data, such as traits added by other mods.

**Seed bin**:
The Modular Planter Block's seed slot. It holds up to 64 seeds of one item type, each with its own data, in the order they were added.
_Avoid_: seed stack

**Soil**:
The one soil type in a Modular Planter Block, up to 64 items. It limits how many seeds can grow.

**Upgrade**:
An ATE Speed or Yield upgrade item in a Modular Planter Block. One upgrade item covers one seed.
_Avoid_: module (ATE's own names and the `soil_and_modules` save key keep it)

**Growing seed**:
A seed that has soil to grow in. The number of growing seeds is the smaller of the seed count and the soil count. The rest are idle.
_Avoid_: active seed

**Upgrade coverage**:
How many growing seeds each upgrade slot reaches. Both slots cover the same seeds first, in bin order.

**Group**:
A set of growing seeds in one Modular Planter Block with the same upgrade set and identical seed data. A group grows and harvests together, like that many Advanced Planters in lockstep.
_Avoid_: batch, cohort, stack

**Fertilized**:
A group that claimed enough fertilizer for all its seeds that tick. A group that cannot claim runs unfertilized.

**Built-in cloche**:
The cloche effect every Modular Planter Block gets from the Core. It is always on, needs no item, and uses ATE's configured cloche bonuses.
_Avoid_: cloche dome (the Core's recipe ingredient)

**Remainder**:
The fraction of a yield bonus that ATE's rounding would discard. The built-in cloche carries it forward and adds one item each time it reaches a whole one.
_Avoid_: leftover, rounding bonus

**Held output**:
A drop that did not fit in the Export Blocks or ME. The Modular Planter Block pauses until all held outputs have moved out.
_Avoid_: overflow

**Status**:
What a Modular Planter Block is doing: growing, short on soil, paused for power, paused for a full export, or idle.

### Screens

**Core screen**:
The Core's screen. It shows the multiblock's totals and one row per Modular Planter Block, and can open any block's own screen remotely.
_Avoid_: controller screen

**Seed heading**:
A collapsible heading on the Core screen that lists Modular Planter Blocks by seed type.
_Avoid_: group

### Outside the mod

**Agritech: Evolved (ATE)**:
The mod this one extends. It owns the crops, soils, fertilizers, upgrades and the numbers everything is measured against.
_Avoid_: Agritech

**Advanced Planter**:
ATE's planter block, the baseline a Modular Planter Block must match.
_Avoid_: Powered Planter

**Optional mod**:
AE2, Applied Flux, Logistics Networks, Oracle Index or Jade. The mod loads and works with any of them missing.
_Avoid_: soft dependency, addon

**Guide**:
The in-game Oracle Index book that explains the mod.
_Avoid_: wiki, manual
