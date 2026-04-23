package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
            method = "getRelativePortalPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalShape;getRelativePosition(Lnet/minecraft/util/BlockUtil$FoundRectangle;Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/EntityDimensions;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 getRelativePortalPositionModify(
            BlockUtil.FoundRectangle largestRectangleAround,
            Direction.Axis axis,
            Vec3 position,
            EntityDimensions dimensions,
            Operation<Vec3> original
    ) {
        Vec3 relativePortalPosition = original.call(
                largestRectangleAround,
                axis,
                position,
                dimensions
        );

        if (this.worldportal$getPortalConfig() != null) {
            BlockPos pos = largestRectangleAround.minCorner;

            BlockState state = this.level().getBlockState(pos);
            if (!state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                relativePortalPosition = relativePortalPosition.subtract(
                        0.0,
                        relativePortalPosition.y(),
                        0.0
                );
            }
        }

        return relativePortalPosition;
    }
}
