package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractProjectile extends CraftEntity implements Projectile {

    private boolean doesBounce;

    public AbstractProjectile(CraftServer server, net.minecraft.entity.Entity entity) {
        super(entity);
        doesBounce = false;
    }

    @Override
    public boolean doesBounce() {
        return doesBounce;
    }

    @Override
    public void setBounce(boolean doesBounce) {
        this.doesBounce = doesBounce;
    }

	@Override
	public @Nullable ProjectileSource getShooter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasBeenShot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLeftShooter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHasBeenShot(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHasLeftShooter(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShooter(@Nullable ProjectileSource arg0) {
		// TODO Auto-generated method stub
		
	}

}