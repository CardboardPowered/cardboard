package org.cardboardpowered.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;

@Mixin(Generic3x3ContainerScreenHandler.class)
public class MixinGeneric3x3ContainerScreenHandler extends MixinScreenHandler {

    @Shadow
    public Inventory inventory;

    private CardboardInventoryView bukkitEntity = null;
    private PlayerInventory playerInv;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, Inventory iinventory, CallbackInfo ci) {
        this.playerInv = playerinventory;
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftInventory inventory = new CraftInventory(this.inventory);
        bukkitEntity = new CardboardInventoryView((Player)((IMixinEntity)this.playerInv.player).getBukkitEntity(), inventory, (Generic3x3ContainerScreenHandler)(Object)this);
        return bukkitEntity;
    }


}