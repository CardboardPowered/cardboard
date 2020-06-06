package com.fungus_soft.bukkitfabric.interfaces;

import org.bukkit.craftbukkit.CraftWorld;

public interface IMixinServerWorld extends IMixinBukkitGetter {

    public CraftWorld getCraftWorld();

}