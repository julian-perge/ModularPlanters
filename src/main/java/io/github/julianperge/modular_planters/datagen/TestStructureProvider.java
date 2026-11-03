package io.github.julianperge.modular_planters.datagen;

import com.google.common.hash.Hashing;
import io.github.julianperge.modular_planters.gametest.MPTestRegistration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;

public class TestStructureProvider implements DataProvider {
  private final PackOutput.PathProvider structures;

  public TestStructureProvider(PackOutput output) {
    this.structures = output.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      NbtIo.writeCompressed(emptyStructure(9, 4, 9), bytes);
      byte[] data = bytes.toByteArray();
      cache.writeIfNeeded(
          structures.file(MPTestRegistration.STRUCTURE, "nbt"),
          data,
          Hashing.sha256().hashBytes(data));
      return CompletableFuture.completedFuture(null);
    } catch (IOException e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  private static CompoundTag emptyStructure(int x, int y, int z) {
    ListTag size = new ListTag();
    size.add(IntTag.valueOf(x));
    size.add(IntTag.valueOf(y));
    size.add(IntTag.valueOf(z));
    CompoundTag structure = new CompoundTag();
    structure.put("size", size);
    structure.put("palette", new ListTag());
    structure.put("blocks", new ListTag());
    structure.put("entities", new ListTag());
    return NbtUtils.addCurrentDataVersion(structure);
  }

  @Override
  public String getName() {
    return "Modular Planters test structures";
  }
}
