# Build and tooling

- Keep `cache-provider: basic` on setup-gradle. The default provider accepts proprietary Terms of Use, and only the owner can agree to those.
- Hooks in `.claude/settings.json` call tools by name behind a `command -v` check, never by absolute path.
- `entire graph` misses calls through record accessors such as `ctx.outputs().insert(...)`, so check `TickContext` callers in source before refactoring.
- `./gradlew clean` wipes `build/moddev`, and rebuilding it needs the network.
