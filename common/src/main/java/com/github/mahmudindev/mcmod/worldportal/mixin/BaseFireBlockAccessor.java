package com.github.mahmudindev.mcmod.worldportal.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseFireBlock.class)
public interface BaseFireBlockAccessor {
    @Invoker
    static boolean invokeInPortalDimension(Level level) {
        throw new AssertionError();
    }
}
