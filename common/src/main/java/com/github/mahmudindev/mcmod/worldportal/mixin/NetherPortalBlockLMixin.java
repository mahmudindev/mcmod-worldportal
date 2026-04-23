package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.BlockUtil;
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
            ResourceKey<Level> dimension,
            Operation<ServerLevel> original,
            ServerLevel currentLevel,
            Entity entity,
            BlockPos portalEntryPos
    ) {
        return original.call(instance, ((IEntity) entity).worldportal$setupPortal(
                portalEntryPos,
                dimension
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
                    target = "Lnet/minecraft/util/BlockUtil;getLargestRectangleAround(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;ILnet/minecraft/core/Direction$Axis;ILjava/util/function/Predicate;)Lnet/minecraft/util/BlockUtil$FoundRectangle;"
            )
    )
    private BlockUtil.FoundRectangle getExitPortalPrepare(
            BlockPos center,
            Direction.Axis axis1,
            int i,
            Direction.Axis axis2,
            int j,
            Predicate<BlockPos> test,
            Operation<BlockUtil.FoundRectangle> original,
            ServerLevel newLevel
    ) {
        BlockState blockState = newLevel.getBlockState(center);
        boolean hasHA = blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);

        return original.call(
                center,
                axis1,
                i,
                hasHA ? axis2 : Direction.Axis.Z,
                j,
                test
        );
    }

    @WrapOperation(
            method = "getDimensionTransitionFromExit",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/phys/Vec3"
            )
    )
    private static Vec3 getDimensionTransitionFromExitHorizontalPortal(
            double x,
            double y,
            double z,
            Operation<Vec3> original,
            Entity entity,
            BlockPos portalEntryPos
    ) {
        if (((IEntity) entity).worldportal$getPortalConfig() != null) {
            Level level = entity.level();
            BlockState state = level.getBlockState(portalEntryPos);

            return entity.getRelativePortalPosition(
                    Direction.Axis.X,
                    BlockUtil.getLargestRectangleAround(
                            portalEntryPos,
                            Direction.Axis.X,
                            21,
                            Direction.Axis.Z,
                            21,
                            posX -> level.getBlockState(posX) == state
                    )
            );
        }

        return original.call(x, y, z);
    }
}
