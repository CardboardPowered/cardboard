/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
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

import java.util.Map;

import org.cardboardpowered.impl.block.CapturedBlockState;
import org.cardboardpowered.impl.world.WorldImpl;

import net.minecraft.util.math.BlockPos;

public interface IMixinWorld {

    WorldImpl getWorldImpl();

    Map<BlockPos, CapturedBlockState> getCapturedBlockStates_BF();

    boolean isCaptureBlockStates_BF();

    void setCaptureBlockStates_BF(boolean b);

    void set_bukkit_world(WorldImpl world);

}