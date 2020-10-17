package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.item.ItemStack;

public interface IMixinPlayerInventory extends IMixinInventory {

    public int canHold(ItemStack itemstack);

}