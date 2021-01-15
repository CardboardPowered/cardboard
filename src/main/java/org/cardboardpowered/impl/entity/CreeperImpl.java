package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.interfaces.IMixinCreeperEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.mob.CreeperEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreeperPowerEvent;

public class CreeperImpl extends MonsterImpl implements Creeper {

    public CreeperImpl(CraftServer server, CreeperEntity entity) {
        super(server, entity);
    }

    @Override
    public boolean isPowered() {
        return ((IMixinCreeperEntity)getHandle()).isPoweredBF();
    }

    @Override
    public void setPowered(boolean powered) {
        CraftServer server = this.server;
        Creeper entity = (Creeper) ((IMixinEntity)this.getHandle()).getBukkitEntity();

        if (powered) {
            CreeperPowerEvent event = new CreeperPowerEvent(entity, CreeperPowerEvent.PowerCause.SET_ON);
            server.getPluginManager().callEvent(event);

            if (!event.isCancelled())
                ((IMixinCreeperEntity)getHandle()).setPowered(true);
        } else {
            CreeperPowerEvent event = new CreeperPowerEvent(entity, CreeperPowerEvent.PowerCause.SET_OFF);
            server.getPluginManager().callEvent(event);

            if (!event.isCancelled())
                ((IMixinCreeperEntity)getHandle()).setPowered(false);
        }
    }

    @Override
    public void setMaxFuseTicks(int ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks < 0");

        ((IMixinCreeperEntity)getHandle()).setFuseTimeBF(ticks);
    }

    @Override
    public int getMaxFuseTicks() {
        return ((IMixinCreeperEntity)getHandle()).getFuseTimeBF();
    }

    @Override
    public void setExplosionRadius(int radius) {
        Preconditions.checkArgument(radius >= 0, "radius < 0");

        ((IMixinCreeperEntity)getHandle()).setExplosionRadiusBF(radius);
    }

    @Override
    public int getExplosionRadius() {
        return ((IMixinCreeperEntity)getHandle()).getExplosionRadiusBF();
    }

    @Override
    public void explode() {
        ((IMixinCreeperEntity)getHandle()).explodeBF();
    }

    @Override
    public void ignite() {
        getHandle().ignite();
    }

    @Override
    public CreeperEntity getHandle() {
        return (CreeperEntity) nms;
    }

    @Override
    public String toString() {
        return "Creeper";
    }

    @Override
    public EntityType getType() {
        return EntityType.CREEPER;
    }

    @Override
    public int getFuseTicks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isIgnited() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setIgnited(boolean arg0) {
        // TODO Auto-generated method stub
        
    }
}
