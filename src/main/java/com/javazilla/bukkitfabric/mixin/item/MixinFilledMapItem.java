package com.javazilla.bukkitfabric.mixin.item;

import org.bukkit.Bukkit;
import org.bukkit.event.server.MapInitializeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import com.javazilla.bukkitfabric.interfaces.IMixinMapState;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

@Mixin(FilledMapItem.class)
public class MixinFilledMapItem {

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
    }

}
