package io.github.julianperge.modular_planters.assets;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TexturesExistTest {
    private static final Path MODELS = Path.of("src/generated/resources/assets/modular_planters/models/block");
    private static final Path TEXTURES = Path.of("src/main/resources/assets/modular_planters/textures/block");
    private static final Pattern REFERENCE = Pattern.compile("\"modular_planters:block/([a-z0-9_]+)\"");

    @Test void everyModelTextureExistsAndIsSixteenBySixteen() throws IOException {
        TreeSet<String> names = new TreeSet<>();
        try (Stream<Path> models = Files.list(MODELS)) {
            for (Path model : models.filter(p -> p.toString().endsWith(".json")).toList()) {
                Matcher m = REFERENCE.matcher(Files.readString(model));
                while (m.find()) names.add(m.group(1));
            }
        }
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
}
