package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> findDimensionEntryPointOverworldKey(
            ResourceKey<Level> original
    ) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> findDimensionEntryPointEndKey(ResourceKey<Level> original) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return null;
        }

        return original;
    }
}
