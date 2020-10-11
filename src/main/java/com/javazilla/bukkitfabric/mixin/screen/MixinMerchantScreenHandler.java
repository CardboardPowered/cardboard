package com.javazilla.bukkitfabric.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventoryMerchant;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Trader;
import net.minecraft.village.TraderInventory;

@Mixin(MerchantScreenHandler.class)
public class MixinMerchantScreenHandler extends MixinScreenHandler {

    @Shadow public Trader trader;
    @Shadow public TraderInventory traderInventory;

    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, Trader imerchant, CallbackInfo ci) {
        this.player = playerinventory;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity == null)
            bukkitEntity = new CraftInventoryView((Player)((IMixinEntity)this.player.player).getBukkitEntity(), new CraftInventoryMerchant(trader, traderInventory), (MerchantScreenHandler)(Object)this);
        return bukkitEntity;
    }

}
