# Optional mods and the ME link

Agritech: Evolved is the only required dependency. DEVELOPMENT.md lists the `-Pno...` switches that leave a dev mod out of a run.

- AE2 classes stay in the `ae2` package behind `ModList.get().isLoaded("ae2")`, and generated data that references AE2 or `modular_planters:me` carries a `mod_loaded` condition.
- `MeLink` implementations return at most the requested amount, and a simulated call changes nothing. Callers subtract the result without clamping, so over-reporting voids items.
