package org.spigotmc;

import net.minecraft.entity.Entity;

public class TrackingRange {

    /**
     * @param defaultRange Default range defined by Mojang
     */
    public static int getEntityTrackingRange(Entity entity, int defaultRange) {
        return defaultRange;
    }

    public static TrackingRangeType getTrackingRangeType(Entity entity) {
        return TrackingRangeType.OTHER;
    }

    public static enum TrackingRangeType {
        PLAYER, ANIMAL, MONSTER, MISC, OTHER, ENDERDRAGON;
    }

}