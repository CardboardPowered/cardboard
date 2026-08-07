package org.cardboardpowered.mixin.world.level.block.entity;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.inventory.LecternMenuBridge;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin {

    /**
     * The lectern menu is built from the lectern's own container, so it never sees who opened it.
     * Hand the viewer over here, where it is known.
     */
    @Inject(method = "createMenu", at = @At("RETURN"))
    public void setPlayer(int i, Inventory playerinventory, Player entityhuman, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if (cir.getReturnValue() instanceof LecternMenuBridge menu) {
            menu.setPlayer((org.bukkit.entity.Player) ((EntityBridge) entityhuman).getBukkitEntity());
        }
    }
}
