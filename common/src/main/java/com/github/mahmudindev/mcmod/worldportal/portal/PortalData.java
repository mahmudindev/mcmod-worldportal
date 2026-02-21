package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class PortalData extends SavedData {
    public static String FIELD = WorldPortal.MOD_ID + "_portals";

    private final Map<BlockPos, ResourceKey<Block>> blocks = new HashMap<>();
    private final Map<BlockPos, ResourceKey<Level>> dimensions = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
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

        ListTag dimensions = new ListTag();
        this.dimensions.forEach((k, v) -> {
            CompoundTag compoundTagX = new CompoundTag();
            compoundTagX.putInt("PosX", k.getX());
            compoundTagX.putInt("PosY", k.getY());
            compoundTagX.putInt("PosZ", k.getZ());
            compoundTagX.putString("Dimension", String.valueOf(v.location()));

            dimensions.add(compoundTagX);
        });
        compoundTag.put("Dimensions", dimensions);

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

    public Map<BlockPos, ResourceKey<Level>> getDimensions() {
        return Map.copyOf(this.dimensions);
    }

    public ResourceKey<Level> getDimension(BlockPos pos) {
        return this.dimensions.get(pos);
    }

    public void putDimension(BlockPos pos, ResourceKey<Level> dimension) {
        this.dimensions.put(pos, dimension);
        this.setDirty();
    }

    public void removeDimension(BlockPos pos) {
        this.dimensions.remove(pos);
        this.setDirty();
    }

    public static PortalData load(CompoundTag compoundTag) {
        PortalData portalData = new PortalData();

        ListTag blocks = compoundTag.getList("Blocks", 10);
        for(int i = 0; i < blocks.size(); ++i) {
            CompoundTag compoundTagX = blocks.getCompound(i);

            portalData.blocks.put(
                    new BlockPos(
                            compoundTagX.getInt("PosX"),
                            compoundTagX.getInt("PosY"),
                            compoundTagX.getInt("PosZ")
                    ),
                    ResourceKey.create(
                            Registries.BLOCK,
                            new ResourceLocation(compoundTagX.getString("Block"))
                    )
            );
        }

        ListTag dimensions = compoundTag.getList("Dimensions", 10);
        for(int i = 0; i < dimensions.size(); ++i) {
            CompoundTag compoundTagX = dimensions.getCompound(i);
            portalData.dimensions.put(
                    new BlockPos(
                            compoundTagX.getInt("PosX"),
                            compoundTagX.getInt("PosY"),
                            compoundTagX.getInt("PosZ")
                    ),
                    ResourceKey.create(
                            Registries.DIMENSION,
                            new ResourceLocation(compoundTagX.getString("Dimension"))
                    )
            );
        }

        return portalData;
    }
}
