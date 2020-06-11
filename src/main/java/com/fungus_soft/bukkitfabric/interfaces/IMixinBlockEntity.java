package com.fungus_soft.bukkitfabric.interfaces;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;

public interface IMixinBlockEntity {

    public CraftPersistentDataContainer getPersistentDataContainer();

}