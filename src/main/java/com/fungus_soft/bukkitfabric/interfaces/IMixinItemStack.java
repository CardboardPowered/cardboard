package com.fungus_soft.bukkitfabric.interfaces;

import net.minecraft.item.Item;

public interface IMixinItemStack {

    public void setItem(Item item);

    public void convertStack(int version);

}