package org.cardboardpowered.mixin.world.entity.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.cardboardpowered.mixin.world.entity.EntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.cardboardpowered.bridge.world.entity.item.ItemEntityBridge;
import org.cardboardpowered.bridge.world.entity.player.InventoryBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

@Mixin(ItemEntity.class)
@SuppressWarnings("deprecation")
public class ItemEntityMixin extends EntityMixin implements ItemEntityBridge {

	/**
	 * net.minecraft.class_1542.field_7201
	 */
	@Shadow
	public int health;
	
	/**
	 * net.minecraft.class_1542.field_7204
	 */
	@Shadow
	public int age;
	
	@Override
	public int cardboard$itemAge() {
		return age;
	}
	
	@Override
	public int cardboard$getHealth() {
		return health;
	}
	
	@Override
	public void cardboard$setItemAge(int value) {
		this.age = value;
	}
	
	@Override
	public void cardboard$setUnlimitedAge(boolean noLimit) {
		if (noLimit) {
			this.age = -32768;
		} else {
			this.age = ((ItemEntity)(Object)this).tickCount; // TODO: age vs totalEntityAge
		}
	}
	
	@Override
	public void cardboard$setHealth(int health) {
		ItemEntity entity = ((ItemEntity)(Object)this);
		if (health <= 0) {
			entity.getItem().onDestroyed(entity);
			entity.discard(); // Cause = Plugin
		} else {
			this.health = health;
		}
	}
	
    @Shadow
    public int pickupDelay;

    // @Shadow
    // public UUID owner;

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    public void setBukkitEntity(CallbackInfo callbackInfo) {
        // Client entities have no Bukkit side; on a remote server CraftServer.console is null
        if (null == bukkitEntity && !((ItemEntity) (Object) this).level().isClientSide())
            this.bukkitEntity = new CraftItem(CraftServer.INSTANCE, (ItemEntity) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V", cancellable = true)
    private static void fireMergeEvent(ItemEntity entityitem, ItemStack itemstack, ItemEntity entityitem1, ItemStack itemstack1, CallbackInfo ci) {
        if (CraftEventFactory.callItemMergeEvent(entityitem1, entityitem).isCancelled()) {
            ci.cancel();
            return;
        }
    }

    /**
     * @reason EntityPickupItemEvent
     */
    @Inject(at = @At("HEAD"), method = "playerTouch", cancellable = true)
    public void fireEntityPickupItemEvent(Player entityhuman, CallbackInfo ci) {
        if (this.mc_world().isClientSide()) return;
        ItemStack itemstack = ((ItemEntity)(Object)this).getItem();
        int i = itemstack.getCount();

        // CraftBukkit start - fire PlayerPickupItemEvent
        int canHold = ((InventoryBridge)entityhuman.getInventory()).canHold(itemstack);
        int remaining = i - canHold;

        if (this.pickupDelay <= 0 && canHold > 0) {
            itemstack.setCount(canHold);
            // Call legacy event
            PlayerPickupItemEvent playerEvent = new PlayerPickupItemEvent((org.bukkit.entity.Player) ((ServerPlayerBridge)entityhuman).getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
            //playerEvent.setCancelled(!entityhuman.canPickUpLoot);
            Bukkit.getServer().getPluginManager().callEvent(playerEvent);
            if (playerEvent.isCancelled()) {
                itemstack.setCount(i); // SPIGOT-5294 - restore count
                return;
            }

            // Call newer event afterwards
            EntityPickupItemEvent entityEvent = new EntityPickupItemEvent((org.bukkit.entity.Player) ((ServerPlayerBridge)entityhuman).getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
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