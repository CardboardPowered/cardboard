package com.javazilla.bukkitfabric.mixin.item;

import java.util.Random;
import java.util.function.Consumer;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

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

    @Overwrite
    public boolean damage(int i, Random random, ServerPlayerEntity entityplayer) {
        if (!((ItemStack)(Object)this).isDamageable()) return false;
        int j;

        if (i > 0) {
            j = EnchantmentHelper.getLevel(Enchantments.UNBREAKING, ((ItemStack)(Object)this));
            for (int l = 0; j > 0 && l < i; ++l) if (UnbreakingEnchantment.shouldPreventDamage(((ItemStack)(Object)this), j, random)) i--;

            if (entityplayer != null) {
                PlayerItemDamageEvent event = new PlayerItemDamageEvent((Player) ((IMixinServerEntityPlayer)entityplayer).getBukkitEntity(), CraftItemStack.asCraftMirror((ItemStack)(Object)this), i);
                event.getPlayer().getServer().getPluginManager().callEvent(event);

                if (i != event.getDamage() || event.isCancelled()) event.getPlayer().updateInventory();
                if (event.isCancelled()) return false;
                i = event.getDamage();
            }
            if (i <= 0) return false;
        }
        if (entityplayer != null && i != 0) Criteria.ITEM_DURABILITY_CHANGED.trigger(entityplayer, ((ItemStack)(Object)this), ((ItemStack)(Object)this).getDamage() + i);

        ((ItemStack)(Object)this).setDamage((j = ((ItemStack)(Object)this).getDamage() + i));
        return j >= ((ItemStack)(Object)this).getMaxDamage();
    }

    @Overwrite
    public <T extends LivingEntity> void damage(int i, T t0, Consumer<T> consumer) {
        if (!t0.world.isClient && (!(t0 instanceof PlayerEntity) || !((PlayerEntity) t0).abilities.creativeMode)) {
            if (((ItemStack)(Object)this).isDamageable()) {
                if (this.damage(i, t0.getRandom(), t0 instanceof ServerPlayerEntity ? (ServerPlayerEntity) t0 : null)) {
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
    }

}