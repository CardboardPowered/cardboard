package org.cardboardpowered.mixin.screen;

import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.inventory.CardboardStonecutterInventory;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.StonecutterScreenHandler;

@Mixin(StonecutterScreenHandler.class)
public class MixinStonecutterScreenHandler extends MixinScreenHandler {

    private CardboardInventoryView bukkitEntity = null;
    private Player player;

    @Shadow public Inventory input;
    @Shadow public CraftingResultInventory output;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, final ScreenHandlerContext containeraccess, CallbackInfo ci) {
        this.player = (Player)((IMixinServerEntityPlayer)playerinventory.player).getBukkitEntity();
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CardboardStonecutterInventory inventory = new CardboardStonecutterInventory(this.input, this.output);
        bukkitEntity = new CardboardInventoryView(this.player, inventory, (StonecutterScreenHandler)(Object)this);
        return bukkitEntity;
    }

}
