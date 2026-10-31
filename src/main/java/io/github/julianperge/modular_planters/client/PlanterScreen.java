package io.github.julianperge.modular_planters.client;

import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.menu.PlanterMenu;
import io.github.julianperge.modular_planters.menu.SeedBinSlot;
import io.github.julianperge.modular_planters.network.CoreRequestPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class PlanterScreen extends AbstractContainerScreen<PlanterMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int ROWS = 3;
    private static final int SLOT_COLUMNS = 9;
    private static final int[] USED_COLUMNS = {1, 3, 5, 6};
    private static final int PANEL_GRAY = 0xFFC6C6C6;
    private static final int TEXT_GRAY = 0xFF404040;
    private static final int SHOWN_COMBOS = 3;

    public PlanterScreen(PlanterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 114 + ROWS * 18);
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.fromCore()) {
            addRenderableWidget(Button.builder(Component.translatable("modular_planters.screen.back_to_core"),
                            button -> ClientPacketDistributor.sendToServer(new CoreRequestPayload(CoreRequestPayload.Action.BACK_TO_CORE, Optional.empty())))
                    .bounds(leftPos + imageWidth - 70, topPos + 3, 64, 12).build());
        }
    }

    // The generic chest background draws a grid of slot frames, so the frames that hold no slot are painted over with the panel color.
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, ROWS * 18 + 17, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos + ROWS * 18 + 17, 0.0F, 126.0F, imageWidth, 96, 256, 256);
        for (int column = 0; column < SLOT_COLUMNS; column++) {
            if (!isUsed(column)) {
                graphics.fill(leftPos + 7 + column * 18, topPos + 17, leftPos + 25 + column * 18, topPos + 35, PANEL_GRAY);
            }
        }
        graphics.fill(leftPos + 7, topPos + 35, leftPos + 169, topPos + 17 + ROWS * 18, PANEL_GRAY);
    }

    private static boolean isUsed(int column) {
        for (int used : USED_COLUMNS) {
            if (used == column) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        PlanterStatus status = menu.status();
        int y = 40;
        graphics.text(font, Component.translatable("modular_planters.status." + status.name().toLowerCase(Locale.ROOT)),
                8, y, TEXT_GRAY, false);
        if (status == PlanterStatus.SHORT_ON_SOIL) {
            y += 11;
            graphics.text(font, Component.translatable("modular_planters.screen.working_idle", menu.growing(),
                    menu.seedCount() - menu.growing()), 8, y, TEXT_GRAY, false);
        }
        if (menu.growing() > 0) {
            y += 11;
            graphics.text(font, Component.translatable("modular_planters.screen.upgraded", menu.upgraded(), menu.growing()),
                    8, y, TEXT_GRAY, false);
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        if (!(hoveredSlot instanceof SeedBinSlot seedSlot)) {
            return super.getTooltipFromContainerItem(stack);
        }
        // Trait mods such as Productive Farming upgrade each seed on its own, so a full bin can hold dozens of combinations, and only the most common ones fit on screen.
        List<ItemStack> combos = new ArrayList<>(seedSlot.combos());
        combos.sort(Comparator.comparingInt(ItemStack::getCount).reversed());
        List<Component> lines = new ArrayList<>();
        for (ItemStack combo : combos.subList(0, Math.min(SHOWN_COMBOS, combos.size()))) {
            List<Component> tooltip = super.getTooltipFromContainerItem(combo);
            lines.add(Component.translatable("modular_planters.screen.combo", combo.getCount(), combo.getHoverName()));
            lines.addAll(tooltip.subList(Math.min(1, tooltip.size()), tooltip.size()));
        }
        if (combos.size() > SHOWN_COMBOS) {
            lines.add(Component.translatable("modular_planters.screen.more_combos", combos.size() - SHOWN_COMBOS));
        }
        return lines;
    }
}
