package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.resources.ResourceLocation;

public interface IEntity {
    ResourceLocation worldportal$getPortalId();

    PortalData worldportal$getPortal();

    void worldportal$setPortal(ResourceLocation id);
}
