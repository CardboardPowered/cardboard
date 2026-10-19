package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.inventory.CraftInventoryGrindstone;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin extends AbstractContainerMenuMixin {

    private CraftInventoryView bukkitEntity = null;
    private org.bukkit.entity.Player player;

    @Shadow private Container resultSlots;
    @Shadow private Container repairSlots;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    public void setPlayerInv(int i, Inventory playerinventory, final ContainerLevelAccess containeraccess, CallbackInfo ci) {
        // Client menus (screens, JEI's fake menu) carry a LocalPlayer with no Bukkit entity
        if (playerinventory.player instanceof ServerPlayerBridge bridge) {
            this.player = (org.bukkit.entity.Player) bridge.getBukkitEntity();
        }
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftInventoryGrindstone inventory = new CraftInventoryGrindstone(this.repairSlots, this.resultSlots);
        bukkitEntity = new CraftInventoryView(this.player, inventory, (GrindstoneMenu)(Object)this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}
