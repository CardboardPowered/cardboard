package com.javazilla.bukkitfabric.interfaces;

import java.util.Map;

import net.minecraft.loot.LootTable;
import net.minecraft.util.Identifier;

public interface IMixinLootManager {

    public Map<LootTable, Identifier> getLootTableToKeyMapBF();

}