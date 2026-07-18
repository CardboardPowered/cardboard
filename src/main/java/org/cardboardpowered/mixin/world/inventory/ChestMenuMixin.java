package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestMenu.class)
public class ChestMenuMixin extends AbstractContainerMenuMixin {

    @Shadow
    public Container container;

    private CraftInventoryView bukkitEntity = null;
    private Inventory inventory;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;I)V", at = @At("TAIL"))
    public void setPlayerInv(MenuType<?> containers, int i, Inventory playerinventory, Container inventory, int j, CallbackInfo ci) {
        this.inventory = (Inventory) playerinventory;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftInventory inventory;
        if (this.container instanceof Inventory) {
            inventory = new CraftInventoryPlayer((Inventory) this.container);
        } else if (this.container instanceof CompoundContainer) {
            inventory = new CraftInventoryDoubleChest((CompoundContainer) this.container);
        } else inventory = new CraftInventory(this.container);

        bukkitEntity = new CraftInventoryView((org.bukkit.entity.Player)((ServerPlayerBridge)this.inventory.player).getBukkitEntity(), inventory, (ChestMenu)(Object)this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}