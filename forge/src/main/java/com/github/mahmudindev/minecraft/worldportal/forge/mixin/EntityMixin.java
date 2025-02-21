package com.github.mahmudindev.minecraft.worldportal.forge.mixin;

import com.github.mahmudindev.minecraft.worldportal.base.IEntity;
import com.github.mahmudindev.minecraft.worldportal.base.IServerLevel;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalData;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalManager;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalReturns;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
            PortalData portalData = ((IEntity) this).worldportal$getPortalEntranceData();
            if (portalData == null) {
                return portalInfo;
            }

            ResourceKey<Level> dimension = serverLevel.dimension();
            if (dimension != portalData.getDestinationKey()) {
                return portalInfo;
            }

            BlockPos blockPos = BlockPos.containing(portalInfo.pos);

            BlockState blockState = serverLevel.getBlockState(blockPos);
            if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                return portalInfo;
            }

            Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            BlockUtil.FoundRectangle portalRectangle = PortalManager.getPortalRectangle(
                    serverLevel,
                    blockPos,
                    blockState,
                    axis
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

    @ModifyExpressionValue(
            method = "lambda$changeDimension$16",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> changeDimensionEndKey(ResourceKey<Level> original) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return null;
        }

        return original;
    }
}
