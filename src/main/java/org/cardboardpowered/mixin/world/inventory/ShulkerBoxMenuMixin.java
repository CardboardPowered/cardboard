package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxMenu.class)
public class ShulkerBoxMenuMixin extends AbstractContainerMenuMixin {

    @Shadow
    public Container container;

    private CraftInventoryView bukkitEntity;
    private Inventory inventory;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At("TAIL"))
    public void setPlayerInv(int i, Inventory playerinventory, Container iinventory, CallbackInfo ci) {
        this.inventory = (Inventory) playerinventory;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        bukkitEntity = new CraftInventoryView((org.bukkit.entity.Player)((ServerPlayerBridge)this.inventory.player).getBukkitEntity(), new CraftInventory(this.container), (ShulkerBoxMenu)(Object)this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}
