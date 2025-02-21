package com.github.mahmudindev.minecraft.worldportal.mixin;

import com.github.mahmudindev.minecraft.worldportal.WorldPortal;
import com.github.mahmudindev.minecraft.worldportal.base.IServerLevel;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(value = PortalForcer.class, priority = 500)
public abstract class PortalForcerLMixin {
    @Shadow @Final private ServerLevel level;

    @WrapMethod(method = "findPortalAround")
    private Optional<BlockUtil.FoundRectangle> findPortalAroundRestore(
            BlockPos blockPos,
            boolean isNether,
            WorldBorder worldBorder,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            @Share(
                    namespace = WorldPortal.MOD_ID + "_PortalForcer",
                    value = "virtualPos"
            ) LocalRef<BlockPos> virtualPosRef
    ) {
        BlockPos blockPosX = ((IServerLevel) this.level).worldportal$getPortalInfoPos(blockPos);
        if (blockPosX != null) {
            virtualPosRef.set(blockPos);

            return original.call(blockPosX, isNether, worldBorder);
        }

        return original.call(blockPos, isNether, worldBorder);
    }

    @WrapMethod(method = "createPortal")
    private Optional<BlockUtil.FoundRectangle> createPortalRestore(
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            @Share(
                    namespace = WorldPortal.MOD_ID + "_PortalForcer",
                    value = "virtualPos"
            ) LocalRef<BlockPos> virtualPosRef
    ) {
        BlockPos blockPosX = ((IServerLevel) this.level).worldportal$getPortalInfoPos(blockPos);
        if (blockPosX != null) {
            virtualPosRef.set(blockPos);

            return original.call(blockPosX, axis);
        }

        return original.call(blockPos, axis);
    }
}
