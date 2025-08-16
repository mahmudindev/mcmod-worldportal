package com.github.mahmudindev.mcmod.worldportal.forge.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(value = Entity.class, priority = 750)
public abstract class EntityLMixin implements IEntity {
    @Shadow protected BlockPos portalEntrancePos;

    @Shadow public abstract Level level();

    @Shadow protected abstract Vec3 getRelativePortalPosition(
            Direction.Axis axis,
            BlockUtil.FoundRectangle foundRectangle
    );

    @WrapOperation(
            method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/util/ITeleporter;placeEntity(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;FLjava/util/function/Function;)Lnet/minecraft/world/entity/Entity;"
            ),
            remap = false
    )
    private Entity changeDimensionEndPlatform(
            ITeleporter instance,
            Entity entity,
            ServerLevel currentWorld,
            ServerLevel destWorld,
            float yaw,
            Function<Boolean, Entity> repositionEntity,
            Operation<Entity> original
    ) {
        Function<Boolean, Entity> modifiedRepositionEntity = (spawnPortal) -> {
            if (this.worldportal$getPortal() != null) {
                return repositionEntity.apply(false);
            }

            return repositionEntity.apply(spawnPortal);
        };

        return original.call(
                instance,
                entity,
                currentWorld,
                destWorld,
                yaw,
                modifiedRepositionEntity
        );
    }

    @WrapOperation(
            method = "lambda$findDimensionEntryPoint$15",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/phys/Vec3"
            ),
            remap = false
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
