package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.passive.ParrotEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;

public class ParrotImpl extends TameableAnimalImpl implements Parrot {

    public ParrotImpl(CraftServer server, ParrotEntity parrot) {
        super(server, parrot);
    }

    @Override
    public ParrotEntity getHandle() {
        return (ParrotEntity) nms;
    }

    @Override
    public Variant getVariant() {
        return Variant.values()[getHandle().getVariant().ordinal()];
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "variant");

        getHandle().setVariant(ParrotEntity.Variant.byIndex(variant.ordinal()));
    }

    @Override
    public String toString() {
        return "CraftParrot";
    }

    @Override
    public EntityType getType() {
        return EntityType.PARROT;
    }

	@Override
	public boolean isDancing() {
		return this.getHandle().isSongPlaying();
	}

}