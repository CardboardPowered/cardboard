package com.fungus_soft.bukkitfabric.interfaces;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;

import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Palette;

public interface IMixinBlockEntity {

    public CraftPersistentDataContainer getPersistentDataContainer();

}