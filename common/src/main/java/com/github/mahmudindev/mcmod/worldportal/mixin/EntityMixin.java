package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
    @Unique
    private ResourceLocation portalId;

    @Shadow public abstract Level level();

    @WrapOperation(
            method = "handlePortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity handlePortalChangeDimensionFinish(
            Entity instance,
            DimensionTransition dimensionTransition,
            Operation<Entity> original
    ) {
        PortalData portal = this.worldportal$getPortal();
        if (portal != null) {
            ServerLevel serverLevel = dimensionTransition.newLevel();

            ResourceKey<Level> dimension = serverLevel.dimension();
            if (dimension != portal.getDestinationKey()) {
                return original.call(instance, dimensionTransition);
            }

            BlockPos blockPos = BlockPos.containing(dimensionTransition.pos());

            BlockState blockState = serverLevel.getBlockState(blockPos);
            if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                return original.call(instance, dimensionTransition);
            }

            Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
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
                    foundRectangle.minCorner,
                    this.level().dimension()
            );
        }

        return original.call(instance, dimensionTransition);
    }

    @Inject(method = "handlePortal", at = @At(value = "RETURN"))
    private void handlePortalChangeDimensionAfter(CallbackInfo ci) {
        this.portalId = null;
    }

    @Override
    public ResourceLocation worldportal$getPortalId() {
        return this.portalId;
    }

    @Override
    public PortalData worldportal$getPortal() {
        return PortalManager.getPortal(this.worldportal$getPortalId());
    }

    @Override
    public void worldportal$setPortal(ResourceLocation id) {
        this.portalId = id;
    }
}
