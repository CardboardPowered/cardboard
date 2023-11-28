package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.EndermiteEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;

public class EndermiteImpl extends MonsterImpl implements Endermite {

    public EndermiteImpl(CraftServer server, EndermiteEntity entity) {
        super(server, entity);
    }

    @Override
    public EndermiteEntity getHandle() {
        return (EndermiteEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "EndermiteImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDERMITE;
    }

    @Override
    public boolean isPlayerSpawned() {
        return false; // TODO 1.17ify getHandle().isPlayerSpawned();
    }

    @Override
    public void setPlayerSpawned(boolean playerSpawned) {
     // TODO 1.17ify  getHandle().setPlayerSpawned(playerSpawned);
    }

	@Override
	public int getLifetimeTicks() {
		// TODO Auto-generated method stub
		// return this.getHandle().lifeTime;
		return 0;
	}

	@Override
	public void setLifetimeTicks(int arg0) {
		// TODO Auto-generated method stub
		// this.getHandle().lifeTime = ticks;
	}

}