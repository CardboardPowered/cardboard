package com.javazilla.bukkitfabric.mixin.entity;

import java.util.Map.Entry;

import org.bukkit.event.player.PlayerItemMendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(ExperienceOrbEntity.class)
public class MixinExperienceOrbEntity extends MixinEntity {

    @Shadow
    public int amount;

    @Shadow
    public int pickupDelay;

    @Overwrite
    public void onPlayerCollision(PlayerEntity entityhuman) {
        if (!this.world.isClient) {
            if (this.pickupDelay == 0 && entityhuman.experiencePickUpDelay == 0) {
                entityhuman.experiencePickUpDelay = 2;
                entityhuman.sendPickup((Entity)(Object)this, 1);
                Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.chooseEquipmentWith(Enchantments.MENDING, (LivingEntity) entityhuman, ItemStack::isDamaged);

                if (entry != null) {
                    ItemStack itemstack = (ItemStack) entry.getValue();

                    if (!itemstack.isEmpty() && itemstack.isDamaged()) {
                        int i = Math.min(this.getMendingRepairAmount(this.amount), itemstack.getDamage());
                        PlayerItemMendEvent event = BukkitEventFactory.callPlayerItemMendEvent(entityhuman, (ExperienceOrbEntity)(Object)this, itemstack, i);
                        i = event.getRepairAmount();
                        if (!event.isCancelled()) {
                            this.amount -= this.getMendingRepairCost(i);
                            itemstack.setDamage(itemstack.getDamage() - i);
                        }
                    }
                }
                if (this.amount > 0)
                    entityhuman.addExperience(BukkitEventFactory.callPlayerExpChangeEvent(entityhuman, this.amount).getAmount());
                this.remove();
            }

        }
    }

    @Shadow
    public int getMendingRepairCost(int i) {
        return i / 2;
    }

    @Shadow
    public int getMendingRepairAmount(int i) {
        return i * 2;
    }

}