package com.javazilla.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public class MixinItemStack implements IMixinItemStack {

    @Shadow
    private Item item;

    public MixinItemStack() {
    }

}