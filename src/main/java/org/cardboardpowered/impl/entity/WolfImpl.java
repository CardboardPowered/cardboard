package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.WolfEntity;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;

public class WolfImpl extends TameableAnimalImpl implements Wolf {

    public WolfImpl(CraftServer server, WolfEntity wolf) {
        super(server, wolf);
    }

    @Override
    public boolean isAngry() {
        return getHandle().hasAngerTime();
    }

    @Override
    public void setAngry(boolean angry) {
        if (angry) {
            getHandle().chooseRandomAngerTime();
        } else getHandle().stopAnger();
    }

    @Override
    public WolfEntity getHandle() {
        return (WolfEntity) nms;
    }

    @Override
    public EntityType getType() {
        return EntityType.WOLF;
    }

    @SuppressWarnings("deprecation")
    @Override
    public DyeColor getCollarColor() {
        return DyeColor.getByWoolData((byte) getHandle().getCollarColor().getId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setCollarColor(DyeColor color) {
        getHandle().setCollarColor(net.minecraft.util.DyeColor.byId(color.getWoolData()));
    }

	@Override
	public boolean isInterested() {
		return this.getHandle().isBegging();
	}

	@Override
	public void setInterested(boolean arg0) {
		this.getHandle().setBegging(arg0);
	}

	@Override
	public float getTailAngle() {
		 return this.getHandle().getTailAngle();
	}

	@Override
	public boolean isWet() {
		 return this.getHandle().isFurWet();
	}

}