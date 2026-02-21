package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class PortalReturns extends SavedData {
    public static String FIELD = WorldPortal.MOD_ID + "_returns";

    private final Map<BlockPos, ResourceKey<Level>> dimensions = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
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

    public static PortalReturns load(CompoundTag compoundTag) {
        PortalReturns portalReturns = new PortalReturns();

        ListTag dimensions = compoundTag.getList("Dimensions", 10);
        for(int i = 0; i < dimensions.size(); ++i) {
            CompoundTag compoundTagX = dimensions.getCompound(i);
            portalReturns.dimensions.put(
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

        return portalReturns;
    }

    public static PortalReturns migrate(CompoundTag compoundTag, PortalData portalData) {
        PortalReturns portalReturns = new PortalReturns();

        ListTag dimensions = compoundTag.getList("Dimensions", 10);
        for(int i = 0; i < dimensions.size(); ++i) {
            CompoundTag compoundTagX = dimensions.getCompound(i);
            portalReturns.dimensions.put(
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

        portalReturns.dimensions.forEach(portalData::putDimension);
        portalReturns.dimensions.clear();
        portalReturns.setDirty();

        return portalReturns;
    }
}
