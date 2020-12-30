package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.item.ItemStack;

public interface IMixinPlayerInventory extends IMixinInventory {

    int canHold(ItemStack itemstack);

}