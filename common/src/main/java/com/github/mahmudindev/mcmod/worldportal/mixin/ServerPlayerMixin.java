package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements IEntity {
    public ServerPlayerMixin(
            Level level,
            BlockPos blockPos,
            float f,
            GameProfile gameProfile
    ) {
        super(level, blockPos, f, gameProfile);
    }

    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockUtil.FoundRectangle> getExitPortalForcerCreatePrepare(
            PortalForcer instance,
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original
    ) {
        PortalData portal = this.worldportal$getPortal();
        if (portal != null) {
            ((IBlockPos) blockPos).worldportal$setLevel(this.level());
            ((IBlockPos) blockPos).worldportal$setPortalEntrancePos(this.portalEntrancePos);
            ((IBlockPos) blockPos).worldportal$setPortal(this.worldportal$getPortalId());
        }

        return original.call(instance, blockPos, axis);
    }
}
