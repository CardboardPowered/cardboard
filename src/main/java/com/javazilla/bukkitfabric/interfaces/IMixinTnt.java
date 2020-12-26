package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.entity.LivingEntity;

public interface IMixinTnt {

    void setSourceBF(LivingEntity entity);

}