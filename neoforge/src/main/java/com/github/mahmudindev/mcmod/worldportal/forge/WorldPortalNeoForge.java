package com.github.mahmudindev.mcmod.worldportal.forge;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraftforge.fml.common.Mod;

@Mod(WorldPortal.MOD_ID)
public final class WorldPortalNeoForge {
    public WorldPortalNeoForge() {
        // Run our common setup.
        WorldPortal.init();
    }
}
