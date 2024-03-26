/**
 * Cardboard
 * Copyright (C) 2020-2022 contributors
 */
package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import net.minecraft.util.math.Box;


public interface IMixinEntity {

    CraftEntity getBukkitEntity();

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

	World mc_world();

	Box cardboad_getBoundingBoxAt(double x2, double y2, double z2);

}
