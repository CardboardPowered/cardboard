/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinSaddledComponent;

import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;

@Mixin(SaddledComponent.class)
public class MixinSaddledComponent implements IMixinSaddledComponent {

    @Shadow public DataTracker dataTracker;
    @Shadow public TrackedData<Integer> boostTime;
    @Shadow public boolean boosted;
    @Shadow public int boostedTime; // field_23216
    @Shadow public int currentBoostTime;

    @Override
    public void setBoostTicks(int ticks) {
        this.boosted = true;
        this.boostedTime = 0;
        this.currentBoostTime = ticks;
        this.dataTracker.set(this.boostTime, this.currentBoostTime);
    }

}
