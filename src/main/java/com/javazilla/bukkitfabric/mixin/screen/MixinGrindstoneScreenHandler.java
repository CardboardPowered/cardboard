package com.javazilla.bukkitfabric.mixin.screen;

import org.cardboardpowered.impl.inventory.CardboardGrindstoneInventory;
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
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

@Mixin(GrindstoneScreenHandler.class)
public class MixinGrindstoneScreenHandler extends MixinScreenHandler {

    private CardboardInventoryView bukkitEntity = null;
    private Player player;

    @Shadow private Inventory result;
    @Shadow private Inventory input;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, final ScreenHandlerContext containeraccess, CallbackInfo ci) {
        this.player = (Player)((IMixinServerEntityPlayer)playerinventory.player).getBukkitEntity();
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CardboardGrindstoneInventory inventory = new CardboardGrindstoneInventory(this.input, this.result);
        bukkitEntity = new CardboardInventoryView(this.player, inventory, (GrindstoneScreenHandler)(Object)this);
        return bukkitEntity;
    }

}
