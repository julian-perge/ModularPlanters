package io.github.julianperge.modular_planters.assets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TexturesExistTest {
    private static final List<Path> MODELS = List.of(
            Path.of("src/main/resources/assets/modular_planters/models"),
            Path.of("src/generated/resources/assets/modular_planters/models"));
    private static final Path TEXTURES = Path.of("src/main/resources/assets/modular_planters/textures");
    private static final String PREFIX = "modular_planters:";

    private static TreeSet<String> referencedTextures() throws IOException {
        TreeSet<String> names = new TreeSet<>();
        for (Path root : MODELS) {
            if (!Files.isDirectory(root)) continue;
            try (Stream<Path> models = Files.walk(root)) {
                for (Path model : models.filter(p -> p.toString().endsWith(".json")).toList()) {
                    JsonObject json = JsonParser.parseString(Files.readString(model)).getAsJsonObject();
                    if (!json.has("textures")) continue;
                    for (JsonElement value : json.getAsJsonObject("textures").asMap().values()) {
                        String reference = value.getAsString();
                        if (reference.startsWith(PREFIX)) names.add(reference.substring(PREFIX.length()));
                    }
                }
            }
        }
        return names;
    }

    @Test void everyModelTextureExistsAndIsSixteenBySixteen() throws IOException {
        TreeSet<String> names = referencedTextures();
        assertFalse(names.isEmpty(), "no modular_planters textures referenced under " + MODELS);

        List<String> problems = new ArrayList<>();
        for (String name : names) {
            Path png = TEXTURES.resolve(name + ".png");
            if (!Files.isRegularFile(png)) {
                problems.add(name + ": missing " + png);
                continue;
            }
            BufferedImage image = ImageIO.read(png.toFile());
            if (image == null || image.getWidth() != 16 || image.getHeight() != 16) {
                problems.add(name + ": not a 16x16 png");
            }
        }
        assertTrue(problems.isEmpty(), "bad textures: " + problems);
    }

    @Test void everyTexturePngIsReferencedByAModel() throws IOException {
        TreeSet<String> names = referencedTextures();
        List<String> unreferenced = new ArrayList<>();
        try (Stream<Path> pngs = Files.walk(TEXTURES)) {
            for (Path png : pngs.filter(p -> p.toString().endsWith(".png")).toList()) {
                String name = TEXTURES.relativize(png).toString().replace('\\', '/');
                if (!names.contains(name.substring(0, name.length() - ".png".length()))) unreferenced.add(name);
            }
        }
        assertTrue(unreferenced.isEmpty(), "pngs no model references: " + unreferenced);
    }
}
