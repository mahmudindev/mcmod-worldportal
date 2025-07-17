package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Entity.class, priority = 750)
public abstract class EntityLMixin implements IEntity {
    @Shadow public abstract Level level();

    @WrapOperation(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
            )
    )
    private ServerLevel handleNetherPortalPrepare(
            MinecraftServer instance,
            ResourceKey<Level> resourceKey,
            Operation<ServerLevel> original
    ) {
        return original.call(instance, this.worldportal$setupPortal(resourceKey));
    }

    @WrapOperation(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;isNetherEnabled()Z"
            )
    )
    private boolean handleNetherPortalIsNetherEnabled(
            MinecraftServer instance,
            Operation<Boolean> original
    ) {
        PortalData portal = this.worldportal$getPortal();
        if (portal != null && portal.getDestinationKey() != Level.NETHER) {
            return true;
        }

        return original.call(instance);
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> findDimensionEntryPointEndKey(ResourceKey<Level> original) {
        if (this.worldportal$getPortal() != null) {
            return null;
        }

        return original;
    }

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
        if (this.worldportal$getPortal() != null) {
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;",
                    ordinal = 1
            )
    )
    private ResourceKey<Level> findDimensionEntryPointNetherKey1(
            ResourceKey<Level> original
    ) {
        if (this.worldportal$getPortal() != null) {
            return this.level().dimension();
        }

        return original;
    }
}
