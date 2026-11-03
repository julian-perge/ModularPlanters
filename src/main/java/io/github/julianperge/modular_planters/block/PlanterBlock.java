package io.github.julianperge.modular_planters.block;

import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.logic.PartType;
import io.github.julianperge.modular_planters.menu.PlanterMenu;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class PlanterBlock extends PartBlock implements EntityBlock {
  public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

  public PlanterBlock(Properties properties) {
    super(properties, PartType.PLANTER);
    registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(ACTIVE);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!level.isClientSide()
        && player instanceof ServerPlayer serverPlayer
        && level.getBlockEntity(pos) instanceof PlanterBlockEntity planter) {
      PlanterMenu.open(serverPlayer, planter, pos, false);
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    if (level.isClientSide() || type != MPBlockEntities.PLANTER.get()) {
      return null;
    }
    return (BlockEntityTicker<T>)
        (BlockEntityTicker<PlanterBlockEntity>) PlanterBlockEntity::serverTick;
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new PlanterBlockEntity(pos, state);
  }
}
