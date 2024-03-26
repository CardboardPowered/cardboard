package org.cardboardpowered.impl.entity;

import java.util.UUID;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.hit.EntityHitResult;

public class AbstractProjectile extends CraftEntity implements Projectile {

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
	
	// 1.19.4:

	@Override
	 public boolean canHitEntity(org.bukkit.entity.Entity entity) {
        //return this.getHandle().canHit(((CraftEntity)entity).getHandle());
		return this.getHandle().canHit();
    }

	@Override
	public @Nullable UUID getOwnerUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void hitEntity(@NotNull Entity arg0) {
		// TODO Auto-generated method stub
        // this.getHandle().preOnHit(new EntityHitResult(((CraftEntity)entity).getHandle()));

	}

	@Override
	public void hitEntity(@NotNull Entity arg0, @NotNull Vector arg1) {
		// TODO Auto-generated method stub
		
	}

}