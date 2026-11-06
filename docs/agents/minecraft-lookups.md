# Minecraft, NeoForge and addon lookups

Check every class, method, registry id and version against the `minecraft-modding` MCP server (`@adhisang/minecraft-modding-mcp`, run through `bunx`) before writing it. `.mcp.json` is gitignored, so add the server to your own client first.

The `target` decides whether a class is found:

| To read                                 | Target                                                                                                                           |
| --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| Vanilla                                 | `{kind: version, value: <minecraft_version>}`                                                                                    |
| NeoForge source                         | `{kind: dependency, group: net.neoforged, name: neoforge, version: <neo_version>}`                                               |
| NeoForge members, or a source read that fails | `{kind: jar, value: <neoforge-<version>-universal.jar>}`, found under `~/.gradle/caches/modules-2`                         |
| FML, such as `ModList`                  | `jar` target on `net.neoforged.fancymodloader:loader` at the version in the NeoForge pom                                         |
| AE2 and other addons                    | `jar` target on the addon's jar under `~/.gradle/caches/modules-2`                                                               |

- Pass explicit versions. `versionFromProject` reads `neoforge_version` and ignores `neo_version`, so `gradle.properties` carries both with the same value. Change them together.
- `workspace` targets miss NeoForge and FML classes, because the patched jars in `build/moddev/artifacts` do not contain them.
- Say what could not be verified when a lookup fails and the answer matters.
