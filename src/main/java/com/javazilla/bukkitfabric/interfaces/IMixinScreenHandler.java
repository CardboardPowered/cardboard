/**
 * Cardboard - CardboardPowered.org
 * Copyright (C) 2020 CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.interfaces;

import java.util.List;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public interface IMixinScreenHandler {

    CardboardInventoryView getBukkitView();

    Text getTitle();

    void setTitle(Text title);

    void transferTo(ScreenHandler other, CraftHumanEntity player);

    DefaultedList<ItemStack> getTrackedStacksBF();

    void setTrackedStacksBF(DefaultedList<ItemStack> trackedStacks);

    void setSlots( DefaultedList<Slot> slots);

    void setCheckReachable(boolean bl);

}