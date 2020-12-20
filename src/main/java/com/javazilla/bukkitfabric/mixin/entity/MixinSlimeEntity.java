package com.javazilla.bukkitfabric.mixin.entity;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinSlimeEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.text.Text;

@Mixin(SlimeEntity.class)
public class MixinSlimeEntity extends MixinEntity implements IMixinSlimeEntity {

    @Shadow public int getSize() {return 0;}
    @Shadow public void setSize(int i, boolean flag) {}

    @Override
    public void setSizeBF(int i, boolean flag) {
        setSize(i, flag);
    }

    /**
     * @author .
     * @reason Call SlimeSplitEvent & EntityTransformEvent
     */
    @SuppressWarnings("resource")
    @Overwrite
    public void remove() {
        int i = this.getSize();
        if (!((SlimeEntity)(Object)this).getEntityWorld().isClient && i > 1 && ((SlimeEntity)(Object)this).isDead()) {
            Text ichatbasecomponent = ((SlimeEntity)(Object)this).getCustomName();
            boolean flag = ((SlimeEntity)(Object)this).isAiDisabled();
            float f = (float) i / 4.0F;
            int j = i / 2;
            int k = 2 + this.random.nextInt(3);

            SlimeSplitEvent event = new SlimeSplitEvent((org.bukkit.entity.Slime) ((IMixinEntity)(SlimeEntity)(Object)this).getBukkitEntity(), k);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

            if (!event.isCancelled() && event.getCount() > 0) {
                k = event.getCount();
            } else {
                super.remove();
                return;
            }
            List<LivingEntity> slimes = new ArrayList<>(j);

            for (int l = 0; l < k; ++l) {
                float f1 = ((float) (l % 2) - 0.5F) * f;
                float f2 = ((float) (l / 2) - 0.5F) * f;
                SlimeEntity entityslime = (SlimeEntity) ((SlimeEntity)(Object)this).getType().create(((SlimeEntity)(Object)this).world);
                if (((SlimeEntity)(Object)this).isPersistent()) entityslime.setPersistent();
                entityslime.setCustomName(ichatbasecomponent);
                entityslime.setAiDisabled(flag);
                entityslime.setInvulnerable(((SlimeEntity)(Object)this).isInvulnerable());
                ((IMixinSlimeEntity)entityslime).setSizeBF(j, true);
                entityslime.refreshPositionAndAngles(((SlimeEntity)(Object)this).getX() + (double) f1, ((SlimeEntity)(Object)this).getY() + 0.5D, ((SlimeEntity)(Object)this).getZ() + (double) f2, this.random.nextFloat() * 360.0F, 0.0F);
                slimes.add(entityslime);
            }

            if (BukkitEventFactory.callEntityTransformEvent(((SlimeEntity)(Object)this), slimes, EntityTransformEvent.TransformReason.SPLIT).isCancelled()) return;
            for (LivingEntity living : slimes) ((SlimeEntity)(Object)this).world.spawnEntity(living);
        }
        super.remove();
    }

}