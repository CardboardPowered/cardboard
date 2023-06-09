package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.AbstractSkeletonEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;

@SuppressWarnings("deprecation")
public class SkeletonImpl extends MonsterImpl implements Skeleton {

    public SkeletonImpl(CraftServer server, AbstractSkeletonEntity entity) {
        super(server, entity);
    }

    @Override
    public AbstractSkeletonEntity getHandle() {
        return (AbstractSkeletonEntity) nms;
    }

    @Override
    public String toString() {
        return "SkeletonImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.SKELETON;
    }

    @Override
    public SkeletonType getSkeletonType() {
       return SkeletonType.NORMAL;
    }

    @Override
    public void setSkeletonType(SkeletonType type) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void rangedAttack(LivingEntity arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setChargingAttack(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShouldBurnInDay(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean shouldBurnInDay() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getConversionTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isConverting() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setConversionTime(int arg0) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public int inPowderedSnowTime() {
		// TODO Auto-generated method stub
		return 0;
	}

}