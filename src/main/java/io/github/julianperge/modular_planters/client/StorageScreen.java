package io.github.julianperge.modular_planters.client;

import io.github.julianperge.modular_planters.menu.StorageMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class StorageScreen extends AbstractContainerScreen<StorageMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    private final int rows;

    public StorageScreen(StorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 114 + menu.rows() * 18);
        this.rows = menu.rows();
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0.0F, 0.0F, imageWidth, rows * 18 + 17, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos + rows * 18 + 17, 0.0F, 126.0F, imageWidth, 96, 256, 256);
    }
}
