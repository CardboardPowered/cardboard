package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface IMixinBrewingStandBlockEntity {

    public DefaultedList<ItemStack> cardboard_getInventory();

}