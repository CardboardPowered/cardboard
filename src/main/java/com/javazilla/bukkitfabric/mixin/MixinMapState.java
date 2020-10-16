package com.javazilla.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.map.MapViewImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinMapState;

import net.minecraft.item.map.MapState;

@Mixin(MapState.class)
public class MixinMapState implements IMixinMapState {

    public MapViewImpl mapView;

    @Inject(at = @At("TAIL"), method="<init>*")
    public void setMapView(String s, CallbackInfo ci) {
        mapView = new MapViewImpl((MapState)(Object)this);
    }

    @Override
    public MapViewImpl getMapViewBF() {
        return mapView;
    }

}