/**
 * Cardboard
 * Copyright (C) 2020-2025 contributors
 */
package org.cardboardpowered.bridge.world.entity;

import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;


public interface EntityBridge {

    default public CraftEntity getBukkitEntity() {
		System.out.println("getBukkitEntity not implemented for " + this.getClass().getName());
    	return null;
	}

    void setProjectileSourceBukkit(ProjectileSource source);

    ProjectileSource getProjectileSourceBukkit();

    boolean isValidBF();

    void setValid(boolean b);

    void setOriginBF(Location loc);

    Location getOriginBF();

    ArrayList<org.bukkit.inventory.ItemStack> cardboard_getDrops();

    void cardboard_setDrops(ArrayList<ItemStack> drops);

    boolean cardboard_getForceDrops();

    void cardboard_setForceDrops(boolean forceDrops);

	Level mc_world();

	AABB cardboad_getBoundingBoxAt(double x2, double y2, double z2);

	void cb$setInWorld(boolean b);
	
	boolean cb$getInWorld();

	CraftEntity getBukkitEntityRaw();

    boolean cardboard$isCollidable(boolean ignoreClimbing);

    boolean cardboard$canCollideWithBukkit(Entity entity);

    float cardboard$getBukkitYaw();

    boolean cardboard$isPersistent();

    void cardboard$setPersistent(boolean persistent);
}
