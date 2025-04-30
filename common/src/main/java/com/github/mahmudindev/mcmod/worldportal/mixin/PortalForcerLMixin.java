package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.base.IPortalForcer;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(value = PortalForcer.class, priority = 500)
public abstract class PortalForcerLMixin implements IPortalForcer {
    @Shadow @Final private ServerLevel level;
    @Unique
    private final Map<BlockPos, BlockPos> virtualPosMap = new HashMap<>();
    @Unique
    private final Map<BlockPos, ResourceLocation> portalMap = new HashMap<>();

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
        BlockPos blockPosX = this.virtualPosMap.get(blockPos);
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
        BlockPos blockPosX = this.virtualPosMap.get(blockPos);
        if (blockPosX != null) {
            virtualPosRef.set(blockPos);

            return original.call(blockPosX, axis);
        }

        return original.call(blockPos, axis);
    }

    @Override
    public PortalData worldportal$getPortal(BlockPos virtualPos) {
        return PortalManager.getPortal(this.portalMap.get(virtualPos));
    }

    @Override
    public BlockPos worldportal$setPortal(BlockPos pos, ResourceLocation id) {
        BlockPos posX = pos.offset(
                0,
                -42069 - this.level.getRandom().nextInt(Math.abs(pos.getY())),
                0
        );

        this.virtualPosMap.put(posX, pos);
        this.portalMap.put(posX, id);

        return posX;
    }

    @Override
    public void worldportal$removePortal(BlockPos virtualPos) {
        this.virtualPosMap.remove(virtualPos);
        this.portalMap.remove(virtualPos);
    }
}
