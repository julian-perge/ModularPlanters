package io.github.julianperge.modular_planters.client;

import io.github.julianperge.modular_planters.menu.CoreMenu;
import io.github.julianperge.modular_planters.network.CoreRequestPayload;
import io.github.julianperge.modular_planters.network.CoreTablePayload;
import io.github.julianperge.modular_planters.network.CoreTablePayload.Row;
import io.github.julianperge.modular_planters.network.CoreTablePayload.Totals;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CoreScreen extends AbstractContainerScreen<CoreMenu> {
    private static final int WIDTH = 372;
    private static final int HEIGHT = 220;
    private static final int PAD = 6;
    private static final int BAR_HEIGHT = 18;
    private static final int STATS_TOP = 28;
    private static final int STATS_HEIGHT = 34;
    private static final int HEADER_TOP = 66;
    private static final int TABLE_TOP = 78;
    private static final int TABLE_BOTTOM = HEIGHT - PAD;
    private static final int HEADING_HEIGHT = 12;
    private static final int ROW_HEIGHT = 20;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int COLUMN_GROWING = 30;
    private static final int COLUMN_MODULES = 72;
    private static final int COLUMN_UPGRADED = 112;
    private static final int COLUMN_STATUS = 156;
    private static final int STATUS_WIDTH = 134;

    private static final int PANEL = 0xFF1D2A22;
    private static final int PANEL_EDGE = 0xFF55705C;
    private static final int CELL = 0xFF243429;
    private static final int CELL_EDGE = 0xFF3C5544;
    private static final int TEXT = 0xFFCFE8D4;
    private static final int ACCENT = 0xFF8FF0A4;
    private static final int WARN = 0xFFF0C36B;
    private static final int BAD = 0xFFF08A8A;
    private static final int DIM = 0xFF7F9A86;

    private record Entry(boolean heading, int groupIndex, Component title, Row row) {
        int height() {
            return heading ? HEADING_HEIGHT : ROW_HEIGHT;
        }
    }

    private final Set<Integer> collapsed = new HashSet<>();
    private final List<Entry> entries = new ArrayList<>();
    private CoreTablePayload builtFrom;
    private int scroll;

    public CoreScreen(CoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        CoreTablePayload table = menu.table();
        if (table != builtFrom) {
            builtFrom = table;
            rebuildEntries();
        }
        graphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, PANEL);
        graphics.outline(leftPos, topPos, WIDTH, HEIGHT, PANEL_EDGE);
        graphics.outline(leftPos + 1, topPos + 1, WIDTH - 2, HEIGHT - 2, PANEL_EDGE);
        if (table == null) {
            return;
        }
        extractTopBar(graphics, table.totals());
        extractTotals(graphics, table.totals());
        extractHeaders(graphics);
        extractTable(graphics, mouseX, mouseY);
    }

    // The default labels draw the title and the inventory name, and this screen draws its own top bar instead.
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    private void extractTopBar(GuiGraphicsExtractor graphics, Totals totals) {
        graphics.outline(leftPos + PAD, topPos + PAD, WIDTH - 2 * PAD, BAR_HEIGHT, PANEL_EDGE);
        graphics.text(font, title, leftPos + PAD + 6, topPos + PAD + 5, ACCENT, false);
        Component pill = totals.formed()
                ? Component.translatable("modular_planters.screen.core.formed", totals.partCount(), totals.partCap())
                : problemText(totals);
        int color = totals.formed() ? ACCENT : WARN;
        int width = font.width(pill) + 12;
        int right = leftPos + WIDTH - PAD - 4;
        graphics.outline(right - width, topPos + PAD + 3, width, BAR_HEIGHT - 6, color);
        graphics.text(font, pill, right - width + 6, topPos + PAD + 6, color, false);
    }

    private static Component problemText(Totals totals) {
        Component name = Component.translatable("modular_planters.problem." + totals.problem().name().toLowerCase(Locale.ROOT));
        return totals.problemPos()
                .<Component>map(pos -> Component.translatable("modular_planters.screen.core.problem", name,
                        pos.getX(), pos.getY(), pos.getZ()))
                .orElse(name);
    }

    private void extractTotals(GuiGraphicsExtractor graphics, Totals totals) {
        int boxWidth = (WIDTH - 2 * PAD - 3 * PAD) / 4;
        String[] labels = {"power", "fertilizer", "export", "planters"};
        Component[] values = {
                Component.translatable("modular_planters.screen.core.power_value", compact(totals.energyStored()), compact(totals.energyCapacity())),
                Component.translatable("modular_planters.screen.core.fertilizer_value", count(totals.fertilizerCount())),
                Component.translatable("modular_planters.screen.core.export_value", totals.exportFillPercent()),
                Component.translatable("modular_planters.screen.core.planters_value", builtFrom == null ? 0 : builtFrom.rows().size())
        };
        for (int i = 0; i < labels.length; i++) {
            int x = leftPos + PAD + i * (boxWidth + PAD);
            int y = topPos + STATS_TOP;
            graphics.outline(x, y, boxWidth, STATS_HEIGHT, CELL_EDGE);
            graphics.text(font, Component.translatable("modular_planters.screen.core." + labels[i]), x + 5, y + 4, ACCENT, false);
            graphics.text(font, values[i], x + 5, y + 14, TEXT, false);
        }
        if (totals.drawPerTick() > 0) {
            graphics.text(font, Component.translatable("modular_planters.screen.core.draw", count(totals.drawPerTick())),
                    leftPos + PAD + 5, topPos + STATS_TOP + 24, DIM, false);
        }
    }

    private void extractHeaders(GuiGraphicsExtractor graphics) {
        int y = topPos + HEADER_TOP;
        graphics.text(font, Component.translatable("modular_planters.screen.core.growing"), leftPos + COLUMN_GROWING, y, ACCENT, false);
        graphics.text(font, Component.translatable("modular_planters.screen.core.upgrades"), leftPos + COLUMN_MODULES, y, ACCENT, false);
        graphics.text(font, Component.translatable("modular_planters.screen.core.status"), leftPos + COLUMN_STATUS, y, ACCENT, false);
        graphics.fill(leftPos + PAD, y + 10, leftPos + WIDTH - PAD, y + 11, CELL_EDGE);
    }

    private void extractTable(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (entries.isEmpty()) {
            graphics.text(font, Component.translatable("modular_planters.screen.core.no_planters"),
                    leftPos + PAD + 4, topPos + TABLE_TOP + 4, DIM, false);
            return;
        }
        graphics.enableScissor(leftPos + PAD, topPos + TABLE_TOP, leftPos + WIDTH - PAD, topPos + TABLE_BOTTOM);
        boolean mouseInTable = inTable(mouseX, mouseY);
        int y = topPos + TABLE_TOP - scroll;
        for (Entry entry : entries) {
            if (y + entry.height() > topPos + TABLE_TOP && y < topPos + TABLE_BOTTOM) {
                if (entry.heading()) {
                    extractHeading(graphics, entry, y);
                } else {
                    extractRow(graphics, entry.row(), y, mouseInTable ? mouseX : -1, mouseInTable ? mouseY : -1);
                }
            }
            y += entry.height();
        }
        graphics.disableScissor();
        extractScrollbar(graphics);
    }

    private void extractHeading(GuiGraphicsExtractor graphics, Entry entry, int y) {
        Component marker = Component.literal(collapsed.contains(entry.groupIndex()) ? "[+] " : "[-] ");
        graphics.text(font, marker.copy().append(entry.title()), leftPos + PAD + 4, y + 2, ACCENT, false);
    }

    private void extractRow(GuiGraphicsExtractor graphics, Row row, int y, int mouseX, int mouseY) {
        int left = leftPos + PAD;
        int right = tableRight();
        graphics.fill(left, y, right, y + ROW_HEIGHT - 1, CELL);
        graphics.outline(left, y, right - left, ROW_HEIGHT - 1, CELL_EDGE);
        if (!row.seedIcon().isEmpty()) {
            graphics.item(row.seedIcon(), left + 4, y + 1);
        }
        boolean allGrowing = row.growing() == row.seeds();
        graphics.text(font, row.growing() + " / " + row.seeds(), leftPos + COLUMN_GROWING, y + 6, allGrowing ? TEXT : WARN, false);
        graphics.item(row.module1(), leftPos + COLUMN_MODULES, y + 1);
        graphics.itemDecorations(font, row.module1(), leftPos + COLUMN_MODULES, y + 1);
        graphics.item(row.module2(), leftPos + COLUMN_MODULES + 18, y + 1);
        graphics.itemDecorations(font, row.module2(), leftPos + COLUMN_MODULES + 18, y + 1);
        graphics.text(font, row.upgraded() + "/" + row.growing(), leftPos + COLUMN_UPGRADED, y + 6, DIM, false);
        Component status = Component.translatable("modular_planters.status." + row.status().name().toLowerCase(Locale.ROOT));
        graphics.text(font, fit(status, STATUS_WIDTH), leftPos + COLUMN_STATUS, y + 2, statusColor(row), false);
        BlockPos pos = row.pos();
        graphics.text(font, pos.getX() + ", " + pos.getY() + ", " + pos.getZ(), leftPos + COLUMN_STATUS, y + 11, DIM, false);
        extractButton(graphics, "[o]", outlineButtonLeft(), y, PlanterOutlines.isOn(pos), mouseX, mouseY);
        extractButton(graphics, "[open]", openButtonLeft(), y, false, mouseX, mouseY);
        extractRowTooltip(graphics, row, y, status, mouseX, mouseY);
    }

    private void extractButton(GuiGraphicsExtractor graphics, String label, int x, int y, boolean on, int mouseX, int mouseY) {
        int width = font.width(label) + 6;
        boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y + 3 && mouseY < y + 15;
        int color = on || hovered ? ACCENT : TEXT;
        if (on) {
            graphics.fill(x, y + 3, x + width, y + 15, CELL_EDGE);
        }
        graphics.outline(x, y + 3, width, 12, hovered ? ACCENT : PANEL_EDGE);
        graphics.text(font, label, x + 3, y + 5, color, false);
    }

    private void extractRowTooltip(GuiGraphicsExtractor graphics, Row row, int y, Component status, int mouseX, int mouseY) {
        if (mouseX < 0) {
            return;
        }
        if (overButton(mouseX, mouseY, y, outlineButtonLeft(), "[o]")) {
            graphics.setTooltipForNextFrame(font, Component.translatable("modular_planters.screen.core.highlight"), mouseX, mouseY);
        } else if (overButton(mouseX, mouseY, y, openButtonLeft(), "[open]")) {
            graphics.setTooltipForNextFrame(font, Component.translatable("modular_planters.screen.core.open"), mouseX, mouseY);
        } else if (mouseY >= y && mouseY < y + ROW_HEIGHT - 1) {
            ItemStack module = moduleUnder(row, mouseX, y, mouseY);
            if (!module.isEmpty()) {
                graphics.setTooltipForNextFrame(font, module, mouseX, mouseY);
            } else if (mouseX >= leftPos + COLUMN_STATUS && mouseX < leftPos + COLUMN_STATUS + STATUS_WIDTH
                    && mouseY < y + 11 && font.width(status) > STATUS_WIDTH) {
                graphics.setTooltipForNextFrame(font, status, mouseX, mouseY);
            }
        }
    }

    private ItemStack moduleUnder(Row row, int mouseX, int y, int mouseY) {
        if (mouseY < y + 1 || mouseY >= y + 17) {
            return ItemStack.EMPTY;
        }
        if (mouseX >= leftPos + COLUMN_MODULES && mouseX < leftPos + COLUMN_MODULES + 16) {
            return row.module1();
        }
        if (mouseX >= leftPos + COLUMN_MODULES + 18 && mouseX < leftPos + COLUMN_MODULES + 34) {
            return row.module2();
        }
        return ItemStack.EMPTY;
    }

    private void extractScrollbar(GuiGraphicsExtractor graphics) {
        int max = maxScroll();
        if (max == 0) {
            return;
        }
        int viewport = TABLE_BOTTOM - TABLE_TOP;
        int barHeight = Math.max(12, viewport * viewport / (viewport + max));
        int barTop = topPos + TABLE_TOP + (viewport - barHeight) * scroll / max;
        int x = leftPos + WIDTH - PAD - SCROLLBAR_WIDTH;
        graphics.fill(x, topPos + TABLE_TOP, x + SCROLLBAR_WIDTH, topPos + TABLE_BOTTOM, CELL);
        graphics.fill(x, barTop, x + SCROLLBAR_WIDTH, barTop + barHeight, PANEL_EDGE);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && inTable((int) event.x(), (int) event.y()) && clickTable((int) event.x(), (int) event.y())) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean clickTable(int mouseX, int mouseY) {
        int y = topPos + TABLE_TOP - scroll;
        for (Entry entry : entries) {
            if (mouseY >= y && mouseY < y + entry.height()) {
                if (entry.heading()) {
                    if (!collapsed.remove(entry.groupIndex())) {
                        collapsed.add(entry.groupIndex());
                    }
                    rebuildEntries();
                    return true;
                }
                BlockPos pos = entry.row().pos();
                if (overButton(mouseX, mouseY, y, outlineButtonLeft(), "[o]")) {
                    PlanterOutlines.toggle(pos);
                    return true;
                }
                if (overButton(mouseX, mouseY, y, openButtonLeft(), "[open]")) {
                    ClientPacketDistributor.sendToServer(new CoreRequestPayload(CoreRequestPayload.Action.OPEN, Optional.of(pos)));
                    return true;
                }
                return false;
            }
            y += entry.height();
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (inTable((int) mouseX, (int) mouseY)) {
            scroll = Math.clamp(scroll - (int) (scrollY * ROW_HEIGHT), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean inTable(int mouseX, int mouseY) {
        return mouseX >= leftPos + PAD && mouseX < leftPos + WIDTH - PAD
                && mouseY >= topPos + TABLE_TOP && mouseY < topPos + TABLE_BOTTOM;
    }

    private int maxScroll() {
        int content = entries.stream().mapToInt(Entry::height).sum();
        return Math.max(0, content - (TABLE_BOTTOM - TABLE_TOP));
    }

    private int tableRight() {
        return leftPos + WIDTH - PAD - (maxScroll() > 0 ? SCROLLBAR_WIDTH + 2 : 0);
    }

    private int openButtonLeft() {
        return tableRight() - 4 - (font.width("[open]") + 6);
    }

    private int outlineButtonLeft() {
        return openButtonLeft() - 3 - (font.width("[o]") + 6);
    }

    private boolean overButton(int mouseX, int mouseY, int y, int x, String label) {
        return mouseX >= x && mouseX < x + font.width(label) + 6 && mouseY >= y + 3 && mouseY < y + 15;
    }

    private void rebuildEntries() {
        entries.clear();
        if (builtFrom == null) {
            return;
        }
        List<List<Row>> groups = new ArrayList<>();
        for (Row row : builtFrom.rows()) {
            List<Row> group = groups.stream().filter(candidate -> sameSeed(candidate.getFirst(), row)).findFirst().orElse(null);
            if (group == null) {
                group = new ArrayList<>();
                groups.add(group);
            }
            group.add(row);
        }
        for (int i = 0; i < groups.size(); i++) {
            int index = i;
            List<Row> group = groups.get(i);
            ItemStack seed = group.getFirst().seedIcon();
            Component name = seed.isEmpty() ? Component.translatable("modular_planters.screen.core.empty") : seed.getHoverName();
            entries.add(new Entry(true, index, Component.translatable("modular_planters.screen.core.group", name, group.size()), null));
            if (!collapsed.contains(i)) {
                group.forEach(row -> entries.add(new Entry(false, index, null, row)));
            }
        }
        scroll = Math.clamp(scroll, 0, maxScroll());
    }

    private static boolean sameSeed(Row left, Row right) {
        ItemStack a = left.seedIcon();
        ItemStack b = right.seedIcon();
        return a.isEmpty() && b.isEmpty() || !a.isEmpty() && !b.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
    }

    private static int statusColor(Row row) {
        return switch (row.status()) {
            case GROWING -> ACCENT;
            case IDLE -> DIM;
            case SHORT_ON_SOIL -> WARN;
            case PAUSED_FOR_POWER, PAUSED_EXPORT_FULL -> BAD;
        };
    }

    private String fit(Component text, int width) {
        Font f = font;
        String full = text.getString();
        return f.width(full) <= width ? full : f.plainSubstrByWidth(full, width - f.width("...")) + "...";
    }

    private static String count(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private static String compact(long value) {
        if (value >= 1_000_000) {
            return trim(value / 1_000_000.0) + "M";
        }
        if (value >= 1_000) {
            return trim(value / 1_000.0) + "K";
        }
        return Long.toString(value);
    }

    private static String trim(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}
