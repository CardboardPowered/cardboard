package com.javazilla.bukkitfabric.mixin.screen;

import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.inventory.CardboardMerchantInventory;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;

@Mixin(MerchantScreenHandler.class)
public class MixinMerchantScreenHandler extends MixinScreenHandler {

    @Shadow public Merchant merchant;
    @Shadow public MerchantInventory merchantInventory;

    private CardboardInventoryView bukkitEntity = null;
    private PlayerInventory player;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, Merchant imerchant, CallbackInfo ci) {
        this.player = playerinventory;
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity == null)
            bukkitEntity = new CardboardInventoryView((Player)((IMixinEntity)this.player.player).getBukkitEntity(), new CardboardMerchantInventory(merchant, merchantInventory), (MerchantScreenHandler)(Object)this);
        return bukkitEntity;
    }

}
