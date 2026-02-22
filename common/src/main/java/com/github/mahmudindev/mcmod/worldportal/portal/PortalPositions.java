package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class PortalPositions extends SavedData {
    public static String FIELD = WorldPortal.MOD_ID + "_positions";

    private final Map<BlockPos, ResourceKey<Block>> blocks = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag blocks = new ListTag();
        this.blocks.forEach((k, v) -> {
            CompoundTag compoundTagX = new CompoundTag();
            compoundTagX.putInt("PosX", k.getX());
            compoundTagX.putInt("PosY", k.getY());
            compoundTagX.putInt("PosZ", k.getZ());
            compoundTagX.putString("Block", String.valueOf(v.location()));

            blocks.add(compoundTagX);
        });
        compoundTag.put("Blocks", blocks);

        return compoundTag;
    }

    public Map<BlockPos, ResourceKey<Block>> getBlocks() {
        return Map.copyOf(this.blocks);
    }

    public ResourceKey<Block> getBlock(BlockPos pos) {
        return this.blocks.get(pos);
    }

    public void putBlock(BlockPos pos, ResourceKey<Block> block) {
        this.blocks.put(pos, block);
        this.setDirty();
    }

    public void removeBlock(BlockPos pos) {
        this.blocks.remove(pos);
        this.setDirty();
    }

    public static SavedData.Factory<PortalPositions> factory() {
        return new SavedData.Factory<>(
                PortalPositions::new,
                (compoundTag, provider) -> load(compoundTag),
                null
        );
    }

    public static PortalPositions load(CompoundTag compoundTag) {
        PortalPositions portalPositions = new PortalPositions();

        ListTag blocks = compoundTag.getList("Blocks", 10);
        for(int i = 0; i < blocks.size(); ++i) {
            CompoundTag compoundTagX = blocks.getCompound(i);

            portalPositions.blocks.put(
                    new BlockPos(
                            compoundTagX.getInt("PosX"),
                            compoundTagX.getInt("PosY"),
                            compoundTagX.getInt("PosZ")
                    ),
                    ResourceKey.create(
                            Registries.BLOCK,
                            ResourceLocation.parse(compoundTagX.getString("Block"))
                    )
            );
        }

        return portalPositions;
    }
}
