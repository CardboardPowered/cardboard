/**
 * Cardboard - Paper API for Fabric
 * Copyright (C) 2020-2025
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 */
package org.cardboardpowered.bridge.world;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;

public interface ContainerBridge {

    default java.util.List<ItemStack> getContents() {
    	Container container = (Container) this;
    	return new java.util.AbstractList<ItemStack>() {

    		@Override
    		public ItemStack get(int index) {
    			return container.getItem(index);
    		}

    		@Override
    		public ItemStack set(int index, ItemStack element) {
    			ItemStack previous = container.getItem(index);
    			container.setItem(index, element);
    			return previous;
    		}

    		@Override
    		public int size() {
    			return container.getContainerSize();
    		}
    	};
    }

    default void onOpen(CraftHumanEntity who) {
    }

    // These are defaulted rather than abstract because MixinContainer grafts this
    // interface onto every net.minecraft.world.Container. Any container class
    // without a dedicated mixin would otherwise throw AbstractMethodError at the
    // first call site, which callers cannot catch.
    default void onClose(CraftHumanEntity who) {
    }

    default java.util.List<org.bukkit.entity.HumanEntity> getViewers() {
        return java.util.Collections.emptyList();
    }

    default org.bukkit.inventory.InventoryHolder getOwner() {
        return null;
    }

    default void cardboard$setMaxStackSize(int size) {
    }

    default org.bukkit.Location getLocation() {
        return null;
    }

    default Recipe<?> getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(Recipe<?> recipe) {
    }

    int MAX_STACK = 64;

    default void cardboard$setOwner(InventoryHolder owner) {
        // TODO
    }

}