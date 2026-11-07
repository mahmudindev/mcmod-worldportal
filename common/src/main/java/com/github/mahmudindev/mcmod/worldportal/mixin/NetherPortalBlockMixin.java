package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
    @WrapOperation(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isValidSpawn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z"
            )
    )
    private boolean randomTickIsValidSpawn(
            BlockState instance,
            BlockGetter blockGetter,
            BlockPos blockPos,
            EntityType<?> entityType,
            Operation<Boolean> original,
            BlockState blockState,
            ServerLevel serverLevel
    ) {
        PortalData portalData = ((IServerLevel) serverLevel).worldportal$getPortalData();
        if (portalData.getBlock(blockPos.above()) != null) {
            return false;
        }

        return original.call(instance, blockGetter, blockPos, entityType);
    }

    @Inject(method = "updateShape", at = @At("TAIL"))
    private void updateShapePortal(
            BlockState blockState,
            Direction direction,
            BlockState blockState2,
            LevelAccessor levelAccessor,
            BlockPos blockPos,
            BlockPos blockPos2,
            CallbackInfoReturnable<BlockState> cir
    ) {
        BlockState blockStateX = cir.getReturnValue();

        if (blockStateX.is((NetherPortalBlock)(Object) this)) {
            return;
        }

        PortalData portalData = ((IServerLevel) levelAccessor).worldportal$getPortalData();
        portalData.removeBlock(blockPos);
    }

    @ModifyArg(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;findClosestPortalPosition(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;"
            ),
            index = 0
    )
    private BlockPos getExitPortalForcerFindPrepare(
            BlockPos blockPos,
            @Local(argsOnly = true) Entity entity,
            @Local(argsOnly = true, ordinal = 0) BlockPos blockPos2
    ) {
        IEntity entityX = (IEntity) entity;

        PortalConfig portalConfig = entityX.worldportal$getPortalConfig();
        if (portalConfig != null) {
            ((IBlockPos) blockPos).worldportal$setLevel(entity.level());
            ((IBlockPos) blockPos).worldportal$setPortalEntrancePos(blockPos2);
            ((IBlockPos) blockPos).worldportal$setPortal(entityX.worldportal$getPortalId());
        }

        return blockPos;
    }

    @ModifyArg(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"
            ),
            index = 0
    )
    private BlockPos getExitPortalForcerCreatePrepare(
            BlockPos blockPos,
            @Local(argsOnly = true) Entity entity,
            @Local(argsOnly = true, ordinal = 0) BlockPos blockPos2
    ) {
        IEntity entityX = (IEntity) entity;

        PortalConfig portalConfig = entityX.worldportal$getPortalConfig();
        if (portalConfig != null) {
            ((IBlockPos) blockPos).worldportal$setLevel(entity.level());
            ((IBlockPos) blockPos).worldportal$setPortalEntrancePos(blockPos2);
            ((IBlockPos) blockPos).worldportal$setPortal(entityX.worldportal$getPortalId());
        }

        return blockPos;
    }
}
