package com.github.mahmudindev.mcmod.worldportal.fabric.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Entity.class, priority = 750)
public abstract class EntityLMixin implements IEntity {
    @Shadow protected BlockPos portalEntrancePos;

    @Shadow public abstract Level level();

    @Shadow protected abstract Vec3 getRelativePortalPosition(
            Direction.Axis axis,
            BlockUtil.FoundRectangle foundRectangle
    );

    @ModifyExpressionValue(
            method = "changeDimension",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> changeDimensionEndPlatform(ResourceKey<Level> original) {
        if (this.worldportal$getPortal() != null) {
            return null;
        }

        return original;
    }

    @WrapOperation(
            method = "method_30331",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/phys/Vec3"
            )
    )
    private Vec3 method30331HorizontalPortal(
            double d,
            double e,
            double f,
            Operation<Vec3> original,
            @Local BlockState blockState
    ) {
        if (this.worldportal$getPortal() != null) {
            return this.getRelativePortalPosition(
                    Direction.Axis.X,
                    BlockUtil.getLargestRectangleAround(
                            this.portalEntrancePos,
                            Direction.Axis.X,
                            21,
                            Direction.Axis.Z,
                            21,
                            blockPos -> this.level().getBlockState(blockPos) == blockState
                    )
            );
        }

        return original.call(d, e, f);
    }
}
