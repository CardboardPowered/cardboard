package org.cardboardpowered.mixin.world.level.saveddata.maps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {

    /**
     * Marks the given pixel dirty for every player holding the map, so the next update
     * packet actually covers it. Private in vanilla, but Bukkit's MapCanvas needs it.
     */
    @Invoker("setColorsDirty")
    void cardboard$setColorsDirty(int x, int y);

}
