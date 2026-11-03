package io.github.julianperge.modular_planters.multiblock;

import net.neoforged.neoforge.transfer.item.ItemResource;

// An ME call can open its own root transaction, as AE2's storage bus does, so callers must never
// make one while a transaction is open; every method returns at most the amount asked for, and a
// simulated call changes nothing.
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
