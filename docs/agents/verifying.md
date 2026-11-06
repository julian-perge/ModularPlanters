# Verifying changes

- After changing `datagen`, run `just data`. Leave the regenerated files in `src/generated/resources` for the owner to commit. The Build workflow fails if `runData` would change them.
- GameTests cannot prove that Jade tooltips or the Oracle Index book work. Jade swallows plugin exceptions and checks its provider rules only on a physical client, so ask the owner for an in-game check.
