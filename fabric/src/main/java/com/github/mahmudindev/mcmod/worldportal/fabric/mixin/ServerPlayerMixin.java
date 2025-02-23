package com.github.mahmudindev.mcmod.worldportal.fabric.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Shadow public abstract ServerLevel serverLevel();

    @WrapOperation(
            method = "changeDimension",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;findDimensionEntryPoint(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/level/portal/PortalInfo;"
            )
    )
    private PortalInfo changeDimensionFindDimensionEntryPointFinish(
            ServerPlayer instance,
            ServerLevel serverLevel,
            Operation<PortalInfo> original
    ) {
        PortalInfo portalInfo = original.call(instance, serverLevel);

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
                    this.serverLevel().dimension()
            );
        }

        return portalInfo;
    }

    @ModifyExpressionValue(
            method = "changeDimension",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;",
                    ordinal = 0
            )
    )
    private ResourceKey<Level> changeDimensionEndKey0(ResourceKey<Level> original) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "changeDimension",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;",
                    ordinal = 0
            )
    )
    private ResourceKey<Level> changeDimensionOverworldKey0(ResourceKey<Level> original) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "changeDimension",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;",
                    ordinal = 1
            )
    )
    private ResourceKey<Level> changeDimensionEndPlatform(ResourceKey<Level> original) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return null;
        }

        return original;
    }
}
