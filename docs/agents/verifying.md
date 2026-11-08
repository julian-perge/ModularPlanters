# Verifying changes

- After changing `datagen`, run `just data`. Leave the regenerated files in `src/generated/resources` for the owner to commit. The Build workflow fails if `runData` would change them.
- GameTests cannot prove that Jade tooltips or the Oracle Index book work. Jade swallows plugin exceptions and checks its provider rules only on a physical client. Check them in a dev client through the Structure Lab bridge (see "Screenshots and client checks with Structure Lab" in DEVELOPMENT.md), and ask the owner before each launch. Without the Structure Lab jar, ask the owner for an in-game check.
- A chunk unload is covered by the GameTest `unloaded_part_pauses_the_multiblock_until_it_loads`, which needs no client.
