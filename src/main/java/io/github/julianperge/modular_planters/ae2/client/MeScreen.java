package io.github.julianperge.modular_planters.ae2.client;

import io.github.julianperge.modular_planters.ae2.MeMenu;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MeScreen extends AbstractContainerScreen<MeMenu> {
  private static final Identifier BACKGROUND =
      Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
  private static final int ROWS = 2;
  private static final int PANEL_GRAY = 0xFFC6C6C6;
  private static final int TEXT_GRAY = 0xFF404040;

  public MeScreen(MeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 114 + ROWS * 18);
    this.inventoryLabelY = imageHeight - 94;
  }

  // The generic chest background draws a grid of slot frames, so every frame except the filter
  // slot's is painted over with the panel color.
  @Override
  public void extractBackground(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractBackground(graphics, mouseX, mouseY, partialTick);
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        BACKGROUND,
        leftPos,
        topPos,
        0.0F,
        0.0F,
        imageWidth,
        ROWS * 18 + 17,
        256,
        256);
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        BACKGROUND,
        leftPos,
        topPos + ROWS * 18 + 17,
        0.0F,
        126.0F,
        imageWidth,
        96,
        256,
        256);
    graphics.fill(leftPos + 25, topPos + 17, leftPos + 169, topPos + 35, PANEL_GRAY);
    graphics.fill(leftPos + 7, topPos + 35, leftPos + 169, topPos + 17 + ROWS * 18, PANEL_GRAY);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    super.extractLabels(graphics, mouseX, mouseY);
    graphics.text(
        font,
        Component.translatable("modular_planters.screen.me.filter"),
        30,
        23,
        TEXT_GRAY,
        false);
    Component status =
        Component.translatable(
            "modular_planters.me_status." + menu.status().name().toLowerCase(Locale.ROOT));
    graphics.text(
        font,
        Component.translatable("modular_planters.screen.me.status", status),
        8,
        40,
        TEXT_GRAY,
        false);
  }
}
