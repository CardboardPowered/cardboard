package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinLivingEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

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
                        ((CraftPlayer)((IMixinServerEntityPlayer)((ServerPlayerEntity) get())).getBukkitEntity()).updateScaledHealth();
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

}