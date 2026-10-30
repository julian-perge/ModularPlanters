package io.github.julianperge.modular_planters.multiblock;

import net.neoforged.neoforge.transfer.item.ItemResource;

public interface MeLink {
    MeStatus status();

    long insertItem(ItemResource resource, long amount, boolean simulate);

    long extractItem(ItemResource resource, long amount, boolean simulate);

    long extractEnergy(long amount, boolean simulate);

    ItemResource fertilizerFilter();

    boolean isRemoved();

    default boolean online() {
        return status() == MeStatus.ONLINE;
    }
}
