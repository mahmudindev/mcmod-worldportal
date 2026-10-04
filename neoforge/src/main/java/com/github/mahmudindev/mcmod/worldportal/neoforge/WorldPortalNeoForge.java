package com.github.mahmudindev.mcmod.worldportal.neoforge;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(WorldPortal.MOD_ID)
public final class WorldPortalNeoForge {
    public static IEventBus EVENT_BUS;

    public WorldPortalNeoForge(IEventBus eventBus) {
        EVENT_BUS = eventBus;

        // Run our common setup.
        WorldPortal.init();
    }
}
