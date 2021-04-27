package org.cardboardpowered.mixin.screen;

import org.cardboardpowered.impl.inventory.CardboardBrewerInventory;
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
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.PropertyDelegate;

@Mixin(BrewingStandScreenHandler.class)
public class MixinBrewingStandScreenHandler extends MixinScreenHandler {

    @Shadow
    public Inventory inventory;

    private CardboardInventoryView bukkitEntity = null;
    private PlayerInventory player;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory inventory, Inventory iinventory, PropertyDelegate icontainerproperties, CallbackInfo ci) {
        this.player = (PlayerInventory) inventory;
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CardboardBrewerInventory inventory = new CardboardBrewerInventory(this.inventory);
        bukkitEntity = new CardboardInventoryView((Player)((IMixinServerEntityPlayer)this.player.player).getBukkitEntity(), inventory, (BrewingStandScreenHandler)(Object)this);
        return bukkitEntity;
    }

}