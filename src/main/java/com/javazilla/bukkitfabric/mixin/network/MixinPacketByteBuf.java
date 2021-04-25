package com.javazilla.bukkitfabric.mixin.network;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

@Mixin(PacketByteBuf.class)
public class MixinPacketByteBuf {

    /**
     * @reason Set org.bukkit.item.ItemStack metadata
     */
    @Redirect(at = @At(value = "INVOKE", target="Lnet/minecraft/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"), 
            method = { "readItemStack" })
    public void t(ItemStack stack, CompoundTag tag) {
        stack.setTag(tag);
        if (stack.getTag() != null) CraftItemStack.setItemMeta(stack, CraftItemStack.getItemMeta(stack));
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