package com.javazilla.bukkitfabric.mixin.screen;

import org.bukkit.Bukkit;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.inventory.CardboardLecternInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.PropertyDelegate;

@Mixin(LecternScreenHandler.class)
public class MixinLecternScreenHandler extends MixinScreenHandler {

    @Shadow
    public Inventory inventory;

    @Shadow
    public PropertyDelegate propertyDelegate;

    private CardboardInventoryView bukkitEntity = null;
    private Player player;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, Inventory iinventory, PropertyDelegate icontainerproperties, CallbackInfo ci) {
        this.player = (Player)((IMixinEntity)((PlayerInventory)iinventory).player).getBukkitEntity();
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CardboardLecternInventory inventory = new CardboardLecternInventory(this.inventory);
        bukkitEntity = new CardboardInventoryView(this.player, inventory, (LecternScreenHandler)(Object)this);
        return bukkitEntity;
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public boolean onButtonClick(PlayerEntity entityhuman, int i) {
        int j;

        if (i >= 100) {
            j = i - 100;
            ((LecternScreenHandler)(Object)this).setProperty(0, j);
            return true;
        } else {
            switch (i) {
                case 1:
                    j = this.propertyDelegate.get(0);
                    ((LecternScreenHandler)(Object)this).setProperty(0, j - 1);
                    return true;
                case 2:
                    j = this.propertyDelegate.get(0);
                    ((LecternScreenHandler)(Object)this).setProperty(0, j + 1);
                    return true;
                case 3:
                    if (!entityhuman.canModifyBlocks()) return false;

                    PlayerTakeLecternBookEvent event = new PlayerTakeLecternBookEvent(player, ((CardboardLecternInventory) getBukkitView().getTopInventory()).getHolder());
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) return false;

                    ItemStack itemstack = this.inventory.removeStack(0);
                    this.inventory.markDirty();
                    if (!entityhuman.getInventory().insertStack(itemstack))  entityhuman.dropItem(itemstack, false);

                    return true;
                default:
                    return false;
            }
        }
    }

}