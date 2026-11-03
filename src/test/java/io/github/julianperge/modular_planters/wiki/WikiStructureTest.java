package io.github.julianperge.modular_planters.wiki;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class WikiStructureTest {
  private static final String MOD_ID = "modular_planters";
  private static final Path WIKI = Path.of("wiki");
  private static final List<Path> PAGE_ROOTS =
      List.of(WIKI.resolve("docs"), WIKI.resolve("content"));
  private static final Path RECIPES =
      Path.of("src/generated/resources/data/modular_planters/recipe");
  static final Map<String, String> SHOWN_FOR_TAG =
      Map.of(
          "#c:glass_blocks", "minecraft:glass",
          "#ae2:interface", "ae2:interface",
          "#c:gems/fluix", "ae2:fluix_crystal");
  private static final Pattern CRAFTING_RECIPE =
      Pattern.compile(
          "<CraftingRecipe\\s+slots=\\{\\[(.*?)\\]\\}\\s+result=\"([^\"]+)\"", Pattern.DOTALL);
  private static final Pattern QUOTED = Pattern.compile("'([^']*)'");
  private static final Pattern MOD_ASSET = Pattern.compile("<ModAsset\\s+location=\"([^\"]+)\"");
  private static final Pattern CONFIG_OPTION =
      Pattern.compile("defineInRange\\(\"(\\w+)\", ([\\d_]+), (\\d+), ([\\w.]+)\\)");

  @Test
  void wikiIdIsTheModId() throws IOException {
    JsonObject json = readJson(WIKI.resolve("sinytra-wiki.json"));
    assertEquals(MOD_ID, json.get("id").getAsString());
  }

  @Test
  void theBookHasItsFolders() {
    PAGE_ROOTS.forEach(WikiStructureTest::assertRootExists);
    assertTrue(
        Files.isRegularFile(WIKI.resolve("docs/overview.mdx")),
        "wiki/docs/overview.mdx must exist");
  }

  @Test
  void everyPageHasATitle() throws IOException {
    for (Path page : pages(PAGE_ROOTS)) {
      List<String> front = frontmatter(page);
      assertTrue(
          front.stream().anyMatch(line -> line.startsWith("title:")), page + " has no title");
      if (page.startsWith(WIKI.resolve("content"))) {
        String name = page.getFileName().toString().replaceFirst("\\.mdx$", "");
        assertTrue(
            front.contains("id: " + MOD_ID + ":" + name),
            page + " needs id: " + MOD_ID + ":" + name);
        assertTrue(front.contains("type: block"), page + " needs type: block");
      }
    }
  }

  @Test
  void metaFilesListEveryPageAndNothingElse() throws IOException {
    for (Path root : PAGE_ROOTS) {
      for (Path folder : folders(root)) {
        Set<String> expected = new TreeSet<>();
        try (Stream<Path> children = Files.list(folder)) {
          for (Path child : (Iterable<Path>) children::iterator) {
            String name = child.getFileName().toString();
            if (Files.isDirectory(child) || name.endsWith(".mdx")) {
              expected.add(name);
            }
          }
        }
        Set<String> actual = new TreeSet<>(readJson(folder.resolve("_meta.json")).keySet());
        assertEquals(expected, actual, folder.resolve("_meta.json") + " keys");
      }
    }
  }

  @Test
  void everyModAssetExists() throws IOException {
    for (Path page : pages(PAGE_ROOTS)) {
      Matcher matcher = MOD_ASSET.matcher(Files.readString(page));
      while (matcher.find()) {
        String[] location = matcher.group(1).split(":", 2);
        assertEquals(
            2,
            location.length,
            page + " has a ModAsset location without a namespace: " + matcher.group(1));
        Path image = WIKI.resolve("assets").resolve(location[0]).resolve(location[1] + ".png");
        assertTrue(Files.isRegularFile(image), page + " references missing " + image);
      }
    }
  }

  @Test
  void blockPagesMatchTheirRecipes() throws IOException {
    List<Path> recipes;
    try (Stream<Path> list = Files.list(RECIPES)) {
      recipes = list.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
    }
    assertEquals(7, recipes.size(), "recipe count");
    for (Path recipe : recipes) {
      String name = recipe.getFileName().toString().replaceFirst("\\.json$", "");
      JsonObject json = readJson(recipe);
      Path page = WIKI.resolve("content").resolve(name + ".mdx");
      assertTrue(Files.isRegularFile(page), page + " does not exist");

      Matcher matcher = CRAFTING_RECIPE.matcher(Files.readString(page));
      assertTrue(matcher.find(), page + " has no <CraftingRecipe>");
      String slotsText = matcher.group(1);
      String result = matcher.group(2);
      assertTrue(!matcher.find(), page + " has more than one <CraftingRecipe>");
      assertEquals(
          json.getAsJsonObject("result").get("id").getAsString(), result, page + " result");

      JsonObject key = json.getAsJsonObject("key");
      List<String> expected = new ArrayList<>();
      for (JsonElement row : json.getAsJsonArray("pattern")) {
        for (char c : row.getAsString().toCharArray()) {
          expected.add(c == ' ' ? "" : shown(key.get(String.valueOf(c)).getAsString()));
        }
      }
      List<String> actual = new ArrayList<>();
      Matcher quoted = QUOTED.matcher(slotsText);
      while (quoted.find()) {
        actual.add(quoted.group(1));
      }
      assertEquals(9, actual.size(), page + " slot count");
      assertEquals(expected, actual, page + " slots");
    }
  }

  @Test
  void guideCoversEveryServerConfigOption() throws IOException {
    String config =
        Files.readString(
            Path.of("src/main/java/io/github/julianperge/modular_planters/MPConfig.java"));
    Path page = WIKI.resolve("docs/config.mdx");
    assertTrue(Files.isRegularFile(page), page + " does not exist");
    String text = Files.readString(page);
    Matcher matcher = CONFIG_OPTION.matcher(config);
    int options = 0;
    while (matcher.find()) {
      options++;
      String name = matcher.group(1);
      String shownDefault =
          NumberFormat.getIntegerInstance(Locale.US)
              .format(Long.parseLong(matcher.group(2).replace("_", "")));
      assertTrue(text.contains("`" + name + "`"), page + " does not name `" + name + "`");
      assertTrue(
          text.contains(shownDefault),
          page + " does not give " + name + "'s default " + shownDefault);
    }
    assertEquals(3, options, "server config options in MPConfig");
  }

  private static String shown(String item) {
    if (!item.startsWith("#")) {
      return item;
    }
    String shown = SHOWN_FOR_TAG.get(item);
    assertTrue(shown != null, "tag " + item + " is missing from SHOWN_FOR_TAG");
    return shown;
  }

  private static JsonObject readJson(Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
  }

  private static void assertRootExists(Path root) {
    assertTrue(Files.isDirectory(root), root + " must exist and be a directory");
  }

  private static List<Path> folders(Path root) throws IOException {
    assertRootExists(root);
    try (Stream<Path> walk = Files.walk(root)) {
      return walk.filter(Files::isDirectory).toList();
    }
  }

  private static List<Path> pages(List<Path> roots) throws IOException {
    List<Path> pages = new ArrayList<>();
    for (Path root : roots) {
      assertRootExists(root);
      try (Stream<Path> walk = Files.walk(root)) {
        walk.filter(p -> p.getFileName().toString().endsWith(".mdx")).forEach(pages::add);
      }
    }
    return pages;
  }

  private static List<String> frontmatter(Path page) throws IOException {
    List<String> lines = Files.readAllLines(page);
    assertTrue(!lines.isEmpty() && lines.get(0).equals("---"), page + " must open with ---");
    int end = lines.subList(1, lines.size()).indexOf("---");
    assertTrue(end >= 0, page + " frontmatter is not closed");
    return lines.subList(1, end + 1);
  }
}
