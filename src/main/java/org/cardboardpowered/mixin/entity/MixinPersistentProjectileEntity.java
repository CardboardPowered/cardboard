package org.cardboardpowered.mixin.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.cardboardpowered.impl.entity.ItemEntityImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinPersistentProjectileEntity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.ItemStack;

@Mixin(PersistentProjectileEntity.class)
public abstract class MixinPersistentProjectileEntity implements IMixinPersistentProjectileEntity {

    @Shadow public boolean inGround;
    @Shadow public int life;
    @Shadow public int punch;
    @Shadow public PersistentProjectileEntity.PickupPermission pickupType;

    @Override
    public int getPunchBF() {
        return punch;
    }

    @Override
    public boolean getInGroundBF() {
        return inGround;
    }

    @Override
    public void setLifeBF(int value) {
        this.life = value;
    }

    private PersistentProjectileEntity getBF() {
        return (PersistentProjectileEntity)(Object)this;
    }

    @SuppressWarnings("deprecation")
    @Inject(at = @At("HEAD"), method = "onPlayerCollision", cancellable = true)
    public void doBukkitEvent_PlayerPickupArrowEvent(PlayerEntity entityhuman, CallbackInfo ci) {
        if (!getBF().world.isClient && (this.inGround || getBF().isNoClip()) && getBF().shake <= 0) {
            ItemStack itemstack = this.asItemStack();
            if (this.pickupType == PickupPermission.ALLOWED && !itemstack.isEmpty()) {
                ItemEntity item = new ItemEntity(getBF().world, getBF().getX(), getBF().getY(), getBF().getZ(), itemstack);
                PlayerPickupArrowEvent event = new PlayerPickupArrowEvent((org.bukkit.entity.Player) ((IMixinEntity)entityhuman).getBukkitEntity(), new ItemEntityImpl(CraftServer.INSTANCE, getBF(), item), (org.bukkit.entity.AbstractArrow) ((IMixinEntity)this).getBukkitEntity());
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    ci.cancel();
                    return;
                }
                itemstack = item.getStack();
            }
            boolean flag = this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED || this.pickupType == PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY && entityhuman.getAbilities().creativeMode || getBF().isNoClip() && getBF().getOwner().getUuid() == entityhuman.getUuid();
            if (!flag) {
                ci.cancel();
                return;
            }
        }
    }

    @Shadow
    public abstract ItemStack asItemStack();

}
