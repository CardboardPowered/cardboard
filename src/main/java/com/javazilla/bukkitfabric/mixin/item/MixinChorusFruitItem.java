package com.javazilla.bukkitfabric.mixin.item;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(ChorusFruitItem.class)
public class MixinChorusFruitItem extends Item {

    public MixinChorusFruitItem(Settings settings) {
        super(settings);
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public ItemStack finishUsing(ItemStack itemstack, World world, LivingEntity entity) {
        ItemStack itemstack1 = super.finishUsing(itemstack, world, entity);
        if (world.isClient) return itemstack1;

        for (int i = 0; i < 16; ++i) {
            double d3 = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;
            double d4 = MathHelper.clamp(entity.getY() + (double) (entity.getRandom().nextInt(16) - 8), 0.0D, (double) (world.getDimensionHeight() - 1));
            double d5 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;

            if (entity instanceof ServerPlayerEntity) {
                Player player = (Player)((IMixinServerEntityPlayer)((ServerPlayerEntity) entity)).getBukkitEntity();
                PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), new Location(player.getWorld(), d3, d4, d5), PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
                Bukkit.getServer().getPluginManager().callEvent(teleEvent);
                if (teleEvent.isCancelled()) break;
                d3 = teleEvent.getTo().getX();
                d4 = teleEvent.getTo().getY();
                d5 = teleEvent.getTo().getZ();
            }

            if (entity.hasVehicle()) entity.stopRiding();

            if (entity.teleport(d3, d4, d5, true)) {
                world.playSound((PlayerEntity) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.MASTER, 1.0F, 1.0F);
                entity.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
        if (entity instanceof PlayerEntity) ((PlayerEntity) entity).getItemCooldownManager().set(this, 20);

        return itemstack1;
    }

}