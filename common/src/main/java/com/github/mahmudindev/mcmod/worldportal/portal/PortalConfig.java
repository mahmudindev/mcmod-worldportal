package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PortalConfig {
    public static final ResourceLocation DEFAULT_MODE = ResourceLocation.tryBuild(
            WorldPortal.MOD_ID,
            "default"
    );
    public static final ResourceLocation HORIZONTAL_MODE = ResourceLocation.tryBuild(
            WorldPortal.MOD_ID,
            "horizontal"
    );

    @SerializedName("frame_top_right")
    private String frameTopRight;
    @SerializedName("frame_top_left")
    private String frameTopLeft;
    @SerializedName("frame_bottom_right")
    private String frameBottomRight;
    @SerializedName("frame_bottom_left")
    private String frameBottomLeft;
    private String mode;
    private String destination;

    public String getFrameTopRight() {
        return this.frameTopRight;
    }

    public ResourceLocation getFrameTopRightLocation() {
        String id = this.getFrameTopRight();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return ResourceLocation.parse(id);
    }

    public void setFrameTopRight(String frameTopRight) {
        this.frameTopRight = frameTopRight;
    }

    public String getFrameTopLeft() {
        return this.frameTopLeft;
    }

    public ResourceLocation getFrameTopLeftLocation() {
        String id = this.getFrameTopLeft();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return ResourceLocation.parse(id);
    }

    public void setFrameTopLeft(String frameTopLeft) {
        this.frameTopLeft = frameTopLeft;
    }

    public String getFrameBottomRight() {
        return this.frameBottomRight;
    }

    public ResourceLocation getFrameBottomRightLocation() {
        String id = this.getFrameBottomRight();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return ResourceLocation.parse(id);
    }

    public void setFrameBottomRight(String frameBottomRight) {
        this.frameBottomRight = frameBottomRight;
    }

    public String getFrameBottomLeft() {
        return this.frameBottomLeft;
    }

    public ResourceLocation getFrameBottomLeftLocation() {
        String id = this.getFrameBottomLeft();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return ResourceLocation.parse(id);
    }

    public void setFrameBottomLeft(String frameBottomLeft) {
        this.frameBottomLeft = frameBottomLeft;
    }

    public String getMode() {
        return this.mode;
    }

    public ResourceLocation getModeLocation() {
        String id = this.getMode();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return ResourceLocation.parse(id);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDestination() {
        return this.destination;
    }

    public ResourceLocation getDestinationLocation() {
        return ResourceLocation.parse(this.getDestination());
    }

    public ResourceKey<Level> getDestinationKey() {
        return ResourceKey.create(Registries.DIMENSION, this.getDestinationLocation());
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
