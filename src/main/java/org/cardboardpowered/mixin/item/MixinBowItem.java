package org.cardboardpowered.mixin.item;

import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@Mixin(BowItem.class)
public class MixinBowItem {

    public boolean cancel_BF = false;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"), method = "onStoppedUsing", cancellable = true)
    public void bukkitize2(ItemStack a, World b, LivingEntity c, int d, CallbackInfo ci) {
        if (cancel_BF) {
            cancel_BF = false;
            if (c instanceof PlayerEntity) {
                Player plr = (Player) ((IMixinServerEntityPlayer)((PlayerEntity) c)).getBukkitEntity();
                plr.updateInventory();
            }
            ci.cancel();
            return;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArrowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"),
            method = "onStoppedUsing")
    public PersistentProjectileEntity bukkitize(ArrowItem itemarrow, World world, ItemStack itemstack, LivingEntity entityliving) {
        PlayerEntity entityhuman = (PlayerEntity) entityliving;
        ItemStack itemstack1 = entityhuman.getArrowType(itemstack);

        PersistentProjectileEntity entityarrow = itemarrow.createArrow(world, itemstack1, (LivingEntity) entityhuman);
        cancel_BF = false;

        boolean flag = entityhuman.abilities.creativeMode || EnchantmentHelper.getLevel(Enchantments.INFINITY, itemstack) > 0;
        boolean flag1 = flag && itemstack1.getItem() == Items.ARROW;

        entityarrow.setVelocity(entityhuman, entityhuman.pitch, entityhuman.yaw, 0.0F, 3.0F, 1.0F);
        int k = EnchantmentHelper.getLevel(Enchantments.POWER, itemstack);
        if (k > 0) entityarrow.setDamage(entityarrow.getDamage() + (double) k * 0.5D + 0.5D);

        int l = EnchantmentHelper.getLevel(Enchantments.PUNCH, itemstack);
        if (l > 0) entityarrow.setPunch(l);
        if (EnchantmentHelper.getLevel(Enchantments.FLAME, itemstack) > 0) entityarrow.setOnFireFor(100);

        org.bukkit.event.entity.EntityShootBowEvent event = BukkitEventFactory.callEntityShootBowEvent(entityhuman, itemstack, itemstack1, entityarrow, entityhuman.getActiveHand(), 0f, !flag1);
        if (event.isCancelled()) 
            cancel_BF = true;
        return entityarrow;
    }

}