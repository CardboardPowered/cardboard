package com.javazilla.bukkitfabric.mixin.network;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

@Mixin(PacketByteBuf.class)
public class MixinPacketByteBuf {

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public ItemStack readItemStack() {
        if (!this.readBoolean()) return ItemStack.EMPTY;

        ItemStack itemstack = new ItemStack(Item.byRawId(this.readVarInt()), this.readByte());
        itemstack.setTag(this.readCompoundTag());
        if (itemstack.getTag() != null) CraftItemStack.setItemMeta(itemstack, CraftItemStack.getItemMeta(itemstack));
        return itemstack;
    }

    @Shadow
    public int readVarInt() {
        return 0;
    }

    @Shadow
    public byte readByte() {
        return 0;
    }

    @Shadow
    public CompoundTag readCompoundTag() {
        return null;
    }

    @Shadow
    public boolean readBoolean() {
        return false;
    }

}