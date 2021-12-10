package org.cardboardpowered.mixin.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(value = PlayerEntity.class, priority = 900)
public class MixinPlayerEntity {
    
    private ItemEntity cardboard_stored_entity;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setPickupDelay(I)V"),
            method = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;")
    public void store_item_entity(ItemEntity ie, int i, net.minecraft.item.ItemStack stack, boolean z, boolean z2) {
        ie.setPickupDelay(i);
        cardboard_stored_entity = ie;
    }

    @SuppressWarnings("deprecation")
    @Inject(at = @At("RETURN"),
            method = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            cancellable = true)
    public void cardboard_doPlayerDropItemEvent(net.minecraft.item.ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> ci) {
        if (stack.isEmpty()) {
            return;
        }
        Player player = (Player)(((IMixinEntity)this).getBukkitEntity());
        Item drop = (Item) ((IMixinEntity)cardboard_stored_entity).getBukkitEntity();
        PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ItemStack cur = player.getInventory().getItemInHand();
            if (retainOwnership && (cur == null || cur.getAmount() == 0)) {
                player.getInventory().setItemInHand(drop.getItemStack());
            } else if (retainOwnership && cur.isSimilar(drop.getItemStack()) && cur.getAmount() < cur.getMaxStackSize() && drop.getItemStack().getAmount() == 1) {
                cur.setAmount(cur.getAmount() + 1);
                player.getInventory().setItemInHand(cur);
            } else player.getInventory().addItem(drop.getItemStack());

            cardboard_stored_entity = null;
            ci.setReturnValue(null);
        }
        cardboard_stored_entity = null;
    }

}
