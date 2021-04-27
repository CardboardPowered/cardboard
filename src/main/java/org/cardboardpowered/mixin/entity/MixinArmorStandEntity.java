package org.cardboardpowered.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinArmorStandEntity;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(ArmorStandEntity.class)
public class MixinArmorStandEntity extends MixinEntity implements IMixinArmorStandEntity {

    public boolean canMove = true; // Paper

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

    @Override
    public boolean canMoveBF() {
        return canMove;
    }

    @Override
    public void setCanMoveBF(boolean b) {
        this.canMove = b;
    }

    @Shadow public void setHideBasePlate(boolean flag) {}
    @Shadow public void setMarker(boolean flag) {}
    @Shadow public void setShowArms(boolean flag) {}
    @Shadow public void setSmall(boolean flag) {}

    // Paper start
    @Override
    public void move(MovementType moveType, Vec3d vec3d) {
        if (this.canMove) super.move(moveType, vec3d);
    }
    // Paper end

}