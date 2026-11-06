# Release tags are the Minecraft version, then the mod version

A tag such as `26.1.2-0.1.0` is `<minecraft_version>-<mod_version>`, the pattern Logistics Networks uses, which leaves room for 26.3 or 1.21.1 releases from the same repo. The release workflow rejects a tag that does not match `gradle.properties`, and a `-alpha` or `-beta` suffix on `mod_version` sets the release type.

A pushed tag is never moved. The GitHub release, CurseForge and Modrinth are separate jobs, so "Re-run failed jobs" repeats only the one that failed and nothing is uploaded twice, and a fix that needs code is released as a new version.
