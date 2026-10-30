package io.github.julianperge.modular_planters.menu;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.logic.ScanResult;
import io.github.julianperge.modular_planters.multiblock.Multiblock;
import io.github.julianperge.modular_planters.network.CoreRequestPayload;
import io.github.julianperge.modular_planters.network.CoreTablePayload;
import io.github.julianperge.modular_planters.registry.MPMenus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class CoreMenu extends AbstractContainerMenu {
    private static final int SEND_INTERVAL = 10;
    private static final double REACH = 4.0;

    private final @Nullable CoreBlockEntity core;
    private final BlockPos corePos;
    private final Player player;
    private int ticks;
    private @Nullable CoreTablePayload table;

    public CoreMenu(int id, Inventory inventory, CoreBlockEntity core) {
        this(id, inventory, core, core.getBlockPos());
    }

    private CoreMenu(int id, Inventory inventory, @Nullable CoreBlockEntity core, BlockPos corePos) {
        super(MPMenus.CORE.get(), id);
        this.core = core;
        this.corePos = corePos;
        this.player = inventory.player;
    }

    public static CoreMenu onClient(int id, Inventory inventory, BlockPos pos) {
        return new CoreMenu(id, inventory, null, pos);
    }

    public static void open(ServerPlayer player, CoreBlockEntity core) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new CoreMenu(id, inventory, core),
                Component.translatable("container.modular_planters.core")), core.getBlockPos());
    }

    public @Nullable CoreBlockEntity core() {
        return core;
    }

    public @Nullable CoreTablePayload table() {
        return table;
    }

    public void showTable(CoreTablePayload table) {
        this.table = table;
    }

    public static CoreTablePayload buildTable(CoreBlockEntity core, int containerId) {
        Level level = core.getLevel();
        Multiblock multiblock = core.multiblock();
        ScanResult<BlockPos> scan = core.lastScan();
        List<CoreTablePayload.Row> rows = new ArrayList<>();
        long draw = 0;
        if (level != null && multiblock != null) {
            for (BlockPos pos : multiblock.planters()) {
                if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof PlanterBlockEntity planter) {
                    rows.add(row(pos, planter));
                    draw += planter.lastDraw();
                }
            }
        }
        CoreTablePayload.Totals totals = new CoreTablePayload.Totals(scan.formed(), scan.partCount(), MPConfig.PART_CAP.get(),
                scan.problem(), Optional.ofNullable(scan.problemPos()),
                core.power() == null ? 0 : core.power().stored(),
                core.power() == null ? 0 : core.power().capacity(),
                draw,
                core.fertilizer() == null ? 0 : core.fertilizer().available(),
                core.outputs() == null ? 0 : core.outputs().fillPercent(),
                core.meStatus());
        return new CoreTablePayload(containerId, totals, rows);
    }

    private static CoreTablePayload.Row row(BlockPos pos, PlanterBlockEntity planter) {
        ItemStack seedIcon = planter.seeds().size() == 0 ? ItemStack.EMPTY : planter.seeds().seed(0).copyWithCount(1);
        return new CoreTablePayload.Row(pos, seedIcon, planter.seeds().size(), planter.growing(),
                moduleStack(planter, PlanterBlockEntity.MODULE_1), moduleStack(planter, PlanterBlockEntity.MODULE_2),
                planter.upgraded(), planter.status());
    }

    private static ItemStack moduleStack(PlanterBlockEntity planter, int slot) {
        return planter.soilAndModules().getResource(slot).toStack(planter.soilAndModules().getAmountAsInt(slot));
    }

    // The request comes from the client, so every field is checked again here against the open menu and the live structure.
    public static void handle(ServerPlayer player, CoreRequestPayload request) {
        switch (request.action()) {
            case OPEN -> request.planter().ifPresent(pos -> openPlanter(player, pos));
            case BACK_TO_CORE -> backToCore(player);
        }
    }

    private static void openPlanter(ServerPlayer player, BlockPos pos) {
        if (!(player.containerMenu instanceof CoreMenu menu) || menu.core == null || !menu.stillValid(player)) {
            return;
        }
        Multiblock multiblock = menu.core.multiblock();
        if (!menu.core.lastScan().formed() || multiblock == null || !multiblock.planters().contains(pos)) {
            return;
        }
        if (player.level().isLoaded(pos) && player.level().getBlockEntity(pos) instanceof PlanterBlockEntity planter) {
            PlanterMenu.open(player, planter, menu.corePos, true);
        }
    }

    private static void backToCore(ServerPlayer player) {
        if (!(player.containerMenu instanceof PlanterMenu menu) || !menu.fromCore()) {
            return;
        }
        BlockPos anchor = menu.anchor();
        if (player.level().isLoaded(anchor) && player.isWithinBlockInteractionRange(anchor, REACH)
                && player.level().getBlockEntity(anchor) instanceof CoreBlockEntity core) {
            open(player, core);
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        sendTable();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (++ticks % SEND_INTERVAL == 0) {
            sendTable();
        }
    }

    private void sendTable() {
        if (core != null && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, buildTable(core, containerId));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        return ItemStack.EMPTY;
    }

    // The client menu has no core block entity, so only the server checks that the core still exists and the player is in range.
    @Override
    public boolean stillValid(Player clicker) {
        return core == null || !core.isRemoved() && core.getLevel() == clicker.level()
                && clicker.isWithinBlockInteractionRange(corePos, REACH);
    }
}
