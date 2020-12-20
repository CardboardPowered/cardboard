package com.javazilla.bukkitfabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.item.FilledMapItem;

@Mixin(FilledMapItem.class)
public class MixinFilledMapItem {

    // TODO 1.17

    /**
     * @reason .
     * @author .
     *
    @Overwrite
    private static MapState createMapState(ItemStack itemstack, World world, int i, int j, int k, boolean flag, boolean flag1, RegistryKey<World> resourcekey) {
        int l = world.getNextMapId();
        MapState worldmap = new MapState("map_" + l);

        worldmap.init(i, j, k, flag, flag1, resourcekey);
        world.putMapState(worldmap);
        itemstack.getOrCreateTag().putInt("map", l);

        MapInitializeEvent event = new MapInitializeEvent(((IMixinMapState)worldmap).getMapViewBF());
        Bukkit.getServer().getPluginManager().callEvent(event);
        return worldmap;
    }*/

}
