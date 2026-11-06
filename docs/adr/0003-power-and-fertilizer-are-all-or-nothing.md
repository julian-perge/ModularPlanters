# Power and fertilizer are all-or-nothing, in block position order

If the Power Blocks cannot cover a Modular Planter Block's full draw for a tick, none of its groups advance that tick, so a block never runs only some of its seeds. Fertilizer works the same way per group: each tick the stored fertilizer is one budget shared by the whole multiblock, and a group either claims one item per seed or runs unfertilized, so one stack cannot speed up more seeds than it could pay for.

Blocks claim power and fertilizer in order of block position. When supply runs short, the same blocks keep running and the rest pause, instead of every block flickering on and off.
