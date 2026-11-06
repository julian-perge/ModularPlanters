# The Core keeps its Multiblock while it waits for unloaded chunks

A build can cross a chunk border, and the player can walk away from half of it. While any part's chunk is unloaded the Core reports the Problem "Waiting for chunks to load", so it counts as not formed on its screen, in Jade and for opening a Modular Planter Block remotely. It keeps its Multiblock instead of dropping it, so it does not stop its Modular Planter Blocks: every block keeps its contents, every seed keeps its progress, and nothing is lost or duplicated. The next scan after the chunks load resumes the work.

Treating the missing part as a broken shape would drop the Multiblock and stop every Modular Planter Block each time the player walked away.
