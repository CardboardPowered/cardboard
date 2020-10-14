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
 * General Public License for more details.
 */
package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.text.Style;

// TODO
public interface IMixinStyle {

    public Style setStrikethrough(Boolean obool);

    public Style setUnderline(Boolean obool);

    public Style setRandom(Boolean obool);

}