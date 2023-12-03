package io.github.jumperonjava.zereph_totems;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ZerephTotems implements ModInitializer {
    public static String MODID = "zereph_totem";

    @Override
    public void onInitialize() {
        new TotemItems();
        new TotemEffects();
        new TotemEvents();
        new PacketManager.Server();

    }
}
