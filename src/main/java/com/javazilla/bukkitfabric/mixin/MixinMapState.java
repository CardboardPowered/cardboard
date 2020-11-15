package com.javazilla.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.impl.map.MapViewImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinMapState;

import net.minecraft.item.map.MapState;
import net.minecraft.util.registry.RegistryKey;

@Mixin(MapState.class)
public class MixinMapState implements IMixinMapState {

    public MapViewImpl mapView;

    @Inject(at = @At("TAIL"), method="<init>*")
    public void setMapView(int a, int b, byte c, boolean d, boolean e, boolean f, RegistryKey key, CallbackInfo ci) {
        mapView = new MapViewImpl((MapState)(Object)this);
    }

    @Override
    public MapViewImpl getMapViewBF() {
        return mapView;
    }

}