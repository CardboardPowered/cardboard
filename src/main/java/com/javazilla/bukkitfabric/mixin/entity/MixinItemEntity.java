package com.javazilla.bukkitfabric.mixin.entity;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import org.cardboardpowered.impl.entity.ItemEntityImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntity.class)
@SuppressWarnings("deprecation")
public class MixinItemEntity extends MixinEntity {

    @Shadow
    public int pickupDelay;

    @Shadow
    public UUID owner;

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    public void setBukkit(CallbackInfo callbackInfo) {
        if (null == bukkit)
            this.bukkit = new ItemEntityImpl(CraftServer.INSTANCE, (ItemEntity) (Object) this, (ItemEntity) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;)V", cancellable = true)
    private static void fireMergeEvent(ItemEntity entityitem, ItemStack itemstack, ItemEntity entityitem1, ItemStack itemstack1, CallbackInfo ci) {
        if (BukkitEventFactory.callItemMergeEvent(entityitem1, entityitem).isCancelled()) {
            ci.cancel();
            return;
        }
    }

    /**
     * @reason EntityPickupItemEvent
     */
    @Inject(at = @At("HEAD"), method = "onPlayerCollision", cancellable = true)
    public void fireEntityPickupItemEvent(PlayerEntity entityhuman, CallbackInfo ci) {
        if (this.world.isClient) return;
        ItemStack itemstack = ((ItemEntity)(Object)this).getStack();
        int i = itemstack.getCount();

        // CraftBukkit start - fire PlayerPickupItemEvent
        int canHold = ((IMixinPlayerInventory)entityhuman.inventory).canHold(itemstack);
        int remaining = i - canHold;

        if (this.pickupDelay <= 0 && canHold > 0) {
            itemstack.setCount(canHold);
            // Call legacy event
            PlayerPickupItemEvent playerEvent = new PlayerPickupItemEvent((org.bukkit.entity.Player) ((IMixinServerEntityPlayer)entityhuman).getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
            //playerEvent.setCancelled(!entityhuman.canPickUpLoot);
            Bukkit.getServer().getPluginManager().callEvent(playerEvent);
            if (playerEvent.isCancelled()) {
                itemstack.setCount(i); // SPIGOT-5294 - restore count
                return;
            }

            // Call newer event afterwards
            EntityPickupItemEvent entityEvent = new EntityPickupItemEvent((org.bukkit.entity.Player) ((IMixinServerEntityPlayer)entityhuman).getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
            //entityEvent.setCancelled(!entityhuman.canPickUpLoot);
            Bukkit.getServer().getPluginManager().callEvent(entityEvent);
            if (entityEvent.isCancelled()) {
                itemstack.setCount(i); // SPIGOT-5294 - restore count
                ci.cancel();
                return;
            }
            itemstack.setCount(canHold + remaining); // = i
            this.pickupDelay = 0;
        } else if (this.pickupDelay == 0) this.pickupDelay = -1;
    }

}