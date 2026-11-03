package io.github.julianperge.modular_planters.client;

import io.github.julianperge.modular_planters.ModularPlanters;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = ModularPlanters.MOD_ID, value = Dist.CLIENT)
public final class PlanterOutlines {
  private static final GizmoStyle STYLE = GizmoStyle.stroke(0xFF8FF0A4, 3.0F);
  private static final Set<BlockPos> ON = new HashSet<>();

  private PlanterOutlines() {}

  public static void toggle(BlockPos pos) {
    if (!ON.remove(pos)) {
      ON.add(pos.immutable());
    }
  }

  public static boolean isOn(BlockPos pos) {
    return ON.contains(pos);
  }

  public static void clear() {
    ON.clear();
  }

  // Gizmos added before the translucent pass are collected for this frame and drawn through walls,
  // so no extra render state is needed.
  @SubscribeEvent
  public static void draw(RenderLevelStageEvent.AfterOpaqueFeatures event) {
    for (BlockPos pos : ON) {
      Gizmos.cuboid(pos, STYLE).setAlwaysOnTop();
    }
  }

  // A dimension change or a respawn replaces the local player, and positions from the old world
  // mean nothing in the new one.
  @SubscribeEvent
  public static void onPlayerReplaced(ClientPlayerNetworkEvent.Clone event) {
    clear();
  }

  @SubscribeEvent
  public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
    clear();
  }
}
