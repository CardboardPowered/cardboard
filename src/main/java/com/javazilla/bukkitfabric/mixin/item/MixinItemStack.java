package com.javazilla.bukkitfabric.mixin.item;

import java.util.Random;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

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

}