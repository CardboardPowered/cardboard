package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventoryLoom;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LoomMenu.class)
public class LoomMenuMixin extends AbstractContainerMenuMixin {

    @Shadow public Container inputContainer;
    @Shadow public Container outputContainer;

    private CraftInventoryView bukkitEntity = null;
    private org.bukkit.entity.Player player;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    public void setPlayerInv(int i, Inventory playerinventory, ContainerLevelAccess containeraccesss, CallbackInfo ci) {
        // Client menus carry a LocalPlayer with no Bukkit entity
        if (playerinventory.player instanceof net.minecraft.server.level.ServerPlayer) {
            this.player = (org.bukkit.entity.Player)((EntityBridge)playerinventory.player).getBukkitEntity();
        }
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CraftInventoryLoom inventory = new CraftInventoryLoom(this.inputContainer, this.outputContainer);
        bukkitEntity = new CraftInventoryView(this.player, inventory, (LoomMenu)(Object)this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}