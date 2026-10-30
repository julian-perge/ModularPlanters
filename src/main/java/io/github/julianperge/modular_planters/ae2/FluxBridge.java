package io.github.julianperge.modular_planters.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;

// This is the only class that imports Applied Flux, and callers reach it only after Ae2Integration.APPLIED_FLUX is true.
final class FluxBridge {
    private FluxBridge() {
    }

    static long extractFe(MEStorage storage, long amount, Actionable mode, IActionSource source) {
        return storage.extract(FluxKey.of(EnergyType.FE), amount, mode, source);
    }
}
