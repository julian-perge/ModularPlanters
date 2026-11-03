package io.github.julianperge.modular_planters.block;

import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.logic.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class ExportBlock extends PartBlock implements EntityBlock {
  public ExportBlock(Properties properties) {
    super(properties, PartType.EXPORT);
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ExportBlockEntity(pos, state);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!level.isClientSide()
        && player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof ExportBlockEntity entity) {
      serverPlayer.openMenu(entity);
    }
    return InteractionResult.SUCCESS;
  }
}
