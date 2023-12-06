package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.passive.MooshroomEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class MushroomImpl extends CowImpl implements MushroomCow {

    public MushroomImpl(CraftServer server, MooshroomEntity entity) {
        super(server, entity);
    }

    @Override
    public MooshroomEntity getHandle() {
        return (MooshroomEntity) nms;
    }

    @Override
    public Variant getVariant() {
        return Variant.values()[getHandle().getVariant().ordinal()];
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "variant");
        getHandle().setVariant(MooshroomEntity.Type.values()[variant.ordinal()]);
    }

    @Override
    public String toString() {
        return "MushroomCow";
    }

    @Override
    public EntityType getType() {
        return EntityType.MUSHROOM_COW;
    }

	@Override
	public int getStewEffectDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
    public PotionEffectType getStewEffectType() {
        // StatusEffect effect = this.getHandle().stewEffect;
        // if (effect == null) {
       //      return null;
        // }
        return null; // PotionEffectType.getById((int)StatusEffect.getRawId(effect));
    }

	@Override
	public void setStewEffect(@Nullable PotionEffectType arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStewEffectDuration(int arg0) {
		// TODO Auto-generated method stub
		//this.getHandle().stewEffectDuration = duration;
	}

}