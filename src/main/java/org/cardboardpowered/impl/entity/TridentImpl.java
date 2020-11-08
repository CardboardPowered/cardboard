package org.cardboardpowered.impl.entity;

import net.minecraft.entity.projectile.TridentEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Trident;

public class TridentImpl extends ArrowImpl implements Trident {

    public TridentImpl(CraftServer server, TridentEntity entity) {
        super(server, entity);
    }

    @Override
    public TridentEntity getHandle() {
        return (TridentEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Trident";
    }

    @Override
    public EntityType getType() {
        return EntityType.TRIDENT;
    }

}