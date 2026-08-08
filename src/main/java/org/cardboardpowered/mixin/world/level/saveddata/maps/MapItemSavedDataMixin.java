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
package org.cardboardpowered.mixin.world.level.saveddata.maps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.cardboardpowered.impl.map.MapViewImpl;
import org.cardboardpowered.bridge.world.level.saveddata.maps.MapItemSavedDataBridge;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin implements MapItemSavedDataBridge {

    public MapViewImpl mapView;

    public int mapIdBF = -1;

    //@Inject(at = @At("TAIL"), method="<init>*")
    //public void setMapView(String s, CallbackInfo ci) {
    //    mapView = new MapViewImpl((MapState)(Object)this);
    //}

    @Inject(at = @At("TAIL"), method="<init>*")
    public void setMapView(int a, int b, byte c, boolean d, boolean e, boolean f, ResourceKey key, CallbackInfo ci) {
        mapView = new MapViewImpl((MapItemSavedData)(Object)this);
    }


    @Override
    public MapViewImpl getMapViewBF() {
        // Maps deserialized from disk do not go through the constructor injected above,
        // so build the view on first use instead of handing back null.
        if (mapView == null) {
            mapView = new MapViewImpl((MapItemSavedData)(Object)this);
        }
        return mapView;
    }

    @Override
    public int getMapIdBF() {
        return mapIdBF;
    }

    @Override
    public void setMapIdBF(int id) {
        this.mapIdBF = id;
    }

}