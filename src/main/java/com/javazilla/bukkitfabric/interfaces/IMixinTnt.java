package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.entity.LivingEntity;

public interface IMixinTnt {

    public void setSourceBF(LivingEntity entity);

}