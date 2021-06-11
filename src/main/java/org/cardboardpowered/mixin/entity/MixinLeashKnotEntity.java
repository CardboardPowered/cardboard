package org.cardboardpowered.mixin.entity;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

@Mixin(LeashKnotEntity.class)
public class MixinLeashKnotEntity {

    private LeashKnotEntity getBF() {
        return (LeashKnotEntity)(Object)this;
    }

    @Overwrite
    public ActionResult interact(PlayerEntity entityhuman, Hand enumhand) {
        if (getBF().world.isClient) return ActionResult.SUCCESS;

        boolean flag = false;
        List<MobEntity> list = getBF().world.getNonSpectatingEntities(MobEntity.class, new Box(getBF().getX() - 7.0D, getBF().getY() - 7.0D, getBF().getZ() - 7.0D, getBF().getX() + 7.0D, getBF().getY() + 7.0D, getBF().getZ() + 7.0D));
        Iterator<MobEntity> iterator = list.iterator();
        MobEntity entityinsentient;
        while (iterator.hasNext()) {
            entityinsentient = (MobEntity) iterator.next();
            if (entityinsentient.getHoldingEntity() == entityhuman) {
                if (BukkitEventFactory.callPlayerLeashEntityEvent(entityinsentient, ((LeashKnotEntity)(Object)this), entityhuman).isCancelled()) {
                    ((ServerPlayerEntity) entityhuman).networkHandler.sendPacket(new EntityAttachS2CPacket(entityinsentient, entityinsentient.getHoldingEntity()));
                    continue;
                }
                entityinsentient.attachLeash((LeashKnotEntity)(Object)this, true);
                flag = true;
            }
        }
        if (flag) return ActionResult.CONSUME;
        boolean die = true;
        iterator = list.iterator();
        while (iterator.hasNext()) {
            entityinsentient = (MobEntity) iterator.next();
            if (entityinsentient.isLeashed() && entityinsentient.getHoldingEntity() == getBF()) {
                if (BukkitEventFactory.callPlayerUnleashEntityEvent(entityinsentient, entityhuman).isCancelled()) {
                    die = false;
                    continue;
                }
                entityinsentient.detachLeash(true, !entityhuman.getAbilities().creativeMode);
            }
        }
        if (die) getBF().remove(RemovalReason.KILLED);
        return ActionResult.CONSUME;
    }

}