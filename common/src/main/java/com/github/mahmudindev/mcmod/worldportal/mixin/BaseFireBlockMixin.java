package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @WrapOperation(
            method = "onPlace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/BaseFireBlock;inPortalDimension(Lnet/minecraft/world/level/Level;)Z"
            )
    )
    private boolean onPlaceInPortalDimension(Level level, Operation<Boolean> original) {
        Boolean inPortalDimension = original.call(level);

        if (!inPortalDimension) {
            ResourceKey<Level> dimension = level.dimension();

            Map<ResourceLocation, PortalData> portals = PortalManager.getPortals();
            for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
                if (inPortalDimension) {
                    break;
                }

                PortalData portal = entry.getValue();

                if (portal.getDestinationKey() != dimension) {
                    inPortalDimension = true;
                }
            }
        }

        return inPortalDimension;
    }
}
