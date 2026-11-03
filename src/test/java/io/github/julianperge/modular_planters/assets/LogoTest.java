package io.github.julianperge.modular_planters.assets;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogoTest {
    private static final String SUMMARY =
            "Build Agritech: Evolved planters into one multiblock with shared power, fertilizer and outputs";

    @Test
    void modListLogoIs256Square() throws IOException {
        assertSquareTransparentLogo(Path.of("src/main/resources/logo.png"), 256);
    }

    @Test
    void pageLogoIs512Square() throws IOException {
        assertSquareTransparentLogo(Path.of("docs/media/logo.png"), 512);
    }

    @Test
    void modsTomlUsesLogoAndSummary() throws IOException {
        String toml = Files.readString(Path.of("src/main/templates/META-INF/neoforge.mods.toml"));
        int mods = toml.indexOf("[[mods]]");
        int nextTable = toml.indexOf("[[", mods + 1);
        assertTrue(mods >= 0 && nextTable > mods, "neoforge.mods.toml has no [[mods]] table");
        String modsTable = toml.substring(mods, nextTable);
        assertTrue(modsTable.lines().anyMatch(line -> line.strip().equals("logoFile=\"logo.png\"")),
                "[[mods]] needs logoFile=\"logo.png\"");
        int description = modsTable.indexOf("description='''");
        assertTrue(description >= 0, "[[mods]] has no description block");
        int end = modsTable.indexOf("'''", description + "description='''".length());
        assertEquals(SUMMARY, modsTable.substring(description + "description='''".length(), end).strip());
    }

    private static void assertSquareTransparentLogo(Path file, int size) throws IOException {
        assertTrue(Files.isRegularFile(file), file + " does not exist");
        BufferedImage image = ImageIO.read(file.toFile());
        assertEquals(size, image.getWidth(), file + " width");
        assertEquals(size, image.getHeight(), file + " height");
        assertTrue(image.getColorModel().hasAlpha(), file + " has no alpha channel");
        assertEquals(0, image.getRGB(0, 0) >>> 24, file + " corner should be transparent");
    }
}
