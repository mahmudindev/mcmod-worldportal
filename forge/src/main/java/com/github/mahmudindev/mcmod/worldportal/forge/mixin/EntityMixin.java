package com.github.mahmudindev.mcmod.worldportal.forge.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract Level level();

    @WrapOperation(
            method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/util/ITeleporter;getPortalInfo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Function;)Lnet/minecraft/world/level/portal/PortalInfo;"
            ),
            remap = false
    )
    private PortalInfo changeDimensionFindDimensionEntryPointFinish(
            ITeleporter instance,
            Entity entity,
            ServerLevel serverLevel,
            Function<ServerLevel, PortalInfo> defaultPortalInfo,
            Operation<PortalInfo> original
    ) {
        PortalInfo portalInfo = original.call(
                instance,
                entity,
                serverLevel,
                defaultPortalInfo
        );

        if (portalInfo != null) {
            PortalData portal = ((IEntity) this).worldportal$getPortal();
            if (portal == null) {
                return portalInfo;
            }

            ResourceKey<Level> dimension = serverLevel.dimension();
            if (dimension != portal.getDestinationKey()) {
                return portalInfo;
            }

            BlockPos blockPos = BlockPos.containing(portalInfo.pos);

            BlockState blockState = serverLevel.getBlockState(blockPos);
            if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                return portalInfo;
            }

            Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            BlockUtil.FoundRectangle portalRectangle = BlockUtil.getLargestRectangleAround(
                    blockPos,
                    axis,
                    21,
                    Direction.Axis.Y,
                    21,
                    blockPosX -> serverLevel.getBlockState(blockPosX) == blockState
            );

            IServerLevel serverLevelX = (IServerLevel) serverLevel;

            PortalReturns portalReturns = serverLevelX.worldportal$getPortalReturns();
            portalReturns.putDimension(
                    portalRectangle.minCorner,
                    this.level().dimension()
            );
        }

        return portalInfo;
    }

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
            if (((IEntity) this).worldportal$getPortal() != null) {
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
}
