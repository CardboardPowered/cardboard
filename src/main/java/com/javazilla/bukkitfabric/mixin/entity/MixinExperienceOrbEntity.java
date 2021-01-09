package com.javazilla.bukkitfabric.mixin.entity;

import java.util.Map.Entry;

import org.bukkit.event.player.PlayerItemMendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
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

    @Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), method = "onPlayerCollision")
    public int doBukkitEvent_PlayerItemMendEvent(int a, int b, PlayerEntity entityhuman) {
        Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.chooseEquipmentWith(Enchantments.MENDING, (LivingEntity) entityhuman, ItemStack::isDamaged);
        ItemStack itemstack = (ItemStack) entry.getValue();

        int i = Math.min(a, b);
        PlayerItemMendEvent event = BukkitEventFactory.callPlayerItemMendEvent(entityhuman, (ExperienceOrbEntity)(Object)this, itemstack, i);
        i = event.getRepairAmount();
        if (!event.isCancelled()) {
            return i;
        } else return 0;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"), method = "onPlayerCollision")
    public void doBukkitEvent_PlayerExpChangeEvent(PlayerEntity plr, int a) {
        plr.addExperience(BukkitEventFactory.callPlayerExpChangeEvent(plr, this.amount).getAmount());
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