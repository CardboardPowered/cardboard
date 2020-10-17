package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinPersistentProjectileEntity;

import net.minecraft.entity.projectile.PersistentProjectileEntity;

@Mixin(PersistentProjectileEntity.class)
public class MixinPersistentProjectileEntity implements IMixinPersistentProjectileEntity {

    @Shadow public boolean inGround;
    @Shadow public int life;
    @Shadow public int punch;

    @Override
    public int getPunchBF() {
        return punch;
    }

    @Override
    public boolean getInGroundBF() {
        return inGround;
    }

    @Override
    public void setLifeBF(int value) {
        this.life = value;
    }

}
