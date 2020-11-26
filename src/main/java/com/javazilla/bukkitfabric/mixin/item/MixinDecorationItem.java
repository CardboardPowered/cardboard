package com.javazilla.bukkitfabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemUsageContext;

@Mixin(DecorationItem.class)
public class MixinDecorationItem {

    public void useOnBlock_BF(ItemUsageContext itemactioncontext) {
        // TODO
    }

}