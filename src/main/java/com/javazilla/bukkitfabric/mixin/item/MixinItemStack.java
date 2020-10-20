package com.javazilla.bukkitfabric.mixin.item;

import java.util.Random;
import java.util.function.Consumer;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(at = @At("HEAD"), method = "damage", cancellable = true)
    public void callPlayerItemDamageEvent(int i, Random random, ServerPlayerEntity entityplayer, CallbackInfoReturnable<Boolean> ci) {
        if (!((ItemStack)(Object)this).isDamageable()) {
            ci.setReturnValue(false);
            return;
        }
        int j;

        if (i > 0) {
            j = EnchantmentHelper.getLevel(Enchantments.UNBREAKING, ((ItemStack)(Object)this));
            for (int l = 0; j > 0 && l < i; ++l) if (UnbreakingEnchantment.shouldPreventDamage(((ItemStack)(Object)this), j, random)) i--;

            if (entityplayer != null) {
                PlayerItemDamageEvent event = new PlayerItemDamageEvent((Player) ((IMixinServerEntityPlayer)entityplayer).getBukkitEntity(), CraftItemStack.asCraftMirror((ItemStack)(Object)this), i);
                event.getPlayer().getServer().getPluginManager().callEvent(event);

                if (i != event.getDamage() || event.isCancelled()) event.getPlayer().updateInventory();
                if (event.isCancelled()) {
                    ci.setReturnValue(false);
                    return;
                }
                i = event.getDamage();
            }
            if (i <= 0) {
                ci.setReturnValue(false);
                return;
            }
        }
        if (entityplayer != null && i != 0) Criteria.ITEM_DURABILITY_CHANGED.trigger(entityplayer, ((ItemStack)(Object)this), ((ItemStack)(Object)this).getDamage() + i);

        ((ItemStack)(Object)this).setDamage((j = ((ItemStack)(Object)this).getDamage() + i));
        ci.setReturnValue(j >= ((ItemStack)(Object)this).getMaxDamage());
        return;
    }

    @Inject(at = @At("HEAD"), method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", cancellable = true)
    public <T extends LivingEntity> void damage(int i, T t0, Consumer<T> consumer, CallbackInfo ci) {
        if (!t0.world.isClient && (!(t0 instanceof PlayerEntity) || !((PlayerEntity) t0).abilities.creativeMode)) {
            if (((ItemStack)(Object)this).isDamageable()) {
                if (((ItemStack)(Object)this).damage(i, t0.getRandom(), t0 instanceof ServerPlayerEntity ? (ServerPlayerEntity) t0 : null)) {
                    consumer.accept(t0);
                    Item item = ((ItemStack)(Object)this).getItem();
                    if (((ItemStack)(Object)this).count == 1 && t0 instanceof PlayerEntity)
                        BukkitEventFactory.callPlayerItemBreakEvent((PlayerEntity) t0, ((ItemStack)(Object)this));

                    ((ItemStack)(Object)this).decrement(1);
                    if (t0 instanceof PlayerEntity)
                        ((PlayerEntity) t0).incrementStat(Stats.BROKEN.getOrCreateStat(item));
                    ((ItemStack)(Object)this).setDamage(0);
                }

            }
        }
        ci.cancel();
        return;
    }

}