package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityReference;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Vex;

public class CraftVex extends CraftMonster implements Vex {

	public CraftVex(CraftServer server, net.minecraft.world.entity.monster.Vex entity) {
		super(server, entity);
	}

	@Override
	public net.minecraft.world.entity.monster.Vex getHandle() {
		return (net.minecraft.world.entity.monster.Vex) this.entity;
	}

	@Override
	public org.bukkit.entity.Mob getSummoner() {
		// 26.2: Vex#getOwner widened from Mob to LivingEntity
		net.minecraft.world.entity.LivingEntity owner = this.getHandle().getOwner();
		return owner instanceof net.minecraft.world.entity.Mob mob
				? (org.bukkit.entity.Mob) mob.getBukkitEntity() : null;
	}

	@Override
	public org.bukkit.entity.LivingEntity getOwner() {
		net.minecraft.world.entity.LivingEntity owner = this.getHandle().getOwner();
		return owner != null ? (org.bukkit.entity.LivingEntity) owner.getBukkitEntity() : null;
	}

	@Override
	public void setSummoner(org.bukkit.entity.Mob summoner) {
		this.getHandle().owner = summoner == null ? null : EntityReference.of(((CraftMob) summoner).getHandle());
	}

	@Override
	public boolean hasLimitedLifetime() {
		return this.getHandle().hasLimitedLife;
	}

	@Override
	public void setLimitedLifetime(boolean hasLimitedLifetime) {
		this.getHandle().hasLimitedLife = hasLimitedLifetime;
	}

	@Override
	public int getLimitedLifetimeTicks() {
		return this.getHandle().limitedLifeTicks;
	}

	@Override
	public void setLimitedLifetimeTicks(int ticks) {
		this.getHandle().limitedLifeTicks = ticks;
	}

	@Override
	public boolean isCharging() {
		return this.getHandle().isCharging();
	}

	@Override
	public void setCharging(boolean charging) {
		this.getHandle().setIsCharging(charging);
	}

	@Override
	public Location getBound() {
		BlockPos pos = this.getHandle().getBoundOrigin();
		return (pos == null) ? null : CraftLocation.toBukkit(pos, this.getWorld());
	}

	@Override
	public void setBound(Location location) {
		if (location == null) {
			this.getHandle().setBoundOrigin(null);
		} else {
			Preconditions.checkArgument(this.getWorld().equals(location.getWorld()), "The bound world cannot be different to the entity's world.");
			this.getHandle().setBoundOrigin(CraftLocation.toBlockPosition(location));
		}
	}

	@Override
	public int getLifeTicks() {
		return this.getHandle().limitedLifeTicks;
	}

	@Override
	public void setLifeTicks(int lifeTicks) {
		this.getHandle().setLimitedLife(lifeTicks);
		if (lifeTicks < 0) {
			this.getHandle().hasLimitedLife = false;
		}
	}

	@Override
	public boolean hasLimitedLife() {
		return this.getHandle().hasLimitedLife;
	}

    // 26.2: Vex owner widened from Mob to LivingEntity
    @Override
    public void setOwner(org.bukkit.entity.LivingEntity owner) {
        this.getHandle().setOwner(owner == null ? null
                : (net.minecraft.world.entity.Mob) ((CraftLivingEntity) owner).getHandle());
    }

}