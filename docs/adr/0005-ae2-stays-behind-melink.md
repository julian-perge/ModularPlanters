# AE2 and Applied Flux stay behind MeLink

The mod must load and work with AE2, Applied Flux or both missing, and both are compile-time only dependencies. The multiblock code talks to ME only through `MeLink`, an interface with no AE2 types, and every class that touches AE2 sits in the `ae2` package, reached only behind a `ModList` loaded check. `FluxBridge` is the one class that touches Applied Flux.

The ME block's registration, item, recipe and loot table exist only when AE2 is installed, and generated data that references AE2 carries a `mod_loaded` condition. The pools and the Core can be tested with a fake `MeLink` and no AE2 on the classpath.
