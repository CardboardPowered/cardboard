package com.javazilla.bukkitfabric.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.inventory.StonecutterInventoryImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.StonecutterScreenHandler;

@Mixin(StonecutterScreenHandler.class)
public class MixinStonecutterScreenHandler extends MixinScreenHandler {

    private CraftInventoryView bukkitEntity = null;
    private Player player;

    @Shadow public Inventory input;
    @Shadow public CraftingResultInventory output;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, final ScreenHandlerContext containeraccess, CallbackInfo ci) {
        this.player = (Player)((IMixinServerEntityPlayer)playerinventory.player).getBukkitEntity();
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        StonecutterInventoryImpl inventory = new StonecutterInventoryImpl(this.input, this.output);
        bukkitEntity = new CraftInventoryView(this.player, inventory, (StonecutterScreenHandler)(Object)this);
        return bukkitEntity;
    }

}
