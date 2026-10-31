package io.github.julianperge.modular_planters.wiki;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikiStructureTest {
    private static final String MOD_ID = "modular_planters";
    private static final Path WIKI = Path.of("wiki");
    private static final List<Path> PAGE_ROOTS = List.of(WIKI.resolve("docs"), WIKI.resolve("content"));
    private static final Pattern MOD_ASSET = Pattern.compile("<ModAsset\\s+location=\"([^\"]+)\"");

    @Test
    void wikiIdIsTheModId() throws IOException {
        JsonObject json = readJson(WIKI.resolve("sinytra-wiki.json"));
        assertEquals(MOD_ID, json.get("id").getAsString());
    }

    @Test
    void theBookHasItsFolders() {
        PAGE_ROOTS.forEach(WikiStructureTest::assertRootExists);
        assertTrue(Files.isRegularFile(WIKI.resolve("docs/overview.mdx")), "wiki/docs/overview.mdx must exist");
    }

    @Test
    void everyPageHasATitle() throws IOException {
        for (Path page : pages(PAGE_ROOTS)) {
            List<String> front = frontmatter(page);
            assertTrue(front.stream().anyMatch(line -> line.startsWith("title:")), page + " has no title");
            if (page.startsWith(WIKI.resolve("content"))) {
                String name = page.getFileName().toString().replaceFirst("\\.mdx$", "");
                assertTrue(front.contains("id: " + MOD_ID + ":" + name), page + " needs id: " + MOD_ID + ":" + name);
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
                assertEquals(2, location.length, page + " has a ModAsset location without a namespace: " + matcher.group(1));
                Path image = WIKI.resolve("assets").resolve(location[0]).resolve(location[1] + ".png");
                assertTrue(Files.isRegularFile(image), page + " references missing " + image);
            }
        }
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
