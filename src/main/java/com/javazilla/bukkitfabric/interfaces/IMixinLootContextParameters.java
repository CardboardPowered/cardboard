package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.util.Identifier;

public interface IMixinLootContextParameters {

    LootContextParameter<Integer> LOOTING_MOD = new LootContextParameter<>(new Identifier("bukkit:looting_mod"));

}