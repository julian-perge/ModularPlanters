package io.github.julianperge.modular_planters.logic;

public enum UpgradeKind {
    NONE, SPEED_MK1, SPEED_MK2, SPEED_MK3, YIELD_MK1, YIELD_MK2, YIELD_MK3;

    public static UpgradeKind fromItemId(String id) {
        return switch (id) {
            case "agritechevolved:sm_mk1" -> SPEED_MK1;
            case "agritechevolved:sm_mk2" -> SPEED_MK2;
            case "agritechevolved:sm_mk3" -> SPEED_MK3;
            case "agritechevolved:ym_mk1" -> YIELD_MK1;
            case "agritechevolved:ym_mk2" -> YIELD_MK2;
            case "agritechevolved:ym_mk3" -> YIELD_MK3;
            default -> NONE;
        };
    }

    public float speed(AteNumbers n) {
        return switch (this) {
            case SPEED_MK1 -> (float) n.speedMk1();
            case SPEED_MK2 -> (float) n.speedMk2();
            case SPEED_MK3 -> (float) n.speedMk3();
            default -> 1.0f;
        };
    }

    public float speedPenalty(AteNumbers n) {
        return switch (this) {
            case YIELD_MK1 -> (float) n.yieldPenaltyMk1();
            case YIELD_MK2 -> (float) n.yieldPenaltyMk2();
            case YIELD_MK3 -> (float) n.yieldPenaltyMk3();
            default -> 1.0f;
        };
    }

    public float power(AteNumbers n) {
        return switch (this) {
            case SPEED_MK1 -> (float) n.powerMk1();
            case SPEED_MK2 -> (float) n.powerMk2();
            case SPEED_MK3 -> (float) n.powerMk3();
            default -> 1.0f;
        };
    }

    public float yield(AteNumbers n) {
        return switch (this) {
            case YIELD_MK1 -> (float) n.yieldMk1();
            case YIELD_MK2 -> (float) n.yieldMk2();
            case YIELD_MK3 -> (float) n.yieldMk3();
            default -> 1.0f;
        };
    }
}
