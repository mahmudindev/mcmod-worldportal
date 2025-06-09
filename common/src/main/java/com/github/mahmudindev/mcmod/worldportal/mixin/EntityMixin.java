package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
    @Unique
    private ResourceLocation portalId;

    @Inject(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
                    shift = At.Shift.AFTER
            )
    )
    private void handleNetherPortalChangeDimensionAfter(CallbackInfo ci) {
        this.portalId = null;
    }

    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;findPortalAround(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockUtil.FoundRectangle> getExitPortalForcerFindPrepare(
            PortalForcer instance,
            BlockPos blockPos,
            boolean isNether,
            WorldBorder worldBorder,
            Operation<Optional<BlockUtil.FoundRectangle>> original
    ) {
        PortalData portal = this.worldportal$getPortal();
        if (portal != null) {
            ((IBlockPos) blockPos).worldportal$setPortal(this.worldportal$getPortalId());
        }

        return original.call(instance, blockPos, isNether, worldBorder);
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
