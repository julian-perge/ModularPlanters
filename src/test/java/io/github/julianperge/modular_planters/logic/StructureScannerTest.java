package io.github.julianperge.modular_planters.logic;

import static io.github.julianperge.modular_planters.logic.PartType.CORE;
import static io.github.julianperge.modular_planters.logic.PartType.EXPORT;
import static io.github.julianperge.modular_planters.logic.PartType.FERTILIZER;
import static io.github.julianperge.modular_planters.logic.PartType.FRAME;
import static io.github.julianperge.modular_planters.logic.PartType.ME;
import static io.github.julianperge.modular_planters.logic.PartType.PLANTER;
import static io.github.julianperge.modular_planters.logic.PartType.POWER;
import static io.github.julianperge.modular_planters.logic.PartType.UNLOADED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StructureScannerTest {
  record P(int x, int y, int z) {}

  static final int CAP = 24;
  static final P ORIGIN = new P(0, 0, 0);

  static List<P> faceNeighbors(P p) {
    return List.of(
        new P(p.x() + 1, p.y(), p.z()), new P(p.x() - 1, p.y(), p.z()),
        new P(p.x(), p.y() + 1, p.z()), new P(p.x(), p.y() - 1, p.z()),
        new P(p.x(), p.y(), p.z() + 1), new P(p.x(), p.y(), p.z() - 1));
  }

  // The core sits at the origin and the given parts follow it along +x, one block each.
  static Map<P, PartType> line(List<PartType> parts) {
    Map<P, PartType> world = new HashMap<>();
    world.put(ORIGIN, CORE);
    for (int i = 0; i < parts.size(); i++) {
      world.put(new P(i + 1, 0, 0), parts.get(i));
    }
    return world;
  }

  static Map<P, PartType> line(PartType... parts) {
    return line(List.of(parts));
  }

  static ScanResult<P> scan(Map<P, PartType> world) {
    return StructureScanner.scan(
        ORIGIN,
        StructureScannerTest::faceNeighbors,
        p -> world.getOrDefault(p, PartType.NOT_A_PART),
        CAP);
  }

  @Test
  void formsWithTheRequiredParts() {
    ScanResult<P> r = scan(line(POWER, EXPORT, PLANTER));
    assertTrue(r.formed());
    assertEquals(Problem.NONE, r.problem());
    assertNull(r.problemPos());
    assertEquals(3, r.partCount());
    assertEquals(List.of(new P(1, 0, 0)), r.parts().get(POWER));
    assertEquals(List.of(new P(2, 0, 0)), r.parts().get(EXPORT));
    assertEquals(List.of(new P(3, 0, 0)), r.parts().get(PLANTER));
  }

  @Test
  void diagonalContactDoesNotConnect() {
    Map<P, PartType> world = line(POWER, EXPORT);
    world.put(new P(-1, 1, 0), PLANTER);
    ScanResult<P> r = scan(world);
    assertEquals(Problem.MISSING_PLANTER, r.problem());
    assertEquals(2, r.partCount());
  }

  @Test
  void framesConnectAndCount() {
    ScanResult<P> r = scan(line(FRAME, FRAME, POWER, EXPORT, PLANTER));
    assertTrue(r.formed());
    assertEquals(5, r.partCount());
    assertEquals(2, r.parts().get(FRAME).size());
  }

  @Test
  void capCountsPartsButNotTheCore() {
    List<PartType> parts = new ArrayList<>(List.of(POWER, EXPORT, PLANTER));
    parts.addAll(Collections.nCopies(21, FRAME));
    Map<P, PartType> world = line(parts);
    ScanResult<P> atCap = scan(world);
    assertTrue(atCap.formed());
    assertEquals(24, atCap.partCount());

    P twentyFifth = new P(25, 0, 0);
    world.put(twentyFifth, FRAME);
    ScanResult<P> overCap = scan(world);
    assertEquals(Problem.TOO_MANY_PARTS, overCap.problem());
    assertEquals(twentyFifth, overCap.problemPos());
  }

  @Test
  void aSecondCoreStopsForming() {
    Map<P, PartType> world = line(FRAME, POWER, EXPORT, PLANTER);
    P secondCore = new P(1, 1, 0);
    world.put(secondCore, CORE);
    ScanResult<P> r = scan(world);
    assertEquals(Problem.SECOND_CORE, r.problem());
    assertEquals(secondCore, r.problemPos());
  }

  @Test
  void aSecondMeStopsForming() {
    Map<P, PartType> world = line(POWER, EXPORT, PLANTER, ME);
    P secondMe = new P(5, 0, 0);
    world.put(secondMe, ME);
    ScanResult<P> r = scan(world);
    assertEquals(Problem.SECOND_ME, r.problem());
    assertEquals(secondMe, r.problemPos());
  }

  @Test
  void missingPartsAreReportedInOrder() {
    ScanResult<P> noPower = scan(line(PLANTER));
    assertEquals(Problem.MISSING_POWER, noPower.problem());
    assertNull(noPower.problemPos());

    ScanResult<P> noExport = scan(line(PLANTER, POWER));
    assertEquals(Problem.MISSING_EXPORT, noExport.problem());
  }

  @Test
  void unloadedPartMakesTheScanWait() {
    Map<P, PartType> world = line(FRAME, POWER, EXPORT, PLANTER);
    P unloaded = new P(5, 0, 0);
    world.put(unloaded, UNLOADED);
    ScanResult<P> r = scan(world);
    assertEquals(Problem.WAITING_FOR_CHUNKS, r.problem());
    assertEquals(unloaded, r.problemPos());
  }

  @Test
  void singleMeStillForms() {
    ScanResult<P> r = scan(line(POWER, EXPORT, PLANTER, ME));
    assertTrue(r.formed());
    assertEquals(1, r.parts().get(ME).size());
  }

  @Test
  void singleFertilizerStillForms() {
    ScanResult<P> r = scan(line(POWER, EXPORT, PLANTER, FERTILIZER));
    assertTrue(r.formed());
    assertEquals(1, r.parts().get(FERTILIZER).size());
  }

  @Test
  void waitingForChunksBeatsMissingParts() {
    Map<P, PartType> world = line(PLANTER);
    P unloaded = new P(2, 0, 0);
    world.put(unloaded, UNLOADED);
    ScanResult<P> r = scan(world);
    assertEquals(Problem.WAITING_FOR_CHUNKS, r.problem());
    assertEquals(unloaded, r.problemPos());
  }

  @Test
  void partReachedByTwoPathsCountsOnce() {
    Map<P, PartType> world = new HashMap<>();
    world.put(ORIGIN, CORE);
    world.put(new P(1, 0, 0), POWER);
    world.put(new P(0, 1, 0), EXPORT);
    world.put(new P(1, 1, 0), PLANTER);
    ScanResult<P> r = scan(world);
    assertTrue(r.formed());
    assertEquals(3, r.partCount());
    assertEquals(1, r.parts().get(PLANTER).size());
  }

  @Test
  void tooManyPartsStopsCountingAtCapPlusOne() {
    List<PartType> parts = new ArrayList<>(List.of(POWER, EXPORT, PLANTER));
    parts.addAll(Collections.nCopies(40, FRAME));
    ScanResult<P> r = scan(line(parts));
    assertEquals(Problem.TOO_MANY_PARTS, r.problem());
    assertEquals(CAP + 1, r.partCount());
  }
}
