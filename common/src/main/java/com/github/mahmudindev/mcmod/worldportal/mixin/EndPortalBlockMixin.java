package com.github.mahmudindev.mcmod.worldportal.mixin;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {
    @Inject(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
            ),
            cancellable = true
    )
    private void entityInsidePrepare(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Entity entity,
            CallbackInfo ci
    ) {
        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();

            ci.cancel();
            return;
        }

        BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                blockPos,
                Direction.Axis.X,
                21,
                Direction.Axis.Z,
                21,
                blockPosX -> level.getBlockState(blockPosX) == blockState
        );

        for (int x = 0; x < foundRectangle.axis1Size; x++) {
            for (int z = 0; z < foundRectangle.axis2Size; z++) {
                BlockPos blockPosH = foundRectangle.minCorner.offset(x, 0, z);

                for (Map.Entry<BlockPos, Boolean> entry : Map.of(
                        blockPosH.west(), x == 0,
                        blockPosH.east(), x == foundRectangle.axis1Size - 1,
                        blockPosH.north(), z == 0,
                        blockPosH.south(), z == foundRectangle.axis2Size - 1
                ).entrySet()) {
                    if (!entry.getValue()) {
                        continue;
                    }

                    if (!level.getBlockState(entry.getKey()).is(Blocks.OBSIDIAN)) {
                        return;
                    }
                }
            }
        }

        entity.setAsInsidePortal((Portal) Blocks.NETHER_PORTAL, blockPos);
        ci.cancel();
    }
}
