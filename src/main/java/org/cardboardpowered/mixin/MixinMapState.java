/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
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
package org.cardboardpowered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.impl.map.MapViewImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinMapState;

import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryKey;

@Mixin(MapState.class)
public class MixinMapState implements IMixinMapState {

    public MapViewImpl mapView;

    //@Inject(at = @At("TAIL"), method="<init>*")
    //public void setMapView(String s, CallbackInfo ci) {
    //    mapView = new MapViewImpl((MapState)(Object)this);
    //}

    @Inject(at = @At("TAIL"), method="<init>*")
    public void setMapView(int a, int b, byte c, boolean d, boolean e, boolean f, RegistryKey key, CallbackInfo ci) {
        mapView = new MapViewImpl((MapState)(Object)this);
    }


    @Override
    public MapViewImpl getMapViewBF() {
        return mapView;
    }

}