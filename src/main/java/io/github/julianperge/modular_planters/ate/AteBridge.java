package io.github.julianperge.modular_planters.ate;

import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class AteBridge {
    private AteBridge() {
    }

    public static @Nullable FertilizerData fertilizer(ItemResource resource) {
        return resource.typeHolder().getData(ATEDataMaps.FERTILIZERS);
    }
}
