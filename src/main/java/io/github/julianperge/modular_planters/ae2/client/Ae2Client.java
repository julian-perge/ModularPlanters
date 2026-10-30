package io.github.julianperge.modular_planters.ae2.client;

import io.github.julianperge.modular_planters.ae2.Ae2Integration;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class Ae2Client {
    private Ae2Client() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Ae2Integration.ME_MENU.get(), MeScreen::new);
    }
}
