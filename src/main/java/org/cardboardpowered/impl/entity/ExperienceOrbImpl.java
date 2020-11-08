package org.cardboardpowered.impl.entity;

import net.minecraft.entity.ExperienceOrbEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

public class ExperienceOrbImpl extends CraftEntity implements ExperienceOrb {

    public ExperienceOrbImpl(CraftServer server, ExperienceOrbEntity entity) {
        super(entity);
    }

    @Override
    public int getExperience() {
        return -1;// TODO return getHandle().amount;
    }

    @Override
    public void setExperience(int value) {
        // TODO getHandle().amount = value;
    }

    @Override
    public ExperienceOrbEntity getHandle() {
        return (ExperienceOrbEntity) nms;
    }

    @Override
    public String toString() {
        return "ExperienceOrbImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.EXPERIENCE_ORB;
    }

}