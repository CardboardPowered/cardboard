package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinArmorStandEntity;

import net.minecraft.entity.decoration.ArmorStandEntity;

@Mixin(ArmorStandEntity.class)
public class MixinArmorStandEntity extends MixinEntity implements IMixinArmorStandEntity {

    @Override
    public void setHideBasePlateBF(boolean b) {
        setHideBasePlate(b);
    }

    @Override
    public void setShowArmsBF(boolean arms) {
        setShowArms(arms);
    }

    @Override
    public void setSmallBF(boolean small) {
        setSmall(small);
    }

    @Override
    public void setMarkerBF(boolean marker) {
        setMarker(marker);
    }

    @Shadow public void setHideBasePlate(boolean flag) {}
    @Shadow public void setMarker(boolean flag) {}
    @Shadow public void setShowArms(boolean flag) {}
    @Shadow public void setSmall(boolean flag) {}

}
