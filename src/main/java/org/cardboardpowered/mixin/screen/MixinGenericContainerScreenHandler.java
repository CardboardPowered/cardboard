package org.cardboardpowered.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.cardboardpowered.impl.inventory.CardboardDoubleChestInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.bukkit.entity.Player;
import org.cardboardpowered.impl.inventory.CardboardPlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

@Mixin(GenericContainerScreenHandler.class)
public class MixinGenericContainerScreenHandler extends MixinScreenHandler {

    @Shadow
    public Inventory inventory;

    private CardboardInventoryView bukkitEntity = null;
    private PlayerInventory player;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(ScreenHandlerType<?> containers, int i, PlayerInventory playerinventory, Inventory inventory, int j, CallbackInfo ci) {
        this.player = (PlayerInventory) playerinventory;
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftInventory inventory;
        if (this.inventory instanceof PlayerInventory) {
            inventory = new CardboardPlayerInventory((PlayerInventory) this.inventory);
        } else if (this.inventory instanceof DoubleInventory) {
            inventory = new CardboardDoubleChestInventory((DoubleInventory) this.inventory);
        } else inventory = new CraftInventory(this.inventory);

        bukkitEntity = new CardboardInventoryView((Player)((IMixinServerEntityPlayer)this.player.player).getBukkitEntity(), inventory, (GenericContainerScreenHandler)(Object)this);
        return bukkitEntity;
    }

}