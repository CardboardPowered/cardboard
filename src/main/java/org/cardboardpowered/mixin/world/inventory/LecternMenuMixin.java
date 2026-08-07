package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftInventoryLectern;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.cardboardpowered.bridge.world.inventory.LecternMenuBridge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternMenu.class)
public class LecternMenuMixin extends AbstractContainerMenuMixin implements LecternMenuBridge {

    @Shadow
    public Container lectern;

    @Shadow
    public ContainerData lecternData;

    private CraftInventoryView bukkitEntity = null;
    private org.bukkit.entity.Player player;

    @Override
    public void setPlayer(org.bukkit.entity.Player player) {
        this.player = player;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CraftInventoryLectern inventory = new CraftInventoryLectern(this.lectern);
        bukkitEntity = new CraftInventoryView(this.player, inventory, (LecternMenu)(Object)this);
        return bukkitEntity;
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public boolean clickMenuButton(Player entityhuman, int i) {
        int j;

        if (i >= 100) {
            j = i - 100;
            ((LecternMenu)(Object)this).setData(0, j);
            return true;
        } else {
            switch (i) {
                case 1:
                    j = this.lecternData.get(0);
                    ((LecternMenu)(Object)this).setData(0, j - 1);
                    return true;
                case 2:
                    j = this.lecternData.get(0);
                    ((LecternMenu)(Object)this).setData(0, j + 1);
                    return true;
                case 3:
                    if (!entityhuman.mayBuild()) return false;

                    PlayerTakeLecternBookEvent event = new PlayerTakeLecternBookEvent(player, ((CraftInventoryLectern) getBukkitView().getTopInventory()).getHolder());
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) return false;

                    ItemStack itemstack = this.lectern.removeItemNoUpdate(0);
                    this.lectern.setChanged();
                    if (!entityhuman.getInventory().add(itemstack))  entityhuman.drop(itemstack, false);

                    return true;
                default:
                    return false;
            }
        }
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}