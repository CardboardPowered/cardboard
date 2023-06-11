package org.cardboardpowered.mixin.screen;

import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.inventory.CardboardLoomInventory;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

@Mixin(LoomScreenHandler.class)
public class MixinLoomScreenHandler extends MixinScreenHandler {

    @Shadow public Inventory input;
    @Shadow public Inventory output;

    private CardboardInventoryView bukkitEntity = null;
    private Player player;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, ScreenHandlerContext containeraccesss, CallbackInfo ci) {
        this.player = (Player)((IMixinEntity)playerinventory.player).getBukkitEntity();
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CardboardLoomInventory inventory = new CardboardLoomInventory(this.input, this.output);
        bukkitEntity = new CardboardInventoryView(this.player, inventory, (LoomScreenHandler)(Object)this);
        return bukkitEntity;
    }

}