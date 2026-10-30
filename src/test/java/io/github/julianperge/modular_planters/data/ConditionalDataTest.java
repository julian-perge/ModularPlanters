package io.github.julianperge.modular_planters.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConditionalDataTest {
    private static final Path GENERATED = Path.of("src/generated/resources");
    private static final List<String> CORE_PARTS = List.of("core", "frame", "power", "fertilizer", "export", "planter");

    private static JsonObject read(String path) throws IOException {
        return JsonParser.parseString(Files.readString(GENERATED.resolve(path))).getAsJsonObject();
    }

    private static List<String> patternOf(JsonObject recipe) {
        List<String> rows = new ArrayList<>();
        for (JsonElement row : recipe.getAsJsonArray("pattern")) rows.add(row.getAsString());
        return rows;
    }

    private static String keyOf(JsonObject recipe, String symbol) {
        return recipe.getAsJsonObject("key").get(symbol).getAsString();
    }

    @Test void meRecipeLootTableAndAdvancementNeedAe2() throws IOException {
        for (String path : List.of("data/modular_planters/recipe/me.json",
                "data/modular_planters/loot_table/blocks/me.json",
                "data/modular_planters/advancement/recipes/misc/me.json")) {
            JsonArray conditions = read(path).getAsJsonArray("neoforge:conditions");
            assertEquals(1, conditions.size(), path);
            JsonObject condition = conditions.get(0).getAsJsonObject();
            assertEquals("neoforge:mod_loaded", condition.get("type").getAsString(), path);
            assertEquals("ae2", condition.get("modid").getAsString(), path);
        }
    }

    @Test void meRecipeMatchesTheSpec() throws IOException {
        JsonObject recipe = read("data/modular_planters/recipe/me.json");
        assertEquals(List.of("IFI", "FXF", "IFI"), patternOf(recipe));
        assertEquals("#ae2:interface", keyOf(recipe, "X"));
        assertEquals("#c:gems/fluix", keyOf(recipe, "F"));
        assertEquals("minecraft:iron_block", keyOf(recipe, "I"));
        assertEquals("modular_planters:me", recipe.getAsJsonObject("result").get("id").getAsString());
    }

    @Test void meLootTableDropsTheMeBlock() throws IOException {
        JsonObject entry = read("data/modular_planters/loot_table/blocks/me.json")
                .getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals("modular_planters:me", entry.get("name").getAsString());
    }

    @Test void pickaxeTagEntryIsOptional() throws IOException {
        JsonArray values = read("data/minecraft/tags/block/mineable/pickaxe.json").getAsJsonArray("values");
        List<JsonObject> optional = values.asList().stream()
                .filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject).toList();
        assertEquals(1, optional.size());
        assertEquals("modular_planters:me", optional.get(0).get("id").getAsString());
        assertFalse(optional.get(0).get("required").getAsBoolean());
        assertEquals(CORE_PARTS.size(), values.size() - 1);
    }

    @Test void coreDataStaysUnconditional() throws IOException {
        for (String part : CORE_PARTS) {
            for (String path : List.of("data/modular_planters/recipe/" + part + ".json",
                    "data/modular_planters/loot_table/blocks/" + part + ".json",
                    "data/modular_planters/advancement/recipes/misc/" + part + ".json")) {
                assertFalse(read(path).has("neoforge:conditions"), path);
            }
        }
    }

    @Test void meHasABlockModelItemModelAndBlockstate() throws IOException {
        JsonObject model = read("assets/modular_planters/models/block/me.json");
        assertEquals("minecraft:block/cube_bottom_top", model.get("parent").getAsString());
        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals("modular_planters:block/me_side", textures.get("side").getAsString());
        assertEquals("modular_planters:block/frame_side", textures.get("top").getAsString());
        assertEquals("modular_planters:block/frame_side", textures.get("bottom").getAsString());
        assertEquals("modular_planters:block/me", read("assets/modular_planters/blockstates/me.json")
                .getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
        assertEquals("modular_planters:block/me", read("assets/modular_planters/items/me.json")
                .getAsJsonObject("model").get("model").getAsString());
    }
}
