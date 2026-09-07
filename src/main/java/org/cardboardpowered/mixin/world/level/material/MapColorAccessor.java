package org.cardboardpowered.mixin.world.level.material;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.material.MapColor;

@Mixin(MapColor.class)
public interface MapColorAccessor {

    /**
     * The base colour table, indexed by colour id. Private in vanilla, but the map canvas
     * needs it to tell a valid packed colour id from an unused one instead of hardcoding
     * a range that goes stale every time Mojang adds a colour.
     */
    @Accessor("MATERIAL_COLORS")
    static MapColor[] cardboard$getMaterialColors() {
        throw new AssertionError();
    }

}
