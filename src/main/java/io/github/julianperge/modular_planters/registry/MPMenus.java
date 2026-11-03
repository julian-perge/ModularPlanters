package io.github.julianperge.modular_planters.registry;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.menu.CoreMenu;
import io.github.julianperge.modular_planters.menu.PlanterMenu;
import io.github.julianperge.modular_planters.menu.StorageMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPMenus {
  public static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(Registries.MENU, ModularPlanters.MOD_ID);

  public static final DeferredHolder<MenuType<?>, MenuType<StorageMenu>> FERTILIZER =
      MENUS.register(
          "fertilizer",
          () ->
              IMenuTypeExtension.create(
                  (id, inventory, buffer) -> StorageMenu.fertilizerOnClient(id, inventory)));
  public static final DeferredHolder<MenuType<?>, MenuType<StorageMenu>> EXPORT =
      MENUS.register(
          "export",
          () ->
              IMenuTypeExtension.create(
                  (id, inventory, buffer) -> StorageMenu.exportOnClient(id, inventory)));

  public static final DeferredHolder<MenuType<?>, MenuType<PlanterMenu>> PLANTER =
      MENUS.register(
          "planter",
          () ->
              IMenuTypeExtension.create(
                  (id, inventory, buffer) ->
                      PlanterMenu.onClient(
                          id,
                          inventory,
                          buffer.readBlockPos(),
                          buffer.readBlockPos(),
                          buffer.readBoolean())));

  public static final DeferredHolder<MenuType<?>, MenuType<CoreMenu>> CORE =
      MENUS.register(
          "core",
          () ->
              IMenuTypeExtension.create(
                  (id, inventory, buffer) ->
                      CoreMenu.onClient(id, inventory, buffer.readBlockPos())));

  private MPMenus() {}
}
