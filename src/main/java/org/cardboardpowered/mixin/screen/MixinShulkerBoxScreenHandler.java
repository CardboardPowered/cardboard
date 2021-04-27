package org.cardboardpowered.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;

@Mixin(ShulkerBoxScreenHandler.class)
public class MixinShulkerBoxScreenHandler extends MixinScreenHandler {

    @Shadow
    public Inventory inventory;

    private CardboardInventoryView bukkitEntity;
    private PlayerInventory player;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, Inventory iinventory, CallbackInfo ci) {
        this.player = (PlayerInventory) playerinventory;
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        bukkitEntity = new CardboardInventoryView((Player)((IMixinServerEntityPlayer)this.player.player).getBukkitEntity(), new CraftInventory(this.inventory), (ShulkerBoxScreenHandler)(Object)this);
        return bukkitEntity;
    }
}
