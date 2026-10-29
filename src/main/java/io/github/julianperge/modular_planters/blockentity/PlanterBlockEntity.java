package io.github.julianperge.modular_planters.blockentity;

import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.integration.PlanterPostHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterPreHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterProcessingTimeEvent;
import com.misterd.agritechevolved.recipe.DropEntry;
import com.mojang.serialization.Codec;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.block.PlanterBlock;
import io.github.julianperge.modular_planters.logic.AteNumbers;
import io.github.julianperge.modular_planters.logic.ClocheYield;
import io.github.julianperge.modular_planters.logic.Coverage;
import io.github.julianperge.modular_planters.logic.Group;
import io.github.julianperge.modular_planters.logic.Grouping;
import io.github.julianperge.modular_planters.logic.GrowthMath;
import io.github.julianperge.modular_planters.logic.ModuleKind;
import io.github.julianperge.modular_planters.logic.PlanterTicker;
import io.github.julianperge.modular_planters.logic.RemainderLedger;
import io.github.julianperge.modular_planters.logic.SeedBin;
import io.github.julianperge.modular_planters.multiblock.OutputSink;
import io.github.julianperge.modular_planters.multiblock.TickContext;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public class PlanterBlockEntity extends BlockEntity {
    public static final int SOIL = 0, MODULE_1 = 1, MODULE_2 = 2;

    private static final Codec<Map<String, Long>> REMAINDERS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);

    private final SeedBin<ItemStack> seeds = new SeedBin<>(ItemStack::isSameItemSameComponents, s -> s.copyWithCount(1));
    private final ItemStacksResourceHandler soilAndModules = new ItemStacksResourceHandler(3) {
        @Override
        protected int getCapacity(int index, ItemResource resource) {
            return Math.min(SeedBin.CAPACITY, super.getCapacity(index, resource));
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            ItemStack current = getResource(index).toStack(getAmountAsInt(index));
            if (index == SOIL && !previousContents.isEmpty() && !current.isEmpty()
                    && !ItemStack.isSameItem(previousContents, current)) {
                seeds.resetAll();
            }
            setChanged();
        }
    };
    private final RemainderLedger<String> clocheRemainders = new RemainderLedger<>();
    private final List<ItemStack> heldOutputs = new ArrayList<>();
    private PlanterStatus status = PlanterStatus.IDLE;
    private long lastDraw;
    private int growing;
    private int upgraded;

    public PlanterBlockEntity(BlockPos pos, BlockState state) {
        super(MPBlockEntities.PLANTER.get(), pos, state);
    }

    public SeedBin<ItemStack> seeds() {
        return seeds;
    }

    public ItemStacksResourceHandler soilAndModules() {
        return soilAndModules;
    }

    public RemainderLedger<String> clocheRemainders() {
        return clocheRemainders;
    }

    public List<ItemStack> heldOutputs() {
        return Collections.unmodifiableList(heldOutputs);
    }

    public PlanterStatus status() {
        return status;
    }

    public long lastDraw() {
        return lastDraw;
    }

    public int growing() {
        return growing;
    }

    public int upgraded() {
        return upgraded;
    }

    public void tickFromCore(TickContext ctx) {
        lastDraw = 0;
        if (!heldOutputs.isEmpty()) {
            flushHeldOutputs(ctx.outputs());
            setChanged();
            if (!heldOutputs.isEmpty()) {
                setStatus(PlanterStatus.PAUSED_EXPORT_FULL);
                return;
            }
        }

        int soilCount = soilAndModules.getAmountAsInt(SOIL);
        ItemStack soil = soilAndModules.getResource(SOIL).toStack(soilCount);
        Optional<AteBridge.PlantRecipe> recipe = seeds.size() == 0 || soil.isEmpty()
                ? Optional.empty() : AteBridge.recipe(ctx.level(), seeds.seed(0));
        if (recipe.isEmpty() || !recipe.get().soilMatches().test(soil)) {
            seeds.resetAll();
            growing = 0;
            upgraded = 0;
            setStatus(PlanterStatus.IDLE);
            setChanged();
            return;
        }

        growing = Coverage.growing(seeds.size(), soilCount);
        seeds.resetFrom(growing);
        ModuleKind kind1 = moduleKind(MODULE_1);
        ModuleKind kind2 = moduleKind(MODULE_2);
        int count1 = moduleCount(MODULE_1, kind1);
        int count2 = moduleCount(MODULE_2, kind2);
        upgraded = Coverage.upgraded(growing, count1, count2);
        List<Group> groups = Grouping.of(seeds, Coverage.assign(growing, kind1, count1, kind2, count2));

        AteNumbers numbers = ctx.numbers();
        float soilModifier = AteBridge.soilModifier(soil);
        PlanterStatus paused = null;
        PlanterTicker.AdvanceResult result = PlanterTicker.advance(seeds, groups,
                group -> processingTime(ctx, group, soilModifier),
                modules -> GrowthMath.powerPerSeedTick(numbers, modules),
                ctx.power().stored());
        if (result.pausedForPower()) {
            paused = PlanterStatus.PAUSED_FOR_POWER;
        } else {
            // This take can't fail, because advance was given ctx.power().stored() and only charges what that covers.
            ctx.power().tryTake(result.energyUsed());
            lastDraw = result.energyUsed();
        }

        for (Group group : groups) {
            if (!seeds.ready(group.first())) {
                continue;
            }
            FertilizerData fertilizer = fertilizerFor(ctx, group);
            float fertilizerYield = fertilizer != null ? fertilizer.yieldMultiplier() : 1.0f;
            float moduleYield = group.modules().yieldModifier(numbers);
            float modifier = fertilizerYield * moduleYield * (float) numbers.clocheYield();
            if (!ctx.outputs().hasRoomFor(listedDrops(recipe.get().drops(), modifier, group.size()))) {
                paused = PlanterStatus.PAUSED_EXPORT_FULL;
                break;
            }
            for (int seed : group.seeds()) {
                harvest(ctx, seed, fertilizer != null, fertilizerYield, moduleYield, modifier);
            }
            // Outputs that did not fit are held until they leave, so later groups wait instead of adding more to the pile.
            if (!heldOutputs.isEmpty()) {
                break;
            }
        }

        if (!heldOutputs.isEmpty()) {
            setStatus(PlanterStatus.PAUSED_EXPORT_FULL);
        } else if (paused != null) {
            setStatus(paused);
        } else if (growing < seeds.size()) {
            setStatus(PlanterStatus.SHORT_ON_SOIL);
        } else {
            setStatus(PlanterStatus.GROWING);
        }
        setChanged();
    }

    public void stopFromCore() {
        lastDraw = 0;
        growing = 0;
        upgraded = 0;
        setStatus(PlanterStatus.IDLE);
    }

    private ModuleKind moduleKind(int slot) {
        return AteBridge.moduleKind(soilAndModules.getResource(slot).toStack());
    }

    private int moduleCount(int slot, ModuleKind kind) {
        return kind == ModuleKind.NONE ? 0 : soilAndModules.getAmountAsInt(slot);
    }

    private static @Nullable FertilizerData fertilizerFor(TickContext ctx, Group group) {
        return ctx.fertilizer().available() >= group.size() ? ctx.fertilizer().data() : null;
    }

    private int processingTime(TickContext ctx, Group group, float soilModifier) {
        FertilizerData fertilizer = fertilizerFor(ctx, group);
        float fertilizerSpeed = fertilizer != null ? fertilizer.speedMultiplier() : 1.0f;
        int time = GrowthMath.growthTime(ctx.numbers(), soilModifier, group.modules(), fertilizerSpeed);
        return NeoForge.EVENT_BUS.post(new PlanterProcessingTimeEvent(this, seeds.seed(group.first()).copy(), time))
                .getProcessingTime();
    }

    // Each drop entry gets one more item per seed than ATE's rounding gives, because the cloche adds at most one extra item per drop and harvest.
    private static List<ItemStack> listedDrops(List<DropEntry> drops, float modifier, int seedCount) {
        List<ItemStack> listed = new ArrayList<>(drops.size());
        for (DropEntry entry : drops) {
            listed.add(new ItemStack(entry.item(), (ClocheYield.ateCount(entry.max(), modifier) + 1) * seedCount));
        }
        return listed;
    }

    private void harvest(TickContext ctx, int index, boolean fertilized, float fertilizerYield, float moduleYield,
                         float modifier) {
        ItemStack stored = seeds.seed(index);
        ItemStack seed = NeoForge.EVENT_BUS.post(new PlanterPreHarvestEvent(this, stored.copy())).getSeed();
        if (!seed.isEmpty() && !ItemStack.matches(seed, stored)) {
            seeds.replaceSeed(index, seed.copyWithCount(1));
        }

        List<ItemStack> rolled = AteBridge.recipe(ctx.level(), seed)
                .map(recipe -> AteBridge.rollDrops(recipe.drops(), ctx.random()))
                .orElse(List.of());
        List<ItemStack> drops = new ArrayList<>(rolled.size());
        List<ItemStack> extras = new ArrayList<>();
        for (ItemStack raw : rolled) {
            drops.add(raw.copyWithCount(ClocheYield.ateCount(raw.getCount(), modifier)));
            long kept = ClocheYield.keptMicros(raw.getCount(), fertilizerYield, moduleYield, ctx.numbers().clocheYield());
            int extra = clocheRemainders.addAndTakeWhole(BuiltInRegistries.ITEM.getKey(raw.getItem()).toString(), kept);
            if (extra > 0) {
                extras.add(raw.copyWithCount(extra));
            }
        }

        PlanterPostHarvestEvent post = NeoForge.EVENT_BUS.post(
                new PlanterPostHarvestEvent(this, seeds.seed(index).copy(), drops));
        for (ItemStack drop : post.getDrops()) {
            hold(ctx.outputs().insert(drop));
        }
        for (ItemStack extra : extras) {
            hold(ctx.outputs().insert(extra));
        }

        if (fertilized) {
            ctx.fertilizer().consume(1);
        }
        seeds.reset(index);
    }

    private void flushHeldOutputs(OutputSink outputs) {
        List<ItemStack> held = new ArrayList<>(heldOutputs);
        heldOutputs.clear();
        for (ItemStack stack : held) {
            hold(outputs.insert(stack));
        }
    }

    // Held stacks are split to their item's stack size, because the item stack codec that saves them rejects counts above 99.
    private void hold(ItemStack stack) {
        while (!stack.isEmpty()) {
            heldOutputs.add(stack.split(stack.getMaxStackSize()));
        }
    }

    private void setStatus(PlanterStatus next) {
        status = next;
        boolean active = next == PlanterStatus.GROWING || next == PlanterStatus.SHORT_ON_SOIL;
        BlockState state = getBlockState();
        if (level != null && state.getValue(PlanterBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(PlanterBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level == null) {
            return;
        }
        for (SeedBin.Combo<ItemStack> combo : seeds.combos()) {
            dropAll(pos, combo.seed().copyWithCount(combo.count()));
        }
        seeds.clear();
        for (int slot = 0; slot < soilAndModules.size(); slot++) {
            dropAll(pos, soilAndModules.getResource(slot).toStack(soilAndModules.getAmountAsInt(slot)));
            soilAndModules.set(slot, ItemResource.EMPTY, 0);
        }
        heldOutputs.forEach(stack -> dropAll(pos, stack));
        heldOutputs.clear();
    }

    private void dropAll(BlockPos pos, ItemStack stack) {
        while (!stack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.split(stack.getMaxStackSize()));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("status", status.name());
        ValueOutput.ValueOutputList bin = output.childrenList("seeds");
        for (int i = 0; i < seeds.size(); i++) {
            ValueOutput entry = bin.addChild();
            entry.store("seed", ItemStack.CODEC, seeds.seed(i));
            entry.putInt("progress", seeds.progress(i));
            entry.putBoolean("ready", seeds.ready(i));
        }
        soilAndModules.serialize(output.child("soil_and_modules"));
        output.store("cloche_remainders", REMAINDERS_CODEC, clocheRemainders.snapshot());
        ValueOutput.TypedOutputList<ItemStack> held = output.list("held_outputs", ItemStack.CODEC);
        heldOutputs.forEach(held::add);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String savedStatus = input.getStringOr("status", "");
        status = Arrays.stream(PlanterStatus.values()).filter(value -> value.name().equals(savedStatus)).findFirst()
                .orElse(PlanterStatus.IDLE);
        seeds.clear();
        input.childrenListOrEmpty("seeds").stream().limit(SeedBin.CAPACITY).forEach(entry -> entry.read("seed", ItemStack.CODEC)
                .ifPresent(seed -> seeds.restore(seed, Math.max(0, entry.getIntOr("progress", 0)),
                        entry.getBooleanOr("ready", false))));
        soilAndModules.deserialize(input.childOrEmpty("soil_and_modules"));
        clocheRemainders.load(input.read("cloche_remainders", REMAINDERS_CODEC).orElse(Map.of()));
        heldOutputs.clear();
        input.listOrEmpty("held_outputs", ItemStack.CODEC).stream().forEach(heldOutputs::add);
    }
}
