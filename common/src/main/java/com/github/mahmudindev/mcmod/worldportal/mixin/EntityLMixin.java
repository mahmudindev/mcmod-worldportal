package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
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

    @WrapOperation(
            method = "getRelativePortalPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalShape;getRelativePosition(Lnet/minecraft/BlockUtil$FoundRectangle;Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/EntityDimensions;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 getRelativePortalPositionModify(
            BlockUtil.FoundRectangle foundRectangle,
            Direction.Axis axis,
            Vec3 vec3,
            EntityDimensions entityDimensions,
            Operation<Vec3> original
    ) {
        Vec3 vec3X = original.call(foundRectangle, axis, vec3, entityDimensions);

        if (this.worldportal$getPortal() != null) {
            BlockPos blockPos = foundRectangle.minCorner;

            BlockState blockState = this.level().getBlockState(blockPos);
            if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                vec3X = vec3X.subtract(0.0, vec3X.y(), 0.0);
            }
        }

        return vec3X;
    }
}
