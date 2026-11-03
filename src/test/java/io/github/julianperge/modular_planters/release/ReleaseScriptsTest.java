package io.github.julianperge.modular_planters.release;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReleaseScriptsTest {
  private static final Path TOOLS = Path.of("tools").toAbsolutePath();
  private static final String CHANGELOG =
      """
      # Changelog

      ## 26.1.2-0.2.0

      - b

      ## 26.1.2-0.1.0

      - a

      """;

  @Test
  void changelogSectionPrintsOnlyThatVersion(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("CHANGELOG.md"), CHANGELOG);
    Result result =
        run(dir, "changelog-section.sh", "26.1.2-0.1.0", dir.resolve("CHANGELOG.md").toString());
    assertEquals(0, result.exit(), result.stderr());
    assertEquals("- a\n", result.stdout());
  }

  @Test
  void changelogSectionFailsForAMissingVersion(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("CHANGELOG.md"), CHANGELOG);
    assertEquals(
        1,
        run(dir, "changelog-section.sh", "26.1.2-9.9.9", dir.resolve("CHANGELOG.md").toString())
            .exit());
  }

  @Test
  void checkReleaseAcceptsAMatchingTag(@TempDir Path dir) throws Exception {
    writeRepo(dir, "0.1.0-beta", "## 26.1.2-0.1.0-beta\n\n- first\n");
    Result result = run(dir, "check-release.sh", "26.1.2-0.1.0-beta", dir.toString());
    assertEquals(0, result.exit(), result.stderr());
  }

  @Test
  void checkReleaseRejectsAMismatchedTag(@TempDir Path dir) throws Exception {
    writeRepo(dir, "0.1.0-beta", "## 26.1.2-0.1.0-beta\n\n- first\n");
    assertEquals(1, run(dir, "check-release.sh", "26.1.2-0.1.1", dir.toString()).exit());
  }

  @Test
  void checkReleaseRejectsATagWithoutChangelog(@TempDir Path dir) throws Exception {
    writeRepo(dir, "0.1.0", "## 26.1.2-0.0.9\n\n- older\n");
    assertEquals(1, run(dir, "check-release.sh", "26.1.2-0.1.0", dir.toString()).exit());
  }

  @Test
  void checkReleaseAcceptsATwoPartMinecraftVersion(@TempDir Path dir) throws Exception {
    writeRepo(dir, "26.3", "0.2.0", "## 26.3-0.2.0\n\n- second\n");
    Result result = run(dir, "check-release.sh", "26.3-0.2.0", dir.toString());
    assertEquals(0, result.exit(), result.stderr());
  }

  @Test
  void checkReleaseRejectsAModVersionWithoutThreeParts(@TempDir Path dir) throws Exception {
    writeRepo(dir, "0.2", "## 26.1.2-0.2\n\n- second\n");
    assertEquals(1, run(dir, "check-release.sh", "26.1.2-0.2", dir.toString()).exit());
  }

  @Test
  void checkReleaseRejectsASuffixOtherThanAlphaOrBeta(@TempDir Path dir) throws Exception {
    writeRepo(dir, "0.2.0-rc1", "## 26.1.2-0.2.0-rc1\n\n- second\n");
    assertEquals(1, run(dir, "check-release.sh", "26.1.2-0.2.0-rc1", dir.toString()).exit());
  }

  private static void writeRepo(Path dir, String modVersion, String changelog) throws IOException {
    writeRepo(dir, "26.1.2", modVersion, changelog);
  }

  private static void writeRepo(
      Path dir, String minecraftVersion, String modVersion, String changelog) throws IOException {
    Files.writeString(
        dir.resolve("gradle.properties"),
        "minecraft_version="
            + minecraftVersion
            + "\nmod_id=modular_planters\nmod_version="
            + modVersion
            + "\n");
    Files.writeString(dir.resolve("CHANGELOG.md"), "# Changelog\n\n" + changelog);
  }

  private static Result run(Path dir, String script, String... args)
      throws IOException, InterruptedException {
    List<String> command = new ArrayList<>(List.of("bash", TOOLS.resolve(script).toString()));
    command.addAll(List.of(args));
    Process process = new ProcessBuilder(command).directory(dir.toFile()).start();
    String stdout = new String(process.getInputStream().readAllBytes());
    String stderr = new String(process.getErrorStream().readAllBytes());
    return new Result(process.waitFor(), stdout, stderr);
  }

  private record Result(int exit, String stdout, String stderr) {}
}
