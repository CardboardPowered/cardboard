package org.cardboardpowered.impl.util;

import org.bukkit.FluidCollisionMode;

import net.minecraft.world.RaycastContext.FluidHandling;

public class CardboardFluidRaytraceMode {

    public static FluidHandling toMc(FluidCollisionMode mode) {
        if (mode == null) return null;

        switch (mode) {
            case ALWAYS:
                return FluidHandling.ANY;
            case SOURCE_ONLY:
                return FluidHandling.SOURCE_ONLY;
            case NEVER:
                return FluidHandling.NONE;
            default:
                return null;
        }
    }

}
