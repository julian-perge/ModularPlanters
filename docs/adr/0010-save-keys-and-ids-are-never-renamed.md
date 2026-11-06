# Save keys and registry ids are never renamed

Renaming a save key or registry id breaks existing worlds, so player-facing words can change while stored names stay. Modules became upgrades in the code and docs on 2026-10-31, but the save key `soil_and_modules` kept its old name. ATE's own names, such as its module tags and config getters, stay as ATE has them.
