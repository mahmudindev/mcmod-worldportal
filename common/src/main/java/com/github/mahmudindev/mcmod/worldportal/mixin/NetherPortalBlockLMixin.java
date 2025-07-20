package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(value = NetherPortalBlock.class, priority = 750)
public abstract class NetherPortalBlockLMixin {
    @WrapOperation(
            method = "getPortalDestination",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
            )
    )
    private ServerLevel getPortalDestinationPrepare(
            MinecraftServer instance,
            ResourceKey<Level> resourceKey,
            Operation<ServerLevel> original,
            ServerLevel serverLevel,
            Entity entity,
            BlockPos blockPos
    ) {
        return original.call(instance, ((IEntity) entity).worldportal$setupPortal(
                blockPos,
                resourceKey
        ));
    }

    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"
            )
    )
    private Comparable<?> getExitPortalHasPropertyConditioned(
            BlockState instance,
            Property<?> property,
            Operation<Comparable<?>> original
    ) {
        return instance.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
                ? original.call(instance, property)
                : Direction.Axis.X;
    }

    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/BlockUtil;getLargestRectangleAround(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;ILnet/minecraft/core/Direction$Axis;ILjava/util/function/Predicate;)Lnet/minecraft/BlockUtil$FoundRectangle;"
            )
    )
    private BlockUtil.FoundRectangle getExitPortalPrepare(
            BlockPos blockPos,
            Direction.Axis axis,
            int i,
            Direction.Axis axis2,
            int j,
            Predicate<BlockPos> predicate,
            Operation<BlockUtil.FoundRectangle> original,
            ServerLevel serverLevel
    ) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        boolean hasHA = blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);

        return original.call(
                blockPos,
                axis,
                i,
                hasHA ? axis2 : Direction.Axis.Z,
                j,
                predicate
        );
    }

    @WrapOperation(
            method = "getDimensionTransitionFromExit",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/phys/Vec3"
            )
    )
    private static Vec3 method30331HorizontalPortal(
            double d,
            double e,
            double f,
            Operation<Vec3> original,
            Entity entity,
            BlockPos blockPos
    ) {
        if (((IEntity) entity).worldportal$getPortal() != null) {
            Level level = entity.level();
            BlockState blockState = level.getBlockState(blockPos);

            return entity.getRelativePortalPosition(
                    Direction.Axis.X,
                    BlockUtil.getLargestRectangleAround(
                            blockPos,
                            Direction.Axis.X,
                            21,
                            Direction.Axis.Z,
                            21,
                            blockPosX -> level.getBlockState(blockPosX) == blockState
                    )
            );
        }

        return original.call(d, e, f);
    }
}
