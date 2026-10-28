package io.github.julianperge.modular_planters.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class StructureScanner {
    private static final Set<PartType> RECORDED =
            EnumSet.complementOf(EnumSet.of(PartType.NOT_A_PART, PartType.UNLOADED, PartType.CORE));

    private StructureScanner() {
    }

    public static <P> ScanResult<P> scan(P core, Function<P, List<P>> neighbors, Function<P, PartType> typeAt, int cap) {
        Map<PartType, List<P>> parts = new EnumMap<>(PartType.class);
        for (PartType type : RECORDED) {
            parts.put(type, new ArrayList<>());
        }
        Set<P> seen = new HashSet<>();
        seen.add(core);
        Deque<P> queue = new ArrayDeque<>();
        queue.add(core);
        int count = 0;
        while (!queue.isEmpty()) {
            for (P next : neighbors.apply(queue.remove())) {
                if (!seen.add(next)) {
                    continue;
                }
                PartType type = typeAt.apply(next);
                if (type == PartType.NOT_A_PART) {
                    continue;
                }
                if (type == PartType.UNLOADED) {
                    return new ScanResult<>(Problem.WAITING_FOR_CHUNKS, next, parts);
                }
                if (type == PartType.CORE) {
                    return new ScanResult<>(Problem.SECOND_CORE, next, parts);
                }
                parts.get(type).add(next);
                count++;
                if (count > cap) {
                    return new ScanResult<>(Problem.TOO_MANY_PARTS, next, parts);
                }
                if (type == PartType.ME && parts.get(PartType.ME).size() > 1) {
                    return new ScanResult<>(Problem.SECOND_ME, next, parts);
                }
                queue.add(next);
            }
        }
        if (parts.get(PartType.POWER).isEmpty()) {
            return new ScanResult<>(Problem.MISSING_POWER, null, parts);
        }
        if (parts.get(PartType.EXPORT).isEmpty()) {
            return new ScanResult<>(Problem.MISSING_EXPORT, null, parts);
        }
        if (parts.get(PartType.PLANTER).isEmpty()) {
            return new ScanResult<>(Problem.MISSING_PLANTER, null, parts);
        }
        return new ScanResult<>(Problem.NONE, null, parts);
    }
}
