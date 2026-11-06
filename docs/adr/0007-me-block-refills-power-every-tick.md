# The ME block refills power blocks every tick

With Applied Flux, the ME block fills the Power Blocks' free room from the network's FE each tick, before the Modular Planter Blocks run. Filling empty Power Blocks takes up to their capacity, 4,000,000 RF each, out of the network once. The first rule refilled only what the Modular Planter Blocks asked for the tick before, and in-game testing on 2026-10-30 showed the Power Blocks then always read 0 and looked broken, so the owner changed it.
