/**
 * Cardboard
 * Copyright (C) 2020-2022 contributors
 */
package com.javazilla.bukkitfabric.interfaces;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

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

}