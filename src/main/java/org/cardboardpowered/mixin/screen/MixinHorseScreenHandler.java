package org.cardboardpowered.mixin.screen;

import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.HorseScreenHandler;

@Mixin(HorseScreenHandler.class)
public class MixinHorseScreenHandler extends MixinScreenHandler {

    @Shadow
    public Inventory inventory;

    private CardboardInventoryView bukkitEntity = null;
    private PlayerInventory playerInv;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, Inventory iinventory, final HorseBaseEntity entityhorseabstract, CallbackInfo ci) {
        this.playerInv = playerinventory;
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;
        return bukkitEntity = new CardboardInventoryView((Player)((IMixinEntity)this.playerInv.player).getBukkitEntity(), ((IMixinInventory)inventory).getOwner().getInventory(), (HorseScreenHandler)(Object)this);
    }


}