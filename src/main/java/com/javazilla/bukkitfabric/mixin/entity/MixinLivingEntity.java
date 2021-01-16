package com.javazilla.bukkitfabric.mixin.entity;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinLivingEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;

@Mixin(LivingEntity.class)
public class MixinLivingEntity extends MixinEntity implements IMixinLivingEntity {

    private LivingEntity get() {
        return (LivingEntity)(Object)this;
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void consumeItem() {
        Hand enumhand = get().getActiveHand();
        if (!get().activeItemStack.equals(get().getStackInHand(enumhand))) {
            get().stopUsingItem();
        } else {
            if (!get().activeItemStack.isEmpty() && get().isUsingItem()) {
                get().spawnConsumptionEffects(get().activeItemStack, 16);
                ItemStack itemstack;
                if (get() instanceof ServerPlayerEntity) {
                    org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitCopy(get().activeItemStack);
                    PlayerItemConsumeEvent event = new PlayerItemConsumeEvent((Player) ((IMixinServerEntityPlayer)((ServerPlayerEntity) get())).getBukkitEntity(), craftItem);
                    Bukkit.getServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        ((Player)((IMixinServerEntityPlayer)((ServerPlayerEntity) get())).getBukkitEntity()).updateInventory();
                        ((PlayerImpl)((IMixinServerEntityPlayer)((ServerPlayerEntity) get())).getBukkitEntity()).updateScaledHealth();
                        return;
                    }
                    itemstack = (craftItem.equals(event.getItem())) ? get().activeItemStack.finishUsing(get().world, get()) : CraftItemStack.asNMSCopy(event.getItem()).finishUsing(get().world, get());
                } else itemstack = get().activeItemStack.finishUsing(get().world, get());
                // CraftBukkit end

                if (itemstack != get().activeItemStack) get().setStackInHand(enumhand, itemstack);
                get().clearActiveItem();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "drop", cancellable = true)
    public void drop(DamageSource damagesource, CallbackInfo ci) {
        Entity entity = damagesource.getAttacker();

        boolean flag = get().playerHitTimer > 0;
        get().dropInventory();
        if (!get().isBaby() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.dropLoot(damagesource, flag);
            this.dropEquipment(damagesource, ((entity instanceof PlayerEntity) ? EnchantmentHelper.getLooting((LivingEntity) entity) : 0), flag);
        }
        BukkitEventFactory.callEntityDeathEvent(get(), ((IMixinEntity)this).cardboard_getDrops());
        ((IMixinEntity)this).cardboard_setDrops(new ArrayList<>());
        get().dropXp();
        ci.cancel();
        return;
    }

    @Shadow
    public int getCurrentExperience(PlayerEntity entityhuman) {
        return 0;
    }

    @Override
    public int getExpReward() {
        if ((get().shouldAlwaysDropXp() || get().lastDamageTime > 0 && get().canDropLootAndXp() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))) {
            int i = getCurrentExperience(get().attackingPlayer);
            return i;
        } else return 0;
    }

    @Shadow
    public void dropLoot(DamageSource damagesource, boolean flag) {
    }

    @Shadow
    public void dropEquipment(DamageSource damagesource, int i, boolean flag) {
    }

    /**
     * @reason Bukkit RegainHealthEvent
     */
    @Inject(at = @At("HEAD"), method = "heal", cancellable = true)
    public void doRegainHealthEvent(float f, CallbackInfo ci) {
        heal(f, EntityRegainHealthEvent.RegainReason.CUSTOM);
        ci.cancel();
        return;
    }

    public void heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
        if (get().getHealth() > 0.0F) {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(this.getBukkitEntity(), f, regainReason);
            if (this.isValidBF()) {
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) get().setHealth((float) (get().getHealth() + event.getAmount()));
            }
        }
    }

}